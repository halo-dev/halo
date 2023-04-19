package run.halo.app.core.extension.attachment.endpoint;

import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static run.halo.app.infra.utils.FileNameUtils.randomFileName;
import static run.halo.app.infra.utils.FileUtils.checkDirectoryTraversal;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.attachment.Attachment.AttachmentSpec;
import run.halo.app.core.extension.attachment.Constant;
import run.halo.app.core.extension.attachment.Policy;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.Metadata;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.infra.exception.AttachmentAlreadyExistsException;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.infra.utils.JsonUtils;

@Slf4j
@Component
class LocalAttachmentUploadHandler implements AttachmentHandler {

    private final HaloProperties haloProp;

    private final ExternalUrlSupplier externalUrl;

    public LocalAttachmentUploadHandler(HaloProperties haloProp,
        ExternalUrlSupplier externalUrl) {
        this.haloProp = haloProp;
        this.externalUrl = externalUrl;
    }

    Path getAttachmentsRoot() {
        return haloProp.getWorkDir().resolve("attachments");
    }

    @Override
    public Mono<Attachment> upload(UploadContext uploadOption) {
        return Mono.just(uploadOption)
            .filter(option -> this.shouldHandle(option.policy()))
            .flatMap(option -> {
                var configMap = option.configMap();
                var settingJson = configMap.getData().getOrDefault("default", "{}");
                var setting = JsonUtils.jsonToObject(settingJson, PolicySetting.class);

                final var attachmentsRoot = getAttachmentsRoot();
                final var uploadRoot = attachmentsRoot.resolve("upload");
                final var file = option.file();
                final Path attachmentPath;
                if (StringUtils.hasText(setting.getLocation())) {
                    attachmentPath =
                        uploadRoot.resolve(setting.getLocation()).resolve(file.filename());
                } else {
                    attachmentPath = uploadRoot.resolve(file.filename());
                }
                checkDirectoryTraversal(uploadRoot, attachmentPath);

                return Mono.fromRunnable(
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
                        log.info("Wrote attachment {} into {}", file.filename(), path);
                        // TODO check the file extension
                        var metadata = new Metadata();
                        metadata.setName(UUID.randomUUID().toString());
                        var relativePath = attachmentsRoot.relativize(path).toString();

                        var pathSegments = new ArrayList<String>();
                        pathSegments.add("upload");
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
                        return attachment;
                    })
                    .onErrorMap(FileAlreadyExistsException.class,
                        e -> new AttachmentAlreadyExistsException(e.getFile()));
            });
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
                        var attachmentsRoot = getAttachmentsRoot();
                        var attachmentPath = attachmentsRoot.resolve(localRelativePath);
                        checkDirectoryTraversal(attachmentsRoot, attachmentPath);

                        // delete it permanently
                        try {
                            log.info("{} is being deleted", attachmentPath);
                            boolean deleted = Files.deleteIfExists(attachmentPath);
                            if (deleted) {
                                log.info("{} was deleted successfully", attachment);
                            } else {
                                log.info("{} was not exist", attachment);
                            }
                        } catch (IOException e) {
                            throw Exceptions.propagate(e);
                        }
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
        var annotations = attachment.getMetadata().getAnnotations();
        if (annotations == null
            || !annotations.containsKey(Constant.URI_ANNO_KEY)) {
            return Mono.empty();
        }
        var uriStr = annotations.get(Constant.URI_ANNO_KEY);
        // the uriStr is encoded before.
        uriStr = UriUtils.decode(uriStr, StandardCharsets.UTF_8);
        var uri = UriComponentsBuilder.fromUri(externalUrl.get())
            // The URI has been encoded before, so there is no need to encode it again.
            .path(uriStr)
            .build()
            .toUri();
        return Mono.just(uri);
    }

    @Override
    public Mono<URI> getSharedURL(Attachment attachment,
        Policy policy,
        ConfigMap configMap,
        Duration ttl) {
        return getPermalink(attachment, policy, configMap);
    }

    private boolean shouldHandle(Policy policy) {
        if (policy == null
            || policy.getSpec() == null
            || !StringUtils.hasText(policy.getSpec().getTemplateName())) {
            return false;
        }
        return "local".equals(policy.getSpec().getTemplateName());
    }

    @Data
    public static class PolicySetting {

        private String location;

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
                .then(Mono.fromSupplier(pathRef::get));
        });
    }
}
