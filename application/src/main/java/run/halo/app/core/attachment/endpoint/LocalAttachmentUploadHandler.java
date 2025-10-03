package run.halo.app.core.attachment.endpoint;

import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static run.halo.app.infra.utils.FileNameUtils.randomFileName;
import static run.halo.app.infra.utils.FileUtils.checkDirectoryTraversal;
import static run.halo.app.infra.utils.FileUtils.deleteFileSilently;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;
import run.halo.app.core.attachment.AttachmentRootGetter;
import run.halo.app.core.attachment.ThumbnailSize;
import run.halo.app.core.attachment.thumbnail.LocalThumbnailService;
import run.halo.app.core.attachment.thumbnail.ThumbnailUtils;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.attachment.Attachment.AttachmentSpec;
import run.halo.app.core.extension.attachment.Constant;
import run.halo.app.core.extension.attachment.Policy;
import run.halo.app.core.extension.attachment.endpoint.AttachmentHandler;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.Metadata;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.infra.FileCategoryMatcher;
import run.halo.app.infra.exception.AttachmentAlreadyExistsException;
import run.halo.app.infra.exception.FileSizeExceededException;
import run.halo.app.infra.exception.FileTypeNotAllowedException;
import run.halo.app.infra.utils.FileNameUtils;
import run.halo.app.infra.utils.FileTypeDetectUtils;
import run.halo.app.infra.utils.JsonUtils;

@Slf4j
@Component
class LocalAttachmentUploadHandler implements AttachmentHandler {

    private static final String UPLOAD_PATH = "upload";

    private final AttachmentRootGetter attachmentDirGetter;

    private final ExternalUrlSupplier externalUrl;

    private final LocalThumbnailService localThumbnailService;

    private Clock clock = Clock.systemUTC();

    public LocalAttachmentUploadHandler(
        AttachmentRootGetter attachmentDirGetter,
        ExternalUrlSupplier externalUrl,
        LocalThumbnailService localThumbnailService) {
        this.attachmentDirGetter = attachmentDirGetter;
        this.externalUrl = externalUrl;
        this.localThumbnailService = localThumbnailService;
    }

