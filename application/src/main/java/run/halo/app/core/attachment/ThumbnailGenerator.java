package run.halo.app.core.attachment;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Optional;
import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import lombok.AllArgsConstructor;
import org.imgscalr.Scalr;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@AllArgsConstructor
public class ThumbnailGenerator {
    private final URL imageUrl;
    private final ThumbnailSize size;
    private final Path storePath;

    /**
     * Generate thumbnail and save it to store path.
     */
    public Mono<Void> generate() {
        return Mono.fromRunnable(
                () -> {
                    String formatName = getFormatName(imageUrl);
                    try {
                        var img = ImageIO.read(imageUrl);
                        BufferedImage thumbnail = Scalr.resize(img, size.getWidth());
                        var thumbnailFile = getThumbnailFile(formatName);
                        ImageIO.write(thumbnail, formatName, thumbnailFile);
                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                })
            .subscribeOn(Schedulers.boundedElastic())
            .then();
    }

    private File getThumbnailFile(String formatName) {
        return Optional.of(storePath)
            .map(path -> {
                if (storePath.endsWith(formatName)) {
                    return storePath.resolve("." + formatName);
                }
                return storePath;
            })
            .map(path -> {
                if (!Files.exists(path.getParent())) {
                    try {
                        Files.createDirectories(path.getParent());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                return path;
            })
            .map(Path::toFile)
            .orElseThrow();
    }

    /**
     * Sanitize file name.
     *
     * @param fileName file name to sanitize
     */
    public static String sanitizeFileName(String fileName) {
        String sanitizedFileName = fileName.replaceAll("[^a-zA-Z0-9\\.\\-]", "");

        if (sanitizedFileName.length() > 255) {
            sanitizedFileName = sanitizedFileName.substring(0, 255);
        }

        return sanitizedFileName;
    }

    private static String getFormatName(URL input) {
        try {
            return doGetFormatName(input);
        } catch (IOException e) {
            return "jpg";
        }
    }

    private static String doGetFormatName(URL input) throws IOException {
        try (var inputStream = input.openStream();
             ImageInputStream imageStream = ImageIO.createImageInputStream(inputStream)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageStream);
            if (!readers.hasNext()) {
                throw new IOException("No ImageReader found for the image.");
            }
            ImageReader reader = readers.next();
            return reader.getFormatName().toLowerCase();
        } catch (IOException e) {
            throw new IIOException("Can't get input stream from URL!", e);
        }
    }
}
