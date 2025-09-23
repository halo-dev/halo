package run.halo.app.core.attachment.endpoint;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.imgscalr.Scalr.Method.AUTOMATIC;
import static org.imgscalr.Scalr.Mode.FIT_TO_WIDTH;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.imgscalr.Scalr;
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
                var fileName = StringUtils.removeStart(originalFilename, "/");
                if (StringUtils.isBlank(fileName)) {
                    log.trace("Filename is blank");
                    return Mono.error(new NoResourceFoundException(fileName));
                }
                var size = ThumbnailSize.fromWidth(width);
                // try to resolve the thumbnail
                // build thumbnail path
                var thumbnailPath = thumbnailRoot.resolve("w" + size.getWidth())
                    .resolve(fileName);
                var thumbnailResource = new FileSystemResource(thumbnailPath);
                if (thumbnailResource.isReadable()) {
                    return ServerResponse.ok().bodyValue(thumbnailResource);
                }
                // generate for the attachment
                return Mono.fromCallable(
                        () -> {
                            var uploadPath = uploadRoot.resolve(fileName);
                            return generateThumbnail(uploadPath, thumbnailPath, size);
                        })
                    .subscribeOn(this.thumbnailGeneratingScheduler)
                    .switchIfEmpty(Mono.error(() -> new NoResourceFoundException(fileName)))
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
            var formatName = getFormatName(attachmentPath);
            if (formatName == null) {
                log.info("Cannot determine image format for path: {}", attachmentPath);
                return null;
            }
            try (
                var inputStream = Files.newInputStream(attachmentPath, READ);
            ) {
                var bufferedImage = ImageIO.read(inputStream);
                if (bufferedImage == null) {
                    return null;
                }
                if (bufferedImage.getWidth() <= size.getWidth()) {
                    // if the image is smaller than the thumbnail size, just copy it
                    Files.createDirectories(thumbnailPath.getParent());
                    Files.copy(attachmentPath, thumbnailPath);
                    return thumbnailPath;
                }
                // TODO Handle image orientation
                var thumbnailBufferedImage =
                    Scalr.resize(bufferedImage, AUTOMATIC, FIT_TO_WIDTH, size.getWidth());
                Files.createDirectories(thumbnailPath.getParent());
                try (var outputStream =
                         Files.newOutputStream(thumbnailPath, CREATE, TRUNCATE_EXISTING, WRITE)
                ) {
                    ImageIO.write(thumbnailBufferedImage, formatName, outputStream);
                }
                log.info("Generated thumbnail for path: {}, target: {}, size: {}",
                    attachmentPath, thumbnailPath, size);
                return thumbnailPath;
            } catch (IOException e) {
                log.warn("Failed to generate thumbnail for path: {}", attachmentPath, e);
                return null;
            }
        }
    }

    private String getFormatName(Path imagePath) {
        try (var imageInputStream = ImageIO.createImageInputStream(
            Files.newInputStream(imagePath))
        ) {
            var readers = ImageIO.getImageReaders(imageInputStream);
            if (!readers.hasNext()) {
                return null;
            }
            var reader = readers.next();
            return reader.getFormatName();
        } catch (IOException e) {
            log.warn("Failed to get image format for path: {}", imagePath, e);
            return null;
        }
    }

}
