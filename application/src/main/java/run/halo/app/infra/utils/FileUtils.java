package run.halo.app.infra.utils;

import static org.springframework.util.FileSystemUtils.deleteRecursively;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import run.halo.app.infra.exception.AccessDeniedException;

/**
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
public abstract class FileUtils {

    private FileUtils() {
    }

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

            checkDirectoryTraversal(targetPath, entryPath);

            if (Files.notExists(entryPath.getParent())) {
                Files.createDirectories(entryPath.getParent());
            }

            if (zipEntry.isDirectory()) {
                // Create directory
                Files.createDirectory(entryPath);
            } else {
                // Copy file
                Files.copy(zis, entryPath);
            }

            zipEntry = zis.getNextEntry();
        }
    }

    public static void zip(Path sourcePath, Path targetPath) throws IOException {
        try (var zos = new ZipOutputStream(Files.newOutputStream(targetPath))) {
            Files.walkFileTree(sourcePath, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                    checkDirectoryTraversal(sourcePath, file);
                    var relativePath = sourcePath.relativize(file);
                    var entry = new ZipEntry(relativePath.toString());
                    zos.putNextEntry(entry);
                    Files.copy(file, zos);
                    zos.closeEntry();
                    return super.visitFile(file, attrs);
                }
            });
        }
    }

    public static void jar(Path sourcePath, Path targetPath) throws IOException {
        try (var jos = new JarOutputStream(Files.newOutputStream(targetPath))) {
            Files.walkFileTree(sourcePath, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                    checkDirectoryTraversal(sourcePath, file);
                    var relativePath = sourcePath.relativize(file);
                    var entry = new JarEntry(relativePath.toString());
                    jos.putNextEntry(entry);
                    Files.copy(file, jos);
                    jos.closeEntry();
                    return super.visitFile(file, attrs);
                }
            });
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

    /**
     * Checks directory traversal vulnerability.
     *
     * @param parentPath parent path must not be null.
     * @param pathToCheck path to check must not be null
     */
    public static void checkDirectoryTraversal(@NonNull Path parentPath,
        @NonNull Path pathToCheck) {
        Assert.notNull(parentPath, "Parent path must not be null");
        Assert.notNull(pathToCheck, "Path to check must not be null");

        if (pathToCheck.normalize().startsWith(parentPath)) {
            return;
        }

        throw new AccessDeniedException("Directory traversal detected: " + pathToCheck,
            "problemDetail.directoryTraversal", new Object[] {parentPath, pathToCheck});
    }

    /**
     * Checks directory traversal vulnerability.
     *
     * @param parentPath parent path must not be null.
     * @param pathToCheck path to check must not be null
     */
    public static void checkDirectoryTraversal(@NonNull String parentPath,
        @NonNull String pathToCheck) {
        checkDirectoryTraversal(Paths.get(parentPath), Paths.get(pathToCheck));
    }

    /**
     * Checks directory traversal vulnerability.
     *
     * @param parentPath parent path must not be null.
     * @param pathToCheck path to check must not be null
     */
    public static void checkDirectoryTraversal(@NonNull Path parentPath,
        @NonNull String pathToCheck) {
        checkDirectoryTraversal(parentPath, Paths.get(pathToCheck));
    }

    /**
     * Delete folder recursively without exception throwing.
     *
     * @param root the root File to delete
     */
    public static void deleteRecursivelyAndSilently(Path root) {
        try {
            var deleted = deleteRecursively(root);
            if (log.isDebugEnabled()) {
                log.debug("Delete {} result: {}", root, deleted);
            }
        } catch (IOException e) {
            // Ignore this error
            if (log.isTraceEnabled()) {
                log.trace("Failed to delete {} recursively", root);
            }
        }
    }

    public static void copy(Path source, Path dest, CopyOption... options) {
        try {
            Files.copy(source, dest, options);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
