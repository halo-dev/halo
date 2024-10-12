package run.halo.app.core.attachment;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.imgscalr.Scalr;
import org.springframework.lang.NonNull;

@Slf4j
@AllArgsConstructor
public class ThumbnailGenerator {
    /**
     * Max file size in bytes for downloading.
     * 30MB
     */
    static final int MAX_FILE_SIZE = 30 * 1024 * 1024;

    private static final Set<String> UNSUPPORTED_FORMATS = Set.of("gif", "svg", "webp");

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
        String formatName = getFormatName(file)
            .orElseThrow(() -> new UnsupportedOperationException("Unknown format"));
        if (isUnsupportedFormat(formatName)) {
            throw new UnsupportedOperationException("Unsupported image format for: " + formatName);
        }

        var img = ImageIO.read(file);
        if (img == null) {
            throw new UnsupportedOperationException("Cannot read image file: " + file);
        }
        var thumbnailFile = getThumbnailFile(formatName);
        if (img.getWidth() <= size.getWidth()) {
            Files.copy(tempImagePath, thumbnailFile.toPath(), REPLACE_EXISTING);
            return;
        }
        var thumbnail = Scalr.resize(img, Scalr.Method.AUTOMATIC, Scalr.Mode.FIT_TO_WIDTH,
            size.getWidth());
        // Rotate image if needed
        var orientation = readExifOrientation(file);
        if (orientation != null) {
            thumbnail = Scalr.rotate(thumbnail, orientation);
        }
        ImageIO.write(thumbnail, formatName, thumbnailFile);
    }

    private static Scalr.Rotation readExifOrientation(File inputFile) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(inputFile);
            Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (directory != null && directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
                return getScalrRotationFromExifOrientation(
                    directory.getInt(ExifIFD0Directory.TAG_ORIENTATION));
            }
        } catch (Exception e) {
            log.debug("Failed to read EXIF orientation from file: {}", inputFile, e);
        }
        return null;
    }

    private static Scalr.Rotation getScalrRotationFromExifOrientation(int orientation) {
        // https://www.media.mit.edu/pia/Research/deepview/exif.html#:~:text=0x0112-,Orientation,-unsigned%20short
        return switch (orientation) {
            case 3 -> Scalr.Rotation.CW_180;
            case 6 -> Scalr.Rotation.CW_90;
            case 8 -> Scalr.Rotation.CW_270;
            default -> null;
        };
    }

    private static boolean isUnsupportedFormat(@NonNull String formatName) {
        return UNSUPPORTED_FORMATS.contains(formatName.toLowerCase());
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
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko)"
                    + " Chrome/92.0.4515.131 Safari/537.36");
            try (InputStream inputStream = connection.getInputStream();
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