    /**
     * Set clock for test.
     *
     * @param clock new clock
     */
    void setClock(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Mono<Attachment> upload(UploadContext uploadOption) {
        return Mono.just(uploadOption)
            .filter(option -> this.shouldHandle(option.policy()))
            .flatMap(option -> {
                var configMap = option.configMap();
                var setting = Optional.ofNullable(configMap)
                    .map(ConfigMap::getData)
                    .map(data -> data.get("default"))
                    .map(json -> JsonUtils.jsonToObject(json, PolicySetting.class))
                    .orElseGet(PolicySetting::new);

                final var attachmentsRoot = attachmentDirGetter.get();
                final var uploadRoot = attachmentsRoot.resolve(UPLOAD_PATH);
                final var file = option.file();
                final Path attachmentPath;
                final String filename = getFilename(file.filename(), setting);
                if (StringUtils.hasText(setting.getLocation())) {
                    attachmentPath =
                        uploadRoot.resolve(setting.getLocation()).resolve(filename);
                } else {
                    attachmentPath = uploadRoot.resolve(filename);
                }
                checkDirectoryTraversal(uploadRoot, attachmentPath);

                return validateFile(file, setting).then(Mono.fromRunnable(
                        () -> {
                            try {
                                // init parent folders
                                Files.createDirectories(attachmentPath.getParent());
                            } catch (IOException e) {
                                throw Exceptions.propagate(e);
                            }
                        })
                    .subscribeOn(Schedulers.boundedElastic())
                    .then(writeContent(file.content(), attachmentPath, true))
                    .map(path -> {
                        log.info("Wrote attachment {} into {}", filename, path);
                        // TODO check the file extension
                        var metadata = new Metadata();
                        metadata.setName(UUID.randomUUID().toString());
                        var relativePath = attachmentsRoot.relativize(path).toString();

                        var pathSegments = new ArrayList<String>();
                        pathSegments.add(UPLOAD_PATH);
                        for (Path p : uploadRoot.relativize(path)) {
                            pathSegments.add(p.toString());
                        }

                        var uri = UriComponentsBuilder.newInstance()
                            .pathSegment(pathSegments.toArray(String[]::new))
                            .encode(StandardCharsets.UTF_8)
                            .build()
                            .toString();
                        metadata.setAnnotations(Map.of(
                            Constant.LOCAL_REL_PATH_ANNO_KEY, relativePath,
                            Constant.URI_ANNO_KEY, uri));
                        var spec = new AttachmentSpec();
                        spec.setSize(path.toFile().length());
                        spec.setMediaType(Optional.ofNullable(file.headers().getContentType())
                            .map(MediaType::toString)
                            .orElse(null));
                        spec.setDisplayName(path.getFileName().toString());
                        var attachment = new Attachment();
                        attachment.setMetadata(metadata);
                        attachment.setSpec(spec);

                        attachment.setStatus(new Attachment.AttachmentStatus());
                        doGetPermalink(attachment).ifPresent(permalink ->
                            attachment.getStatus().setPermalink(permalink.toASCIIString())
                        );
                        var thumbnailLinks = doGetThumbnailLinks(attachment);
                        var thumbnails = thumbnailLinks.keySet().stream()
                            .collect(Collectors.toMap(
                                ThumbnailSize::name,
                                size -> thumbnailLinks.get(size).toASCIIString()
                            ));
                        if (!thumbnails.isEmpty()) {
                            attachment.getStatus().setThumbnails(thumbnails);
                        }
                        return attachment;
                    })
                    .onErrorMap(FileAlreadyExistsException.class,
                        e -> new AttachmentAlreadyExistsException(e.getFile()))
                );
            });
    }

    private Mono<Void> validateFile(FilePart file, PolicySetting setting) {
        var validations = new ArrayList<Publisher<?>>(2);
        var maxSize = setting.getMaxFileSize();
        if (maxSize != null && maxSize.toBytes() > 0) {
            validations.add(
                file.content()
                    .map(DataBuffer::readableByteCount)
                    .reduce(0L, Long::sum)
                    .filter(size -> size <= setting.getMaxFileSize().toBytes())
                    .switchIfEmpty(Mono.error(new FileSizeExceededException(
                        "File size exceeds the maximum limit",
                        "problemDetail.attachment.upload.fileSizeExceeded",
                        new Object[] {setting.getMaxFileSize().toKilobytes() + "KB"})
                    ))
            );
        }
        if (!CollectionUtils.isEmpty(setting.getAllowedFileTypes())) {
            var typeValidator = file.content()
                .next()
                .handle((dataBuffer, sink) -> {
                    var mimeType = detectMimeType(dataBuffer.asInputStream(), file.filename());
                    if (!FileTypeDetectUtils.isValidExtensionForMime(mimeType, file.filename())) {
                        handleFileTypeError(sink, "fileTypeNotMatch", mimeType);
                        return;
                    }
                    var isAllow = setting.getAllowedFileTypes()
                        .stream()
                        .map(FileCategoryMatcher::of)
                        .anyMatch(matcher -> matcher.match(mimeType));
                    if (isAllow) {
                        sink.next(dataBuffer);
                        return;
                    }
                    handleFileTypeError(sink, "fileTypeNotSupported", mimeType);
                });
            validations.add(typeValidator);
        }
        return Mono.when(validations);
    }

    private static void handleFileTypeError(SynchronousSink<Object> sink, String detailCode,
        String mimeType) {
        sink.error(new FileTypeNotAllowedException("File type is not allowed",
            "problemDetail.attachment.upload." + detailCode,
            new Object[] {mimeType})
        );
    }

    @NonNull
    private String detectMimeType(InputStream inputStream, String name) {
        try {
            return FileTypeDetectUtils.detectMimeType(inputStream, name);
        } catch (IOException e) {
            log.warn("Failed to detect file type", e);
            return "Unknown";
        }
    }

    @Override
    public Mono<Attachment> delete(DeleteContext deleteContext) {
        return Mono.just(deleteContext)
            .filter(context -> this.shouldHandle(context.policy()))
            .publishOn(Schedulers.boundedElastic())
            .doOnNext(context -> {
                var attachment = context.attachment();
                log.info("Trying to delete {} from local", attachment.getMetadata().getName());
                var annotations = attachment.getMetadata().getAnnotations();
                if (annotations != null) {
                    var localRelativePath = annotations.get(Constant.LOCAL_REL_PATH_ANNO_KEY);
                    if (StringUtils.hasText(localRelativePath)) {
                        var attachmentsRoot = attachmentDirGetter.get();
                        var attachmentPath = attachmentsRoot.resolve(localRelativePath);
                        deleteAttachmentFile(attachmentPath);
                        deleteThumbnails(attachmentPath);
                    }
                }
            })
            .map(DeleteContext::attachment);
    }

    @Override
    public Mono<URI> getPermalink(Attachment attachment, Policy policy, ConfigMap configMap) {
        if (!this.shouldHandle(policy)) {
            return Mono.empty();
        }
        return Mono.justOrEmpty(doGetPermalink(attachment));
    }

    @Override
    public Mono<URI> getSharedURL(Attachment attachment,
        Policy policy,
        ConfigMap configMap,
        Duration ttl) {
        return getPermalink(attachment, policy, configMap);
    }

    @Override
    public Mono<Map<ThumbnailSize, URI>> getThumbnailLinks(Attachment attachment, Policy policy,
        ConfigMap configMap) {
        if (!this.shouldHandle(policy)) {
            return Mono.empty();
        }
        return Mono.just(doGetThumbnailLinks(attachment));
    }

    protected Optional<URI> doGetPermalink(Attachment attachment) {
        var annotations = attachment.getMetadata().getAnnotations();
        if (annotations == null
            || !annotations.containsKey(Constant.URI_ANNO_KEY)) {
            return Optional.empty();
        }
        var uriStr = annotations.get(Constant.URI_ANNO_KEY);
        URI uri;
        try {
            uri = UriComponentsBuilder.fromUri(externalUrl.get())
                // The URI has been encoded before, so there is no need to encode it again.
                .path(uriStr)
                .build(true)
                .toUri();
        } catch (IllegalArgumentException e) {
            // The URI may not be encoded, so we need to build with encoding.
            uri = UriComponentsBuilder.fromUri(externalUrl.get())
                .path(uriStr)
                .build()
                .toUri();
        }
        return Optional.of(uri);
    }

    private Map<ThumbnailSize, URI> doGetThumbnailLinks(Attachment attachment) {
        if (attachment.getStatus() == null
            || !StringUtils.hasText(attachment.getStatus().getPermalink())) {
            return Map.of();
        }
        var permalinkUri = URI.create(attachment.getStatus().getPermalink());
        return ThumbnailUtils.buildSrcsetMap(permalinkUri);
    }

    private void deleteAttachmentFile(Path attachmentPath) {
        var attachmentsRoot = attachmentDirGetter.get();
        checkDirectoryTraversal(attachmentsRoot, attachmentPath);
        try {
            Files.deleteIfExists(attachmentPath);
            log.info("Deleted attachment file {}", attachmentPath);
        } catch (IOException e) {
            throw Exceptions.propagate(e);
        }
    }

    private void deleteThumbnails(Path attachmentPath) {
        this.localThumbnailService.delete(attachmentPath);
    }

    private boolean shouldHandle(Policy policy) {
        if (policy == null
            || policy.getSpec() == null
            || !StringUtils.hasText(policy.getSpec().getTemplateName())) {
            return false;
        }
        return "local".equals(policy.getSpec().getTemplateName());
    }

    /**
     * Write content into file. We will detect duplicate filename and auto-rename it with 3 times
     * retry.
     *
     * @param content is file content
     * @param targetPath is target path
     * @return file path
     */
    private Mono<Path> writeContent(Flux<DataBuffer> content,
        Path targetPath,
        boolean renameIfExists) {
        return Mono.defer(() -> {
            final var pathRef = new AtomicReference<>(targetPath);
            return Mono.defer(
                    // we have to use defer method to obtain a fresh path
                    () -> DataBufferUtils.write(content, pathRef.get(), CREATE_NEW))
                .retryWhen(Retry.max(3)
                    .filter(t -> {
                        if (renameIfExists) {
                            return t instanceof FileAlreadyExistsException;
                        }
                        return false;
                    })
                    .doAfterRetry(signal -> {
                        // rename the path
                        var oldPath = pathRef.get();
                        var fileName = randomFileName(oldPath.toString(), 4);
                        pathRef.set(oldPath.resolveSibling(fileName));
                    }))
                // Delete file already wrote partially into attachment folder
                // in case of content is terminated with an error
                .onErrorResume(t -> deleteFileSilently(pathRef.get()).then(Mono.error(t)))
                .then(Mono.fromSupplier(pathRef::get));
        });
    }

    private String getFilename(String filename, PolicySetting setting) {
        if (!setting.isAlwaysRenameFilename()) {
            return filename;
        }
        var renameStrategy = setting.getRenameStrategy();
        if (renameStrategy == null) {
            return filename;
        }
        var renameMethod = renameStrategy.getMethod();
        if (renameMethod == null) {
            renameMethod = RenameMethod.RANDOM;
        }
        var excludeOriginalFilename = renameStrategy.isExcludeOriginalFilename();
        switch (renameMethod) {
            case TIMESTAMP -> {
                return FileNameUtils.renameFilename(
                    filename,
                    () -> {
                        var now = clock.instant();
                        return now.toEpochMilli() + "";
                    },
                    excludeOriginalFilename);
            }
            case UUID -> {
                return FileNameUtils.renameFilename(
                    filename,
                    () -> UUID.randomUUID().toString(),
                    excludeOriginalFilename
                );
            }
            default -> {
                return FileNameUtils.renameFilename(
                    filename,
                    () -> {
                        var length = renameStrategy.getRandomLength();
                        if (length < 8) {
                            length = 8;
                        } else if (length > 64) {
                            // The max filename length is 256, so we limit the random length to 64
                            // for most cases.
                            length = 64;
                        }
                        return RandomStringUtils.secure().nextAlphabetic(length);
                    },
                    excludeOriginalFilename);
            }
        }
    }


    @Data
    public static class PolicySetting {

        private String location;

        private DataSize maxFileSize;

        private Set<String> allowedFileTypes;

        private boolean alwaysRenameFilename;

        private RenameStrategy renameStrategy;

        public void setMaxFileSize(String maxFileSize) {
            if (!StringUtils.hasText(maxFileSize)) {
                return;
            }
            this.maxFileSize = DataSize.parse(maxFileSize);
        }
    }

    public enum RenameMethod {
        RANDOM,
        UUID,
        TIMESTAMP
    }

    @Data
    public static class RenameStrategy {

        private RenameMethod method;

        private int randomLength = 32;

        private boolean excludeOriginalFilename;

    }

}
