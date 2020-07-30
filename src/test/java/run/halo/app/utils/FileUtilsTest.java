package run.halo.app.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import run.halo.app.model.support.HaloConst;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author johnniang
 * @date 19-4-19
 */
@Slf4j
class FileUtilsTest {

    @Test
    void deleteFolder() throws IOException {
        // Create a temp folder
        Path tempDirectory = Files.createTempDirectory("halo-test");

        Path testPath = tempDirectory.resolve("test/test/test");

        // Create test folders
        Files.createDirectories(testPath);

        try (Stream<Path> pathStream = Files.walk(tempDirectory)) {
            List<Path> walkList = pathStream.collect(Collectors.toList());
            walkList.forEach(path -> log.debug(path.toString()));
            assertEquals(4, walkList.size());
        }

        try (Stream<Path> pathStream = Files.walk(tempDirectory, 1)) {
            List<Path> walkList = pathStream.collect(Collectors.toList());
            walkList.forEach(path -> log.debug(path.toString()));
            assertEquals(2, walkList.size());
        }

        try (Stream<Path> pathStream = Files.list(tempDirectory)) {
            List<Path> walkList = pathStream.collect(Collectors.toList());
            walkList.forEach(path -> log.debug(path.toString()));
            assertEquals(1, walkList.size());
        }

        try (Stream<Path> pathStream = Files.list(testPath)) {
            List<Path> walkList = pathStream.collect(Collectors.toList());
            walkList.forEach(path -> log.debug(path.toString()));
            assertEquals(0, walkList.size());
        }

        // Delete it
        FileUtils.deleteFolder(tempDirectory);

        assertTrue(Files.notExists(tempDirectory));
    }

    @Test
    void zipFolderTest() throws IOException {
        // Create some temporary files
        Path rootFolder = Files.createTempDirectory("zip-root-");
        log.debug("Folder name: [{}]", rootFolder.getFileName());
        Files.createTempFile(rootFolder, "zip-file1-", ".txt");
        Files.createTempFile(rootFolder, "zip-file2-", ".txt");
        Path subRootFolder = Files.createTempDirectory(rootFolder, "zip-subroot-");
        Files.createTempFile(subRootFolder, "zip-subfile1-", ".txt");
        Files.createTempFile(subRootFolder, "zip-subfile2-", ".txt");

        // Create target file
        Path zipToStore = Files.createTempFile("zipped-", ".zip");
        // Create zip output stream
        try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(zipToStore))) {
            // Zip file
            FileUtils.zip(rootFolder, zipOut);
        }

        // Clear the test folder created before
        FileUtils.deleteFolder(rootFolder);
        Files.delete(zipToStore);
    }

    @Test
    void tempFolderTest() {
        log.debug(HaloConst.TEMP_DIR);
    }

    @Test
    @Disabled("Due to depend on halo.mv.db file")
    void dbFileReadTest() throws IOException {
        Path dbPath = Paths.get(HaloConst.USER_HOME + "/halo-test/db/halo.mv.db");

        try (RandomAccessFile randomAccessFile = new RandomAccessFile(dbPath.toString(), "r")) {
            randomAccessFile.seek(2283640);
            byte[] buffer = new byte[1024];
            int count = randomAccessFile.read(buffer, 0, buffer.length);
            log.debug("Count: [{}]", count);
            String bufString = new String(buffer);
            log.debug("Buffer String: [{}]", bufString);
        }
    }

    @Test
    void testRenameFile() throws IOException {
        // Create a temp folder
        Path tempDirectory = Files.createTempDirectory("halo-test");

        Path testPath = tempDirectory.resolve("test/test");
        Path filePath = tempDirectory.resolve("test/test/test.file");

        // Create a temp file and folder
        Files.createDirectories(testPath);
        Files.createFile(filePath);

        // Write content to the temp file
        String content = "Test Content!\n";
        Files.write(filePath, content.getBytes());

        // Rename temp file
        FileUtils.rename(filePath, "newName");
        Path newPath = filePath.resolveSibling("newName");

        assertFalse(Files.exists(filePath));
        assertTrue(Files.isRegularFile(newPath));
        assertEquals(content, new String(Files.readAllBytes(newPath)));

        FileUtils.deleteFolder(tempDirectory);
    }

    @Test
    void testRenameFolder() throws IOException {
        // Create a temp folder
        Path tempDirectory = Files.createTempDirectory("halo-test");

        Path testPath = tempDirectory.resolve("test/test");
        Path filePath = tempDirectory.resolve("test/test.file");

        // Create a temp file and folder
        Files.createDirectories(testPath);
        Files.createFile(filePath);

        // Rename temp folder
        FileUtils.rename(tempDirectory.resolve("test"), "newName");
        Path newPath = tempDirectory.resolve("newName");

        assertTrue(Files.isDirectory(newPath));
        assertTrue(Files.isRegularFile(newPath.resolve("test.file")));

        FileUtils.deleteFolder(tempDirectory);
    }

    @Test
    void testRenameRepeat() throws IOException {
        // Create a temp folder
        Path tempDirectory = Files.createTempDirectory("halo-test");

        Path testPathOne = tempDirectory.resolve("test/testOne");
        Path testPathTwo = tempDirectory.resolve("test/testTwo");
        Path filePathOne = tempDirectory.resolve("test/testOne.file");
        Path filePathTwo = tempDirectory.resolve("test/testTwo.file");

        // Create temp files and folders
        Files.createDirectories(testPathOne);
        Files.createDirectories(testPathTwo);
        Files.createFile(filePathOne);
        Files.createFile(filePathTwo);

        try {
            FileUtils.rename(testPathOne, "testTwo");
        } catch (Exception e) {
            assertTrue(e instanceof FileAlreadyExistsException);
        }

        try {
            FileUtils.rename(filePathOne, "testTwo.file");
        } catch (Exception e) {
            assertTrue(e instanceof FileAlreadyExistsException);
        }

        try {
            FileUtils.rename(filePathOne, "testOne");
        } catch (Exception e) {
            assertTrue(e instanceof FileAlreadyExistsException);
        }

        FileUtils.deleteFolder(tempDirectory);
    }
}