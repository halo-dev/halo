package run.halo.app.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import run.halo.app.exception.ForbiddenException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * File utilities.
 *
 * @author johnniang
 * @date 4/9/19
 */
@Slf4j
public class FileUtils {

    private FileUtils() {
    }

    /**
     * Copies folder.
     *
     * @param source source path must not be null
     * @param target target path must not be null
     */
    public static void copyFolder(@NonNull Path source, @NonNull Path target) throws IOException {
        Assert.notNull(source, "Source path must not be null");
        Assert.notNull(target, "Target path must not be null");

        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {

            private Path current;

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                current = target.resolve(source.relativize(dir).toString());
                Files.createDirectories(current);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, target.resolve(source.relativize(file).toString()), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Deletes folder recursively.
     *
     * @param deletingPath deleting path must not be null
     */
    public static void deleteFolder(@NonNull Path deletingPath) throws IOException {
        Assert.notNull(deletingPath, "Deleting path must not be null");

        if (Files.notExists(deletingPath)) {
            return;
        }

        log.info("Deleting [{}]", deletingPath);

        // Delete folder recursively
        org.eclipse.jgit.util.FileUtils.delete(deletingPath.toFile(),
                org.eclipse.jgit.util.FileUtils.RECURSIVE | org.eclipse.jgit.util.FileUtils.RETRY);

        log.info("Deleted [{}] successfully", deletingPath);
    }

    /**
     * Unzips content to the target path.
     *
     * @param zis        zip input stream must not be null
     * @param targetPath target path must not be null and not empty
     * @throws IOException
     */
    public static void unzip(@NonNull ZipInputStream zis, @NonNull Path targetPath) throws IOException {
        Assert.notNull(zis, "Zip input stream must not be null");
        Assert.notNull(targetPath, "Target path must not be null");

        // Create path if absent
        createIfAbsent(targetPath);

        // Must be empty
        mustBeEmpty(targetPath);

        ZipEntry zipEntry = zis.getNextEntry();

        while (zipEntry != null) {
            // Resolve the entry path
            Path entryPath = targetPath.resolve(zipEntry.getName());

            // Check directory
            FileUtils.checkDirectoryTraversal(targetPath, entryPath);

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
     * Unzips content to the target path.
     *
     * @param bytes      zip bytes array must not be null
     * @param targetPath target path must not be null and not empty
     * @throws IOException
     */
    public static void unzip(@NonNull byte[] bytes, @NonNull Path targetPath) throws IOException {
        Assert.notNull(bytes, "Zip bytes must not be null");

        ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(bytes));
        unzip(zis, targetPath);
    }

    /**
     * Try to skip zip parent folder. (Go into base folder)
     *
     * @param unzippedPath unzipped path must not be null
     * @return path containing base files
     * @throws IOException
     */
    public static Path tryToSkipZipParentFolder(@NonNull Path unzippedPath) throws IOException {
        Assert.notNull(unzippedPath, "Unzipped folder must not be  null");

        // TODO May cause a latent problem.
        try (Stream<Path> pathStream = Files.list(unzippedPath)) {
            List<Path> childrenPath = pathStream.collect(Collectors.toList());

            if (childrenPath.size() == 1 && Files.isDirectory(childrenPath.get(0))) {
                return childrenPath.get(0);
            }
            return unzippedPath;
        }
    }

    /**
     * Creates directories if absent.
     *
     * @param path path must not be null
     * @throws IOException
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
     * Checks if the given path is empty.
     *
     * @param path path must not be null
     * @return true if the given path is empty; false otherwise
     * @throws IOException
     */
    public static boolean isEmpty(@NonNull Path path) throws IOException {
        Assert.notNull(path, "Path must not be null");

        if (!Files.isDirectory(path) || Files.notExists(path)) {
            return true;
        }

        try (Stream<Path> pathStream = Files.list(path)) {
            return pathStream.count() == 0;
        }
    }

    /**
     * The given path must be empty.
     *
     * @param path path must not be null
     * @throws IOException
     */
    public static void mustBeEmpty(@NonNull Path path) throws IOException {
        if (!isEmpty(path)) {
            throw new DirectoryNotEmptyException("Target directory: " + path + " was not empty");
        }
    }

    /**
     * Checks directory traversal vulnerability.
     *
     * @param parentPath  parent path must not be null.
     * @param pathToCheck path to check must not be null
     */
    public static void checkDirectoryTraversal(@NonNull String parentPath, @NonNull String pathToCheck) {
        checkDirectoryTraversal(Paths.get(parentPath), Paths.get(pathToCheck));
    }

    /**
     * Checks directory traversal vulnerability.
     *
     * @param parentPath  parent path must not be null.
     * @param pathToCheck path to check must not be null
     */
    public static void checkDirectoryTraversal(@NonNull Path parentPath, @NonNull String pathToCheck) {
        checkDirectoryTraversal(parentPath, Paths.get(pathToCheck));
    }

    /**
     * Checks directory traversal vulnerability.
     *
     * @param parentPath  parent path must not be null.
     * @param pathToCheck path to check must not be null
     */
    public static void checkDirectoryTraversal(@NonNull Path parentPath, @NonNull Path pathToCheck) {
        Assert.notNull(parentPath, "Parent path must not be null");
        Assert.notNull(pathToCheck, "Path to check must not be null");

        if (pathToCheck.startsWith(parentPath.normalize())) {
            return;
        }

        throw new ForbiddenException("You cannot access " + pathToCheck).setErrorData(pathToCheck);
    }

    /**
     * Closes input stream quietly.
     *
     * @param inputStream input stream
     */
    public static void closeQuietly(@Nullable InputStream inputStream) {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            // Ignore this exception
            log.warn("Failed to close input stream", e);
        }
    }

    /**
     * Closes zip input stream quietly.
     *
     * @param zipInputStream zip input stream
     */
    public static void closeQuietly(@Nullable ZipInputStream zipInputStream) {
        try {
            if (zipInputStream != null) {
                zipInputStream.closeEntry();
                zipInputStream.close();
            }
        } catch (IOException e) {
            // Ignore this exception
            log.warn("Failed to close zip input stream", e);
        }
    }

    /**
     * Deletes folder quietly.
     *
     * @param deletingPath deleting path
     */
    public static void deleteFolderQuietly(@Nullable Path deletingPath) {
        try {
            if (deletingPath != null) {
                FileUtils.deleteFolder(deletingPath);
            }
        } catch (IOException e) {
            log.warn("Failed to delete " + deletingPath);
        }
    }


    /**
     * Creates temp directory.
     *
     * @return temp directory path
     * @throws IOException if an I/O error occurs or the temporary-file directory does not exist
     */
    @NonNull
    public static Path createTempDirectory() throws IOException {
        return Files.createTempDirectory("halo");
    }
}
