package run.halo.app.core.attachment.endpoint;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.READ;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.queryParam;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.ThumbnailParameter;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import reactor.core.publisher.Mono;
import run.halo.app.core.attachment.AttachmentRootGetter;
import run.halo.app.core.attachment.ThumbnailSize;
import run.halo.app.core.attachment.ThumbnailUtils;

@Slf4j
@Component
class ThumbnailRouters {

    private static final MediaType IMAGE_MEDIA_TYPE = MediaType.parseMediaType("image/*");

    private static final int DEFAULT_GENERATION_TIMEOUT_SECONDS = 60;

    private static final int DEFAULT_GENERATION_CONCURRENT_THREADS = 5;

    private final Path uploadRoot;

    private final Path thumbnailRoot;

    private final Executor executor;

    /**
     * Map to track in-progress thumbnail generation tasks. The key is the filename, and the value
     * is a CompletableFuture representing the generation task.
     */
    private final ConcurrentMap<String, CompletableFuture<Path>> inProgress;

    private final boolean useLastModified;

    private final CacheControl cacheControl;

    public ThumbnailRouters(AttachmentRootGetter attachmentRoot,
        WebProperties webProperties) {
        this.uploadRoot = attachmentRoot.get().resolve("upload");
        this.thumbnailRoot = attachmentRoot.get().resolve("thumbnails");
        this.executor = Executors.newFixedThreadPool(DEFAULT_GENERATION_CONCURRENT_THREADS,
            Thread.ofPlatform()
                .daemon()
                .name("thumbnail-generator-", 0)
                .factory());
        this.inProgress = new ConcurrentHashMap<>();

        var resources = webProperties.getResources();
        this.useLastModified = resources.getCache().isUseLastModified();
        this.cacheControl = resources.getCache().getCachecontrol().toHttpCacheControl();
    }

    @Bean
    RouterFunction<ServerResponse> thumbnailRouter() {
        return RouterFunctions.route()
            .GET(
                "/upload/{*filename}",
                queryParam("width", StringUtils::isNotBlank).and(accept(IMAGE_MEDIA_TYPE)),
                serverRequest -> {
                    var size = serverRequest.queryParam("width")
                        .map(ThumbnailSize::fromWidth)
                        .orElse(ThumbnailSize.M);
                    String originalFilename = serverRequest.pathVariable("filename");
                    var filename = StringUtils.removeStart(originalFilename, "/");
                    if (StringUtils.isBlank(filename)) {
                        log.trace("Filename is blank");
                        return Mono.error(new NoResourceFoundException(filename));
                    }
                    return Mono.defer(
                            () -> {
                                // try to resolve the thumbnail
                                // build thumbnail path
                                var thumbnailPath = thumbnailRoot.resolve("w" + size.getWidth())
                                    .resolve(filename);
                                var thumbnailResource = new FileSystemResource(thumbnailPath);
                                if (thumbnailResource.isReadable()) {
                                    return Mono.just(thumbnailResource);
                                }

                                var attachmentPath = uploadRoot.resolve(filename);
                                var fileSuffix = FilenameUtils.getExtension(filename);
                                if (!ThumbnailUtils.isSupportedImage(fileSuffix)) {
                                    log.warn(
                                        "File suffix {} is not supported for thumbnail generation, "
                                            + "return "
                                            + "original file", fileSuffix);
                                    // return the original file
                                    thumbnailResource = new FileSystemResource(attachmentPath);
                                    if (!thumbnailResource.isReadable()) {
                                        return Mono.empty();
                                    }
                                    return Mono.just(thumbnailResource);
                                }
                                return generateThumbnail(
                                    filename, attachmentPath, thumbnailPath, size
                                );

                            })
                        .switchIfEmpty(Mono.error(() -> new NoResourceFoundException(filename)))
                        .flatMap(resource -> {
                            Instant lastModified;
                            try {
                                lastModified = useLastModified
                                    ? Instant.ofEpochMilli(resource.lastModified()) : Instant.MIN;
                            } catch (IOException e) {
                                return Mono.error(e);
                            }
                            return serverRequest.checkNotModified(lastModified)
                                .switchIfEmpty(Mono.defer(() -> {
                                    var builder = ServerResponse.ok();
                                    if (this.cacheControl != null) {
                                        builder.cacheControl(this.cacheControl);
                                    }
                                    return builder.bodyValue(resource);
                                }));
                        });
                })
            .build();
    }

    private Mono<Resource> generateThumbnail(
        String filename, Path attachmentPath, Path thumbnailPath, ThumbnailSize size
    ) {
        return Mono.fromFuture(() -> inProgress.computeIfAbsent(filename,
                    f ->
                        CompletableFuture.supplyAsync(() -> generateThumbnail(
                                    attachmentPath, thumbnailPath, size
                                ),
                                this.executor
                            )
                            .orTimeout(
                                DEFAULT_GENERATION_TIMEOUT_SECONDS,
                                TimeUnit.SECONDS
                            )
                            .whenComplete((p, t) -> inProgress.remove(filename))
                ),
                // We don't want to cancel the thumbnail generation task
                // when some requests are cancelled
                true
            )
            .map(FileSystemResource::new);
    }

    private Path generateThumbnail(Path attachmentPath, Path thumbnailPath, ThumbnailSize size) {
        if (!Files.exists(attachmentPath)) {
            log.trace("Attachment path does not exist: {}", attachmentPath);
            return null;
        }
        if (Files.exists(thumbnailPath)) {
            return thumbnailPath;
        }
        if (log.isDebugEnabled()) {
            log.debug("Generating thumbnail for path: {}, target: {}, size: {}",
                attachmentPath, thumbnailPath, size);
        }
        try (
            var inputStream = Files.newInputStream(attachmentPath, READ);
        ) {
            Files.createDirectories(thumbnailPath.getParent());
            // Pass InputStream or File here.
            // See https://github.com/coobird/thumbnailator/issues/159#issuecomment-694978197
            // for more.
            Thumbnails.of(inputStream)
                .width(size.getWidth())
                .imageType(ThumbnailParameter.DEFAULT_IMAGE_TYPE)
                .useExifOrientation(true)
                .toFile(thumbnailPath.toFile());
            log.info("Generated thumbnail for path: {}, target: {}, size: {}",
                attachmentPath, thumbnailPath, size);

            // check size of thumbnails
            var attachmentFileSize = Files.size(attachmentPath);
            var thumbnailFileSize = Files.size(thumbnailPath);
            if (attachmentFileSize < thumbnailFileSize) {
                Files.copy(attachmentPath, thumbnailPath, REPLACE_EXISTING);
                log.info(
                    """
                        Replaced thumbnail with original file since it's smaller, \
                        path: {}, size: {} < {}\
                        """,
                    thumbnailPath, attachmentFileSize, thumbnailFileSize);
            }
            return thumbnailPath;
        } catch (IOException e) {
            log.warn("Failed to generate thumbnail for path: {}", attachmentPath, e);
            // delete the possibly created file
            try {
                Files.deleteIfExists(thumbnailPath);
            } catch (IOException ex) {
                // ignore this error
                log.warn("Failed to delete possibly created thumbnail file: {}",
                    thumbnailPath, ex);
            }
            // return the original attachment path
            return attachmentPath;
        }
    }

}
