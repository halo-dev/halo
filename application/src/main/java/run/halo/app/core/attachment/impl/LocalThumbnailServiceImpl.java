package run.halo.app.core.attachment.impl;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static run.halo.app.extension.index.query.QueryFactory.and;
import static run.halo.app.extension.index.query.QueryFactory.equal;
import static run.halo.app.extension.index.query.QueryFactory.isNull;

import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.core.attachment.AttachmentRootGetter;
import run.halo.app.core.attachment.AttachmentUtils;
import run.halo.app.core.attachment.LocalThumbnailService;
import run.halo.app.core.attachment.ThumbnailGenerator;
import run.halo.app.core.attachment.ThumbnailSigner;
import run.halo.app.core.attachment.ThumbnailSize;
import run.halo.app.core.extension.attachment.LocalThumbnail;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.infra.exception.NotFoundException;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocalThumbnailServiceImpl implements LocalThumbnailService {
    private final AttachmentRootGetter attachmentDirGetter;
    private final ReactiveExtensionClient client;
    private final ExternalUrlSupplier externalUrlSupplier;

    private static Path buildThumbnailStorePath(Path rootPath, String fileName, String year,
        ThumbnailSize size) {
        return rootPath
            .resolve("thumbnails")
            .resolve(year)
            .resolve("w" + size.getWidth())
            .resolve(fileName);
    }

    static String endpointFor(String fileName, String year, ThumbnailSize size) {
        return "/upload/thumbnails/%s/w%s/%s".formatted(year, size.getWidth(), fileName);
    }

    static String getThumbnailFileName(URL imageUrl) {
        var fileName = substringAfterLast(imageUrl.getPath(), "/");
        fileName = defaultIfBlank(fileName, randomAlphanumeric(10));
        return ThumbnailGenerator.sanitizeFileName(fileName);
    }

    static String getYear() {
        return String.valueOf(LocalDateTime.now().getYear());
    }

    @Override
    public Mono<Resource> getThumbnail(String year, ThumbnailSize size, String filename) {
        Assert.notNull(year, "Year must not be null.");
        Assert.notNull(size, "Thumbnail size must not be null.");
        Assert.notNull(filename, "Filename must not be null.");
        var filePath = buildThumbnailStorePath(attachmentDirGetter.get(), filename, year, size);
        if (Files.exists(filePath)) {
            return Mono.just(new FileSystemResource(filePath));
        }
        var thumbnailUri = endpointFor(filename, year, size);
        var thumbSignature = ThumbnailSigner.generateSignature(thumbnailUri);
        return fetchThumbnail(thumbSignature)
            .switchIfEmpty(Mono.error(new NotFoundException("Thumbnail resource not found.")))
            .flatMap(this::generate);
    }

    private Mono<LocalThumbnail> fetchThumbnail(String thumbSignature) {
        return client.listBy(LocalThumbnail.class, ListOptions.builder()
                .fieldQuery(and(
                    equal("spec.thumbSignature", thumbSignature),
                    isNull("metadata.deletionTimestamp"))
                )
                .build(), PageRequestImpl.ofSize(1))
            .flatMap(result -> Mono.justOrEmpty(ListResult.first(result)));
    }

    @Override
    public Mono<Resource> generate(LocalThumbnail thumbnail) {
        Assert.notNull(thumbnail, "Thumbnail must not be null.");
        var filePath = toFilePath(thumbnail.getSpec().getFilePath());
        if (Files.exists(filePath)) {
            return Mono.just(new FileSystemResource(filePath));
        }
        var imageUrl = AttachmentUtils.toUrl(thumbnail.getSpec().getImageUrl());
        var size = thumbnail.getSpec().getSize();
        return new ThumbnailGenerator(imageUrl, size, filePath).generate()
            .thenReturn((Resource) new FileSystemResource(filePath))
            .onErrorResume(e -> {
                log.warn("Failed to generate thumbnail for image: {}", imageUrl, e);
                return Mono.just(new UrlResource(imageUrl));
            });
    }

    @Override
    public Mono<LocalThumbnail> create(URL imageUrl, ThumbnailSize size) {
        Assert.notNull(imageUrl, "Image URL must not be null.");
        Assert.notNull(size, "Thumbnail size must not be null.");
        var year = getYear();
        return ThumbnailInfo.from(imageUrl, size, attachmentDirGetter.get(), year)
            .flatMap(thumbInfo -> {
                var thumbnail = new LocalThumbnail();
                thumbnail.setMetadata(new Metadata());
                thumbnail.getMetadata().setGenerateName("thumbnail-");
                var thumbnailUri = endpointFor(thumbInfo.fileName(), year, size);
                var thumbSignature = ThumbnailSigner.generateSignature(thumbnailUri);
                thumbnail.setSpec(new LocalThumbnail.Spec()
                    .setImageSignature(signatureForImageUri(imageUrl.toString()))
                    .setFilePath(toRelativeUnixPath(thumbInfo.filePath()))
                    .setImageUrl(imageUrl.toString())
                    .setSize(size)
                    .setThumbSignature(thumbSignature)
                    .setThumbnailUri(thumbnailUri));
                return client.create(thumbnail);
            });
    }

    @Override
    public Mono<Void> delete(URI imageUri) {
        var signature = signatureForImageUri(imageUri.toString());
        return client.listAll(LocalThumbnail.class, ListOptions.builder()
                .fieldQuery(and(
                    equal("spec.imageSignature", signature),
                    isNull("metadata.deletionTimestamp"))
                )
                .build(), Sort.unsorted())
            .flatMap(thumbnail -> {
                var filePath = toFilePath(thumbnail.getSpec().getFilePath());
                return deleteFile(filePath)
                    .then(client.delete(thumbnail));
            })
            .then();
    }


    Path toFilePath(String relativeUnixPath) {
        Assert.notNull(relativeUnixPath, "Relative path must not be null.");
        var systemPath = removeStart(relativeUnixPath, "/")
            .replace("/", FileSystems.getDefault().getSeparator());
        return attachmentDirGetter.get().resolve(systemPath);
    }

    private String toRelativeUnixPath(Path filePath) {
        var dir = attachmentDirGetter.get().toString();
        var relativePath = removeStart(filePath.toString(), dir);
        return relativePath.replace("\\", "/");
    }

    private Mono<Void> deleteFile(Path path) {
        return Mono.fromRunnable(
                () -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (Exception e) {
                        throw Exceptions.propagate(e);
                    }
                })
            .subscribeOn(Schedulers.boundedElastic())
            .then();
    }

    /**
     * Generate signature for the given image URL.
     * <p>if externalUrl is not configured, it will return the signature generated by the image URL
     * directly, otherwise, it will return the signature generated by the relative path of the
     * image URL to the external URL.</p>
     */
    String signatureForImageUri(String imageUriStr) {
        var externalUrl = externalUrlSupplier.getRaw();
        var imageUri = URI.create(imageUriStr);
        if (externalUrl == null || !isSameOrigin(imageUri, externalUrl)) {
            return ThumbnailSigner.generateSignature(imageUriStr);
        }
        return ThumbnailSigner.generateSignature(imageUri.getPath());
    }

    private boolean isSameOrigin(URI imageUri, URL externalUrl) {
        return StringUtils.equals(imageUri.getHost(), externalUrl.getHost())
            && imageUri.getPort() == externalUrl.getPort();
    }

    record ThumbnailInfo(String fileName, Path filePath) {
        public static Mono<ThumbnailInfo> from(URL image, ThumbnailSize size, Path rootPath,
            String year) {
            return Mono.defer(() -> {
                var fileName = getThumbnailFileName(image);
                var filePath = generateUniqueFilePath(rootPath, fileName, year, size);
                return Mono.just(new ThumbnailInfo(fileName, filePath));
            });
        }

        private static Path generateUniqueFilePath(Path rootPath, String fileName, String year,
            ThumbnailSize size) {
            var filePath = buildThumbnailStorePath(rootPath, fileName, year, size);
            if (Files.exists(filePath)) {
                fileName = appendRandomSuffix(fileName);
                return generateUniqueFilePath(rootPath, fileName, year, size);
            }
            return filePath;
        }

        private static String appendRandomSuffix(String fileName) {
            var baseName = StringUtils.substringBeforeLast(fileName, ".");
            var extension = substringAfterLast(fileName, ".");
            var randomSuffix = randomAlphanumeric(5);
            return String.format("%s_%s.%s", baseName, randomSuffix, extension);
        }
    }
}
