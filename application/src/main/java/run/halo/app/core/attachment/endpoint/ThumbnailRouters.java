package run.halo.app.core.attachment.endpoint;

import static java.nio.file.StandardOpenOption.READ;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import run.halo.app.core.attachment.AttachmentRootGetter;
import run.halo.app.core.attachment.ThumbnailSize;
import run.halo.app.core.attachment.ThumbnailUtils;

@Slf4j
@Component
class ThumbnailRouters {

    private final Path uploadRoot;

    private final Path thumbnailRoot;

    private final Scheduler thumbnailGeneratingScheduler;

    public ThumbnailRouters(AttachmentRootGetter attachmentRoot) {
        this.uploadRoot = attachmentRoot.get().resolve("upload");
        this.thumbnailRoot = attachmentRoot.get().resolve("thumbnails");
        this.thumbnailGeneratingScheduler = Schedulers.newBoundedElastic(
            // Currently, we only allow 10 concurrent thumbnail generations
            10,
            Schedulers.DEFAULT_BOUNDED_ELASTIC_QUEUESIZE,
            "thumbnail-generator-"
        );
    }

    @Bean
    RouterFunction<ServerResponse> thumbnailRouter() {
        return RouterFunctions.route()
            .GET("/thumbnails/w{width}/upload/{*filename}", serverRequest -> {
                var width = serverRequest.pathVariable("width");
                String originalFilename = serverRequest.pathVariable("filename");
                var filename = StringUtils.removeStart(originalFilename, "/");
                if (StringUtils.isBlank(filename)) {
                    log.trace("Filename is blank");
                    return Mono.error(new NoResourceFoundException(filename));
                }
                var size = ThumbnailSize.fromWidth(width);
                // try to resolve the thumbnail
                // build thumbnail path
                var thumbnailPath = thumbnailRoot.resolve("w" + size.getWidth())
                    .resolve(filename);
                var thumbnailResource = new FileSystemResource(thumbnailPath);
                if (thumbnailResource.isReadable()) {
                    return ServerResponse.ok().bodyValue(thumbnailResource);
                }

                var uploadPath = uploadRoot.resolve(filename);
                var fileSuffix = FilenameUtils.getExtension(filename);
                if (!ThumbnailUtils.isSupportedImage(fileSuffix)) {
                    log.warn("File suffix {} is not supported for thumbnail generation, return "
                        + "original file", fileSuffix);
                    // return the original file
                    thumbnailResource = new FileSystemResource(uploadPath);
                    if (!thumbnailResource.isReadable()) {
                        return Mono.error(new NoResourceFoundException(filename));
                    }
                    return ServerResponse.ok().bodyValue(thumbnailResource);
                }

                // generate for the attachment
                return Mono.fromCallable(
                        () -> generateThumbnail(uploadPath, thumbnailPath, size)
                    )
                    .subscribeOn(this.thumbnailGeneratingScheduler)
                    .switchIfEmpty(Mono.error(() -> new NoResourceFoundException(filename)))
                    .map(FileSystemResource::new)
                    .flatMap(resource -> ServerResponse.ok().bodyValue(resource));
            })
            .build();
    }

    private Path generateThumbnail(Path attachmentPath, Path thumbnailPath, ThumbnailSize size) {
        if (!Files.exists(attachmentPath)) {
            log.trace("Attachment path does not exist: {}", attachmentPath);
            return null;
        }
        if (Files.exists(thumbnailPath)) {
            return thumbnailPath;
        }
        var attachmentPathString = attachmentPath.toString();
        synchronized (attachmentPathString) {
            // double check
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
                var bufferedImage = ImageIO.read(inputStream);
                if (bufferedImage == null) {
                    // indicate that it's not an image or unsupported image format
                    return null;
                }
                if (bufferedImage.getWidth() <= size.getWidth()) {
                    // if the image is smaller than the thumbnail size, just copy it
                    Files.createDirectories(thumbnailPath.getParent());
                    Files.copy(attachmentPath, thumbnailPath);
                    return thumbnailPath;
                }
                Files.createDirectories(thumbnailPath.getParent());
                Thumbnails.of(bufferedImage)
                    .width(size.getWidth())
                    .toFile(thumbnailPath.toFile());
                log.info("Generated thumbnail for path: {}, target: {}, size: {}",
                    attachmentPath, thumbnailPath, size);
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

}
