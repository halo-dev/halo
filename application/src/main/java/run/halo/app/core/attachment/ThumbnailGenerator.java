package run.halo.app.core.attachment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Optional;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.imgscalr.Scalr;

@Slf4j
@AllArgsConstructor
public class ThumbnailGenerator {
    /**
     * Max file size in bytes for downloading.
     * 30MB
     */
    static final int MAX_FILE_SIZE = 30 * 1024 * 1024;

    private final ImageDownloader imageDownloader = new ImageDownloader();
    private final ThumbnailSize size;
    private final Path storePath;

    /**
     * Generate thumbnail and save it to store path.
     */
    public void generate(URL imageUrl) {
        Path tempImagePath = null;
        try {
            tempImagePath = imageDownloader.downloadFile(imageUrl);
            generateThumbnail(tempImagePath);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            if (tempImagePath != null) {
                try {
                    Files.deleteIfExists(tempImagePath);
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    /**
     * Generate thumbnail by image file path.
     */
    public void generate(Path imageFilePath) {
        try {
            generateThumbnail(imageFilePath);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void generateThumbnail(Path tempImagePath) throws IOException {
        var file = tempImagePath.toFile();
        if (file.length() > MAX_FILE_SIZE) {
            throw new IOException("File size exceeds the limit: " + MAX_FILE_SIZE);
        }
        var formatNameOpt = getFormatName(file);
        var img = ImageIO.read(file);
        if (img == null) {
            throw new UnsupportedOperationException(
                "Unsupported image format for: " + formatNameOpt.orElse("unknown"));
        }
        var thumbnail = Scalr.resize(img, Scalr.Method.AUTOMATIC, Scalr.Mode.FIT_TO_WIDTH,
            size.getWidth());
        var formatName = formatNameOpt.orElse("jpg");
        var thumbnailFile = getThumbnailFile(formatName);
        ImageIO.write(thumbnail, formatName, thumbnailFile);
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

    private static Optional<String> getFormatName(File file) {
        try {
            return Optional.of(doGetFormatName(file));
        } catch (IOException e) {
            // Ignore
        }
        return Optional.empty();
    }

    private static String doGetFormatName(File file) throws IOException {
        try (ImageInputStream imageStream = ImageIO.createImageInputStream(file)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageStream);
            if (!readers.hasNext()) {
                throw new IOException("No ImageReader found for the image.");
            }
            ImageReader reader = readers.next();
            return reader.getFormatName().toLowerCase();
        }
    }

    static class ImageDownloader {
        public Path downloadFile(URL url) throws IOException {
            return downloadFileInternal(encodedUrl(url));
        }

        private static URL encodedUrl(URL url) {
            try {
                return new URL(url.toURI().toASCIIString());
            } catch (MalformedURLException | URISyntaxException e) {
                throw new IllegalArgumentException(e);
            }
        }

        Path downloadFileInternal(URL url) throws IOException {
            File tempFile = File.createTempFile("halo-image-thumb-", ".tmp");
            long totalBytesDownloaded = 0;
            var tempFilePath = tempFile.toPath();
            try (InputStream inputStream = url.openStream();
                 FileOutputStream outputStream = new FileOutputStream(tempFile)) {

                byte[] buffer = new byte[4096];
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    totalBytesDownloaded += bytesRead;

                    if (totalBytesDownloaded > MAX_FILE_SIZE) {
                        outputStream.close();
                        Files.deleteIfExists(tempFilePath);
                        throw new IOException("File size exceeds the limit: " + MAX_FILE_SIZE);
                    }

                    outputStream.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                Files.deleteIfExists(tempFilePath);
                throw e;
            }
            return tempFile.toPath();
        }
    }
}
