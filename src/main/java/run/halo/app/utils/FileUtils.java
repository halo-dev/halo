package run.halo.app.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import run.halo.app.exception.ForbiddenException;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

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

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path current = target.resolve(source.relativize(dir).toString());
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
     * Renames file or folder.
     *
     * @param pathToRename file path to rename must not be null
     * @param newName      new name must not be null
     */
    public static void rename(@NonNull Path pathToRename, @NonNull String newName) throws IOException {
        Assert.notNull(pathToRename, "File path to rename must not be null");
        Assert.notNull(newName, "New name must not be null");

        Path newPath = pathToRename.resolveSibling(newName);
        log.info("Rename [{}] to [{}]", pathToRename, newPath);

        Files.move(pathToRename, newPath);

        log.info("Rename [{}] successfully", pathToRename);
    }

    /**
     * Unzips content to the target path.
     *
     * @param zis        zip input stream must not be null
     * @param targetPath target path must not be null and not empty
     * @throws IOException throws when failed to access file to be unzipped
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
        File targetDir = targetPath.toFile();
        List<File> files = Arrays.asList(targetDir.listFiles());
        // if zip file has root file
        if (files.size() == 1 && files.get(0).isDirectory()) {
            String rootPath = files.get(0).toPath().toString();
            String rootFile = rootPath.substring(rootPath.lastIndexOf("/") + 1);
            File[] propertyFiles = files.get(0).listFiles();
            for (File propertyFile : propertyFiles) {
                String filePath = propertyFile.toPath().toString();
                String destPath = filePath.replace(rootFile, "");
                Files.copy(propertyFile.toPath(), Paths.get(destPath));
            }
        }
    }

    /**
     * Zips folder or file.
     *
     * @param pathToZip     file path to zip must not be null
     * @param pathOfArchive zip file path to archive must not be null
     * @throws IOException throws when failed to access file to be zipped
     */
    public static void zip(@NonNull Path pathToZip, @NonNull Path pathOfArchive) throws IOException {
        try (OutputStream outputStream = Files.newOutputStream(pathOfArchive)) {
            try (ZipOutputStream zipOut = new ZipOutputStream(outputStream)) {
                zip(pathToZip, zipOut);
            }
        }
    }

    /**
     * Zips folder or file.
     *
     * @param pathToZip file path to zip must not be null
     * @param zipOut    zip output stream must not be null
     * @throws IOException throws when failed to access file to be zipped
     */
    public static void zip(@NonNull Path pathToZip, @NonNull ZipOutputStream zipOut) throws IOException {
        // Zip file
        zip(pathToZip, pathToZip.getFileName().toString(), zipOut);
    }

    /**
     * Zips folder or file.
     *
     * @param fileToZip file path to zip must not be null
     * @param fileName  file name must not be blank
     * @param zipOut    zip output stream must not be null
     * @throws IOException throws when failed to access file to be zipped
     */
    private static void zip(@NonNull Path fileToZip, @NonNull String fileName, @NonNull ZipOutputStream zipOut) throws IOException {
        if (Files.isDirectory(fileToZip)) {
            log.debug("Try to zip folder: [{}]", fileToZip);
            // Append with '/' if missing
            String folderName = StringUtils.appendIfMissing(fileName, File.separator, File.separator);
            // Create zip entry and put into zip output stream
            zipOut.putNextEntry(new ZipEntry(folderName));
            // Close entry for writing the next entry
            zipOut.closeEntry();

            // Iterate the sub files recursively
            try (Stream<Path> subPathStream = Files.list(fileToZip)) {
                // There should not use foreach for stream as internal zip method will throw IOException
                List<Path> subFiles = subPathStream.collect(Collectors.toList());
                for (Path subFileToZip : subFiles) {
                    // Zip children
                    zip(subFileToZip, folderName + subFileToZip.getFileName(), zipOut);
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

            Path realPath = childrenPath.get(0);
            if (childrenPath.size() == 1 && Files.isDirectory(realPath)) {
                // Check directory traversal
                checkDirectoryTraversal(unzippedPath, realPath);
                return realPath;
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
