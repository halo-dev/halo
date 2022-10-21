package run.halo.app.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import run.halo.app.exception.ForbiddenException;

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

        Files.walkFileTree(source, new SimpleFileVisitor<>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException {
                Path current = target.resolve(source.relativize(dir).toString());
                Files.createDirectories(current);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException {
                Files.copy(file, target.resolve(source.relativize(file).toString()),
                    StandardCopyOption.REPLACE_EXISTING);
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
     * Renames file or folder.
     *
     * @param pathToRename file path to rename must not be null
     * @param newName new name must not be null
     */
    public static void rename(@NonNull Path pathToRename, @NonNull String newName)
        throws IOException {
        Assert.notNull(pathToRename, "File path to rename must not be null");
        Assert.notNull(newName, "New name must not be null");

        Path newPath = pathToRename.resolveSibling(newName);
        var parent = pathToRename.getParent();
        if (parent == null) {
            parent = pathToRename;
        }
        checkDirectoryTraversal(parent, newPath);

        log.info("Rename [{}] to [{}]", pathToRename, newPath);

        Files.move(pathToRename, newPath);

        log.info("Rename [{}] successfully", pathToRename);
    }

    /**
     * Unzips content to the target path.
     *
     * @param zis zip input stream must not be null
     * @param targetPath target path must not be null and not empty
     * @throws IOException throws when failed to access file to be unzipped
     */
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
            checkDirectoryTraversal(targetPath, entryPath);

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
     * @param bytes zip bytes array must not be null
     * @param targetPath target path must not be null and not empty
     * @throws IOException io exception
     */
    public static void unzip(@NonNull byte[] bytes, @NonNull Path targetPath) throws IOException {
        Assert.notNull(bytes, "Zip bytes must not be null");

        ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(bytes));
        unzip(zis, targetPath);
    }

    /**
     * Zips folder or file.
     *
     * @param pathToZip file path to zip must not be null
     * @param pathOfArchive zip file path to archive must not be null
     * @throws IOException throws when failed to access file to be zipped
     */
    public static void zip(@NonNull Path pathToZip, @NonNull Path pathOfArchive)
        throws IOException {
        try (OutputStream outputStream = Files.newOutputStream(pathOfArchive)) {
            try (ZipOutputStream zipOut = new ZipOutputStream(outputStream)) {
                zip(pathToZip, zipOut);
            }
        }
    }

    /**
     * Zips folder or file with filter.
     *
     * @param pathToZip file path to zip must not be null
     * @param pathOfArchive zip file path to archive must not be null
     * @param filter folder or file filter
     * @throws IOException throws when failed to access file to be zipped
     */
    public static void zip(@NonNull Path pathToZip, @NonNull Path pathOfArchive,
        @Nullable Predicate<Path> filter) throws IOException {
        try (OutputStream outputStream = Files.newOutputStream(pathOfArchive)) {
            try (ZipOutputStream zipOut = new ZipOutputStream(outputStream)) {
                zip(pathToZip, zipOut, filter);
            }
        }
    }

    /**
     * Zips folder or file.
     *
     * @param pathToZip file path to zip must not be null
     * @param zipOut zip output stream must not be null
     * @throws IOException throws when failed to access file to be zipped
     */
    public static void zip(@NonNull Path pathToZip, @NonNull ZipOutputStream zipOut)
        throws IOException {
        // Zip file
        zip(pathToZip, pathToZip.getFileName().toString(), zipOut);
    }

    /**
     * Zips folder or file with filter.
     *
     * @param pathToZip file path to zip must not be null
     * @param zipOut zip output stream must not be null
     * @param filter directory or file filter
     * @throws IOException throws when failed to access file to be zipped
     */
    public static void zip(@NonNull Path pathToZip, @NonNull ZipOutputStream zipOut,
        Predicate<Path> filter) throws IOException {
        // Zip file
        zip(pathToZip, pathToZip.getFileName().toString(), zipOut, filter);
    }

    /**
     * Zips folder or file.
     *
     * @param fileToZip file path to zip must not be null
     * @param fileName file name must not be blank
     * @param zipOut zip output stream must not be null
     * @throws IOException throws when failed to access file to be zipped
     */
    private static void zip(@NonNull Path fileToZip, @NonNull String fileName,
        @NonNull ZipOutputStream zipOut) throws IOException {
        zip(fileToZip, fileName, zipOut, null);
    }

    /**
     * Zips folder or file with path filter.
     *
     * @param fileToZip file path to zip must not be null
     * @param fileName file name must not be blank
     * @param zipOut zip output stream must not be null
     * @param filter directory or file filter
     * @throws IOException throws when failed to access file to be zipped
     */
    private static void zip(@NonNull Path fileToZip, @NonNull String fileName,
        @NonNull ZipOutputStream zipOut, @Nullable Predicate<Path> filter) throws IOException {
        if (Files.isDirectory(fileToZip)) {
            log.debug("Try to zip folder: [{}]", fileToZip);
            // Append with '/' if missing
            String folderName =
                StringUtils.appendIfMissing(fileName, File.separator, File.separator);
            // Create zip entry and put into zip output stream
            zipOut.putNextEntry(new ZipEntry(folderName));
            // Close entry for writing the next entry
            zipOut.closeEntry();

            // Iterate the sub files recursively
            try (Stream<Path> subPathStream = Files.list(fileToZip)) {
                // There should not use foreach for stream as internal zip method will throw
                // IOException
                List<Path> subFiles =
                    filter != null ? subPathStream.filter(filter).collect(Collectors.toList())
                        : subPathStream.collect(Collectors.toList());
                for (Path subFileToZip : subFiles) {
                    // Zip children
                    zip(subFileToZip, folderName + subFileToZip.getFileName(), zipOut, filter);
                }
            }
        } else {
            // Open file to be zipped
            // Create zip entry for target file
            ZipEntry zipEntry = new ZipEntry(fileName);
            // Put the entry into zip output stream
            zipOut.putNextEntry(zipEntry);
            // Copy file to zip output stream
            Files.copy(fileToZip, zipOut);
            // Close entry
            zipOut.closeEntry();
        }
    }


    /**
     * Find root path.
     *
     * @param path super root path starter
     * @param pathPredicate path predicate
     * @return empty if path is not a directory or the given path predicate is null
     * @throws IOException IO exception
     */
    @NonNull
    public static Optional<Path> findRootPath(@NonNull final Path path,
        @Nullable final Predicate<Path> pathPredicate)
        throws IOException {
        return findRootPath(path, Integer.MAX_VALUE, pathPredicate);
    }

    /**
     * Find root path.
     *
     * @param path super root path starter
     * @param maxDepth max loop depth
     * @param pathPredicate path predicate
     * @return empty if path is not a directory or the given path predicate is null
     * @throws IOException IO exception
     */
    @NonNull
    public static Optional<Path> findRootPath(@NonNull final Path path,
        int maxDepth,
        @Nullable final Predicate<Path> pathPredicate)
        throws IOException {
        return findPath(path, maxDepth, pathPredicate).map(Path::getParent);
    }

    /**
     * Find path.
     *
     * @param path super root path starter
     * @param pathPredicate path predicate
     * @return empty if path is not a directory or the given path predicate is null
     * @throws IOException IO exception
     */
    @NonNull
    public static Optional<Path> findPath(@NonNull final Path path,
        @Nullable final Predicate<Path> pathPredicate)
        throws IOException {
        return findPath(path, Integer.MAX_VALUE, pathPredicate);
    }

    /**
     * Find path.
     *
     * @param path super root path starter
     * @param pathPredicate path predicate
     * @return empty if path is not a directory or the given path predicate is null
     * @throws IOException IO exception
     */
    @NonNull
    public static Optional<Path> findPath(@NonNull final Path path,
        int maxDepth,
        @Nullable final Predicate<Path> pathPredicate)
        throws IOException {
        Assert.isTrue(maxDepth > 0, "Max depth must not be less than 1");
        if (!Files.isDirectory(path) || pathPredicate == null) {
            // if the path is not a directory or the given path predicate is null, then return an
            // empty optional
            return Optional.empty();
        }

        log.debug("Trying to find path from [{}]", path);

        // the queue holds folders which may be root
        final var queue = new LinkedList<Path>();
        // depth container
        final var depthQueue = new LinkedList<Integer>();

        // init queue
        queue.push(path);
        depthQueue.push(1);

        boolean found = false;
        Path result = null;
        while (!found && !queue.isEmpty()) {
            // pop the first path as candidate root path
            final var rootPath = queue.pop();
            final int depth = depthQueue.pop();
            if (log.isDebugEnabled()) {
                log.debug("Peek({}) into {}", depth, rootPath);
            }
            try (final Stream<Path> childrenPaths = Files.list(rootPath)) {
                final var subFolders = new LinkedList<Path>();
                var resultPath = childrenPaths
                    .peek(p -> {
                        if (Files.isDirectory(p)) {
                            subFolders.add(p);
                        }
                    })
                    .filter(pathPredicate)
                    .findFirst();
                if (resultPath.isPresent()) {
                    queue.clear();
                    depthQueue.clear();
                    // return current result path
                    found = true;
                    result = resultPath.get();
                } else {
                    // put all directory into queue
                    if (depth < maxDepth) {
                        for (Path subFolder : subFolders) {
                            if (!Files.isHidden(subFolder)) {
                                // skip hidden folder
                                queue.push(subFolder);
                                depthQueue.push(depth + 1);
                            }
                        }
                    }
                }
                subFolders.clear();
            }
        }

        log.debug("Found path: [{}]", result);
        return Optional.ofNullable(result);
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
            return pathStream.count() == 0;
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

        throw new ForbiddenException("你没有权限访问 " + pathToCheck).setErrorData(pathToCheck);
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
            log.warn("Failed to delete {}", deletingPath);
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
        final var tempDirectory = Files.createTempDirectory("halo");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> deleteFolderQuietly(tempDirectory)));
        return tempDirectory;
    }

    /**
     * Convert an InputStream to a String.
     *
     * @param inputStream input stream
     * @return the string content read through the input stream without closing the InputStream
     */
    public static String readString(InputStream inputStream) {
        return new BufferedReader(
            new InputStreamReader(inputStream, StandardCharsets.UTF_8))
            .lines()
            .collect(Collectors.joining("\n"));
    }

    /**
     * Writes a String to a file creating the file if it does not exist using the UTF_8 encoding.
     * NOTE: the parent directories of the file will be created if they do not exist.
     *
     * @param file the file to write
     * @param content the content to write to the file
     * @throws IOException in case of an I/O error
     */
    public static void writeStringToFile(File file, String content) throws IOException {
        org.apache.commons.io.FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
    }
}
