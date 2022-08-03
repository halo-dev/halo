package run.halo.app.infra.utils;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

/**
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
public abstract class FileUtils {

    public static void unzip(@NonNull ZipInputStream zis, @NonNull Path targetPath)
        throws IOException {
        // 1. unzip file to folder
        // 2. return the folder path
        Assert.notNull(zis, "Zip input stream must not be null");
        Assert.notNull(targetPath, "Target path must not be null");

        // Create path if absent
        createIfAbsent(targetPath);

        // Folder must be empty
        ensureEmpty(targetPath);

        ZipEntry zipEntry = zis.getNextEntry();

        while (zipEntry != null) {
            // Resolve the entry path
            Path entryPath = targetPath.resolve(zipEntry.getName());

            // Check directory
            if (targetPath.normalize().startsWith(entryPath)) {
                throw new IllegalArgumentException("Cannot unzip to a subdirectory of itself");
            }

            if (zipEntry.isDirectory()) {
                // Create directories
                Files.createDirectories(entryPath);
            } else {
                // Copy file
                Files.copy(zis, entryPath);
            }

            zipEntry = zis.getNextEntry();
        }
    }

    /**
     * Creates directories if absent.
     *
     * @param path path must not be null
     * @throws IOException io exception
     */
    public static void createIfAbsent(@NonNull Path path) throws IOException {
        Assert.notNull(path, "Path must not be null");

        if (Files.notExists(path)) {
            // Create directories
            Files.createDirectories(path);

            log.debug("Created directory: [{}]", path);
        }
    }

    /**
     * The given path must be empty.
     *
     * @param path path must not be null
     * @throws IOException io exception
     */
    public static void ensureEmpty(@NonNull Path path) throws IOException {
        if (!isEmpty(path)) {
            throw new DirectoryNotEmptyException("Target directory: " + path + " was not empty");
        }
    }

    /**
     * Checks if the given path is empty.
     *
     * @param path path must not be null
     * @return true if the given path is empty; false otherwise
     * @throws IOException io exception
     */
    public static boolean isEmpty(@NonNull Path path) throws IOException {
        Assert.notNull(path, "Path must not be null");

        if (!Files.isDirectory(path) || Files.notExists(path)) {
            return true;
        }

        try (Stream<Path> pathStream = Files.list(path)) {
            return pathStream.findAny().isEmpty();
        }
    }

    public static void closeQuietly(final Closeable closeable) {
        closeQuietly(closeable, null);
    }

    /**
     * Closes the given {@link Closeable} as a null-safe operation while consuming IOException by
     * the given {@code consumer}.
     *
     * @param closeable The resource to close, may be null.
     * @param consumer Consumes the IOException thrown by {@link Closeable#close()}.
     */
    public static void closeQuietly(final Closeable closeable,
        final Consumer<IOException> consumer) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                if (consumer != null) {
                    consumer.accept(e);
                }
            }
        }
    }
}
