package run.halo.app.core.attachment.thumbnail;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.READ;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.ThumbnailParameter;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import run.halo.app.core.attachment.AttachmentRootGetter;
import run.halo.app.core.attachment.ThumbnailSize;

/**
 * Default implementation of {@link LocalThumbnailService} that generates thumbnails using
 * Thumbnailator library and deletes them when no longer needed.
 *
 * @author johnniang
 * @since 2.22.0
 */
@Slf4j
@Service
class DefaultLocalThumbnailService implements LocalThumbnailService, DisposableBean {

    private static final String THUMBNAIL_ROOT = "thumbnails";

    private static final int DEFAULT_GENERATION_TIMEOUT_SECONDS = 60;

    private static final int DEFAULT_GENERATION_CONCURRENT_THREADS = 5;

    private ExecutorService executorService;

    private final AttachmentRootGetter attachmentRootGetter;

    /**
     * Map to track in-progress thumbnail generation tasks. The key is the filename, and the
     * value
     * is a CompletableFuture representing the generation task.
     */
    private final ConcurrentMap<Path, CompletableFuture<Path>> inProgress;

    public DefaultLocalThumbnailService(AttachmentRootGetter attachmentRootGetter) {
        this.attachmentRootGetter = attachmentRootGetter;
        this.executorService =
            Executors.newFixedThreadPool(DEFAULT_GENERATION_CONCURRENT_THREADS, Thread.ofPlatform()
                .daemon()
                .name("thumbnail-generator-", 0)
                .factory());
        this.inProgress = new ConcurrentHashMap<>();
    }

    void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public void destroy() throws Exception {
        this.executorService.close();
    }

    @Override
    public Mono<Resource> generate(Path source, ThumbnailSize size) {
        return Mono.fromFuture(() -> inProgress.computeIfAbsent(source, f ->
                        CompletableFuture.supplyAsync(() -> generateThumbnail(
                                    source, size
                                ),
                                this.executorService
                            )
                            .orTimeout(
                                DEFAULT_GENERATION_TIMEOUT_SECONDS,
                                TimeUnit.SECONDS
                            )
                    )
                    .whenComplete((p, t) -> inProgress.remove(source)),
                // We don't want to cancel the thumbnail generation task
                // when some requests are cancelled
                true
            )
            .map(FileSystemResource::new);
    }

    @Override
    public void delete(Path source) {
        Arrays.stream(ThumbnailSize.values()).forEach(size -> {
            var thumbnailPath = resolveThumbnailPath(source, size);
            if (thumbnailPath.isEmpty()) {
                log.warn("Failed to resolve thumbnail path for source: {}, size: {}", source, size);
                return;
            }
            try {
                var deleted = Files.deleteIfExists(thumbnailPath.get());
                if (deleted) {
                    log.info("Deleted thumbnail: {} for {}", thumbnailPath.get(), source);
                }
            } catch (IOException e) {
                // Ignore the error
                log.error("Failed to delete thumbnail: {}", thumbnailPath.get(), e);
            }
        });
    }

    @Override
    public Optional<Path> resolveThumbnailPath(Path source, ThumbnailSize size) {
        var attachmentRoot = this.attachmentRootGetter.get();
        Path relativize;
        try {
            relativize = attachmentRoot.relativize(source);
        } catch (IllegalArgumentException e) {
            // The source path is not under the attachment root
            return Optional.empty();
        }
        var thumbnailPath = attachmentRoot.resolve(THUMBNAIL_ROOT)
            .resolve("w" + size.getWidth())
            .resolve(relativize);
        return Optional.of(thumbnailPath);
    }

    private Path generateThumbnail(Path sourcePath, ThumbnailSize size) {
        if (!Files.exists(sourcePath)) {
            log.trace("Attachment path does not exist: {}", sourcePath);
            return null;
        }
        var optionalThumbnailPath = resolveThumbnailPath(sourcePath, size);
        if (optionalThumbnailPath.isEmpty()) {
            log.warn("Failed to resolve thumbnail path for source: {}, size: {}", sourcePath, size);
            return null;
        }
        var thumbnailPath = optionalThumbnailPath.get();
        if (Files.exists(thumbnailPath)) {
            return thumbnailPath;
        }
        if (log.isDebugEnabled()) {
            log.debug(
                "Generating thumbnail for path: {}, target: {}, size: {}",
                sourcePath, thumbnailPath, size);
        }
        try (var inputStream = Files.newInputStream(sourcePath, READ)) {
            Files.createDirectories(thumbnailPath.getParent());
            // Pass InputStream or File here.
            // See https://github.com/coobird/thumbnailator/issues/159#issuecomment-694978197
            // for more.
            Thumbnails.of(inputStream)
                .width(size.getWidth())
                .imageType(ThumbnailParameter.DEFAULT_IMAGE_TYPE)
                .useExifOrientation(true)
                .toFile(thumbnailPath.toFile());
            log.info(
                "Generated thumbnail for path: {}, target: {}, size: {}",
                sourcePath, thumbnailPath, size);

            // check size of thumbnails
            var attachmentFileSize = Files.size(sourcePath);
            var thumbnailFileSize = Files.size(thumbnailPath);
            if (attachmentFileSize < thumbnailFileSize) {
                Files.copy(sourcePath, thumbnailPath, REPLACE_EXISTING);
                log.info(
                    """
                        Replaced thumbnail with original file since it's smaller, \
                        path: {}, size: {} < {}\
                        """,
                    thumbnailPath, attachmentFileSize, thumbnailFileSize);
            }
            return thumbnailPath;
        } catch (IOException e) {
            log.warn("Failed to generate thumbnail for path: {}",
                sourcePath, e);
            // delete the possibly created file
            try {
                Files.deleteIfExists(thumbnailPath);
            } catch (IOException ex) {
                // ignore this error
                log.warn("Failed to delete possibly created thumbnail file: {}",
                    thumbnailPath, ex);
            }
            // return the original attachment path
            return sourcePath;
        }
    }

}
