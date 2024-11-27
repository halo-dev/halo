package run.halo.app.core.attachment.impl;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static run.halo.app.extension.MetadataUtil.nullSafeAnnotations;
import static run.halo.app.extension.index.query.QueryFactory.and;
import static run.halo.app.extension.index.query.QueryFactory.equal;
import static run.halo.app.extension.index.query.QueryFactory.isNull;

import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;
import run.halo.app.core.attachment.AttachmentRootGetter;
import run.halo.app.core.attachment.LocalThumbnailService;
import run.halo.app.core.attachment.ThumbnailGenerator;
import run.halo.app.core.attachment.ThumbnailSigner;
import run.halo.app.core.attachment.ThumbnailSize;
import run.halo.app.core.attachment.extension.LocalThumbnail;
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

    static String geImageFileName(URI imageUri) {
        var fileName = substringAfterLast(imageUri.getPath(), "/");
        fileName = defaultIfBlank(fileName, randomAlphanumeric(10));
        return ThumbnailGenerator.sanitizeFileName(fileName);
    }

    static String getYear() {
        return String.valueOf(LocalDateTime.now().getYear());
    }

    @Override
    public Mono<URI> getOriginalImageUri(URI thumbnailUri) {
        return fetchThumbnail(thumbnailUri)
            .map(local -> URI.create(local.getSpec().getImageUri()))
            .switchIfEmpty(Mono.error(() -> new NotFoundException("Resource not found.")));
    }

    @Override
    public Mono<Resource> getThumbnail(URI thumbnailUri) {
        Assert.notNull(thumbnailUri, "Thumbnail URI must not be null.");
        return fetchThumbnail(thumbnailUri)
            .flatMap(thumbnail -> {
                var filePath = toFilePath(thumbnail.getSpec().getFilePath());
                if (Files.exists(filePath)) {
                    return getResourceMono(() -> new FileSystemResource(filePath));
                }
                return generate(thumbnail)
                    .then(Mono.empty());
            });
    }

    @Override
    public Mono<Resource> getThumbnail(URI originalImageUri, ThumbnailSize size) {
        var imageHash = signatureForImageUri(originalImageUri);
        return fetchByImageHashAndSize(imageHash, size)
            .flatMap(this::generate);
    }

    private Mono<LocalThumbnail> fetchThumbnail(URI thumbnailUri) {
        Assert.notNull(thumbnailUri, "Thumbnail URI must not be null.");
        var thumbSignature = ThumbnailSigner.generateSignature(thumbnailUri);
        return client.listBy(LocalThumbnail.class, ListOptions.builder()
                .fieldQuery(equal("spec.thumbSignature", thumbSignature))
                .build(), PageRequestImpl.ofSize(1))
            .flatMap(result -> Mono.justOrEmpty(ListResult.first(result)));
    }

    private Mono<LocalThumbnail> fetchByImageHashAndSize(String imageSignature,
        ThumbnailSize size) {
        var indexValue = LocalThumbnail.uniqueImageAndSize(imageSignature, size);
        return client.listBy(LocalThumbnail.class, ListOptions.builder()
                .fieldQuery(equal(LocalThumbnail.UNIQUE_IMAGE_AND_SIZE_INDEX, indexValue))
                .build(), PageRequestImpl.ofSize(ThumbnailSize.values().length)
            )
            .flatMapMany(result -> Flux.fromIterable(result.getItems()))
            .next();
    }

    @Override
    public Mono<Resource> generate(LocalThumbnail thumbnail) {
        Assert.notNull(thumbnail, "Thumbnail must not be null.");
        var filePath = toFilePath(thumbnail.getSpec().getFilePath());
        if (Files.exists(filePath)) {
            return getResourceMono(() -> new FileSystemResource(filePath));
        }
        return updateWithRetry(thumbnail,
            record -> nullSafeAnnotations(record)
                .put(LocalThumbnail.REQUEST_TO_GENERATE_ANNO, "true"))
            .then(Mono.empty());
    }

    private static Mono<Resource> getResourceMono(Callable<Resource> callable) {
        return Mono.fromCallable(callable)
            .subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<Void> updateWithRetry(LocalThumbnail localThumbnail, Consumer<LocalThumbnail> op) {
        op.accept(localThumbnail);
        return client.update(localThumbnail)
            .onErrorResume(OptimisticLockingFailureException.class,
                e -> client.fetch(LocalThumbnail.class, localThumbnail.getMetadata().getName())
                    .flatMap(latest -> {
                        op.accept(latest);
                        return client.update(latest);
                    })
                    .retryWhen(Retry.backoff(5, Duration.ofMillis(100))
                        .filter(OptimisticLockingFailureException.class::isInstance))
            )
            .then();
    }

    @Override
    public Mono<LocalThumbnail> create(URL imageUrl, ThumbnailSize size) {
        Assert.notNull(imageUrl, "Image URL must not be null.");
        Assert.notNull(size, "Thumbnail size must not be null.");
        var imageUri = URI.create(imageUrl.toString());
        var imageHash = signatureForImageUri(imageUri);
        return fetchByImageHashAndSize(imageHash, size)
            .switchIfEmpty(Mono.defer(() -> doCreate(imageUri, size)));
    }

    private Mono<LocalThumbnail> doCreate(URI imageUri, ThumbnailSize size) {
        var year = getYear();
        var originalFileName = geImageFileName(imageUri);
        return generateUniqueThumbFileName(originalFileName, year, size)
            .flatMap(thumbFileName -> {
                var filePath =
                    buildThumbnailStorePath(attachmentDirGetter.get(), thumbFileName, year, size);
                var thumbnail = new LocalThumbnail();
                thumbnail.setMetadata(new Metadata());
                thumbnail.getMetadata().setGenerateName("thumbnail-");
                var thumbnailUri = buildThumbnailUri(year, size, thumbFileName);
                var thumbSignature = ThumbnailSigner.generateSignature(thumbnailUri);
                thumbnail.setSpec(new LocalThumbnail.Spec()
                    .setImageSignature(signatureForImageUri(imageUri))
                    .setFilePath(toRelativeUnixPath(filePath))
                    .setImageUri(ensureInSiteUriIsRelative(imageUri).toASCIIString())
                    .setSize(size)
                    .setThumbSignature(thumbSignature)
                    .setThumbnailUri(thumbnailUri.toASCIIString()));
                return client.create(thumbnail);
            });
    }

    @Override
    public Mono<Void> delete(URI imageUri) {
        var signature = signatureForImageUri(imageUri);
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

    @Override
    @NonNull
    public URI ensureInSiteUriIsRelative(URI imageUri) {
        Assert.notNull(imageUri, "Image URI must not be null.");
        var externalUrl = externalUrlSupplier.getRaw();
        if (externalUrl == null || !isSameOrigin(imageUri, externalUrl)) {
            return imageUri;
        }
        var uriStr = imageUri.toString().replaceFirst("^\\w+://", "");
        uriStr = StringUtils.removeStart(uriStr, imageUri.getAuthority());
        return URI.create(uriStr);
    }

    Mono<String> generateUniqueThumbFileName(String originalFileName, String year,
        ThumbnailSize size) {
        Assert.notNull(originalFileName, "Original file name must not be null.");
        return generateUniqueThumbFileName(originalFileName, originalFileName, year, size);
    }

    private Mono<String> generateUniqueThumbFileName(String originalFileName, String tryFileName,
        String year, ThumbnailSize size) {
        var thumbnailUri = buildThumbnailUri(year, size, tryFileName);
        return fetchThumbnail(thumbnailUri)
            .flatMap(thumbnail -> {
                // use the original file name to generate a new file name
                var newTryFileName = appendRandomSuffix(originalFileName);
                return generateUniqueThumbFileName(originalFileName, newTryFileName, year, size);
            })
            .switchIfEmpty(Mono.just(tryFileName));
    }

    @Override
    public Path toFilePath(String relativeUnixPath) {
        Assert.notNull(relativeUnixPath, "Relative path must not be null.");
        var systemPath = removeStart(relativeUnixPath, "/")
            .replace("/", FileSystems.getDefault().getSeparator());
        return attachmentDirGetter.get().resolve(systemPath);
    }

    @Override
    public URI buildThumbnailUri(String year, ThumbnailSize size, String filename) {
        return URI.create("/upload/thumbnails/%s/w%s/%s".formatted(year, size.getWidth(),
            filename));
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
     * Generate signature for the given image URI.
     * <p>if externalUrl is not configured, it will return the signature generated by the image URI
     * directly, otherwise, it will return the signature generated by the relative path of the
     * image URL to the external URL.</p>
     */
    String signatureForImageUri(URI imageUri) {
        var uriToSign = ensureInSiteUriIsRelative(imageUri);
        return ThumbnailSigner.generateSignature(uriToSign);
    }

    private boolean isSameOrigin(URI imageUri, URL externalUrl) {
        return StringUtils.equals(imageUri.getHost(), externalUrl.getHost())
            && imageUri.getPort() == externalUrl.getPort();
    }

    static String appendRandomSuffix(String fileName) {
        var baseName = StringUtils.substringBeforeLast(fileName, ".");
        var extension = substringAfterLast(fileName, ".");
        var randomSuffix = randomAlphanumeric(6);
        return String.format("%s_%s.%s", baseName, randomSuffix, extension);
    }
}
