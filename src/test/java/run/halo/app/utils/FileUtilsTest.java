package run.halo.app.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
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
import java.util.Optional;
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

    Path tempDirectory = null;

    @AfterEach
    void cleanUp() throws IOException {
        if (tempDirectory != null) {
            FileUtils.deleteFolder(tempDirectory);
            assertTrue(Files.notExists(tempDirectory));
        }
    }

    @Test
    void deleteFolder() throws IOException {
        // Create a temp folder
        tempDirectory = Files.createTempDirectory("halo-test");

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
    }

    @Test
    void zipFolderTest() throws IOException {
        // Create some temporary files
        tempDirectory = Files.createTempDirectory("zip-root-");
        log.debug("Folder name: [{}]", tempDirectory.getFileName());
        Files.createTempFile(tempDirectory, "zip-file1-", ".txt");
        Files.createTempFile(tempDirectory, "zip-file2-", ".txt");
        Path subRootFolder = Files.createTempDirectory(tempDirectory, "zip-subroot-");
        Files.createTempFile(subRootFolder, "zip-subfile1-", ".txt");
        Files.createTempFile(subRootFolder, "zip-subfile2-", ".txt");

        // Create target file
        Path zipToStore = Files.createTempFile("zipped-", ".zip");
        // Create zip output stream
        try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(zipToStore))) {
            // Zip file
            FileUtils.zip(tempDirectory, zipOut);
        }

        // Clear the test folder created before
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
        tempDirectory = Files.createTempDirectory("halo-test");

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
    }

    @Test
    void testRenameFolder() throws IOException {
        // Create a temp folder
        tempDirectory = Files.createTempDirectory("halo-test");

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
    }

    @Test
    void testRenameRepeat() throws IOException {
        // Create a temp folder
        tempDirectory = Files.createTempDirectory("halo-test");

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
    }

    @Test
    void findRootPathTest() throws IOException {
        // build folder structure
        // folder1
        // file1
        // folder2
        //   file2
        //   folder3
        //     expected_file
        // expected: folder2
        tempDirectory = Files.createTempDirectory("halo-test");

        log.info("Preparing test folder structure");
        Path folder1 = tempDirectory.resolve("folder1");
        Files.createDirectory(folder1);
        Path file1 = tempDirectory.resolve("file1");
        Files.createFile(file1);
        Path folder2 = tempDirectory.resolve("folder2");
        Files.createDirectory(folder2);
        Path file2 = folder2.resolve("file2");
        Files.createFile(file2);
        Path folder3 = folder2.resolve("folder3");
        Files.createDirectory(folder3);
        Path expectedFile = folder3.resolve("expected_file");
        Files.createFile(expectedFile);
        log.info("Prepared test folder structure");

        // find the root folder where expected file locates, and we expect folder3
        Optional<Path> rootPath = FileUtils.findRootPath(tempDirectory, path -> path.getFileName().toString().equals("expected_file"));
        assertTrue(rootPath.isPresent());
        assertEquals(folder3.toString(), rootPath.get().toString());
    }


    @Test
    void findRootPathIgnoreTest() throws IOException {
        // build folder structure
        // folder1
        // .git
        //   expected_file
        // file1
        // folder2
        //   file2
        //   folder3
        //     file3
        // expected: folder2
        tempDirectory = Files.createTempDirectory("halo-test");

        log.info("Preparing test folder structure");
        Path folder1 = tempDirectory.resolve("folder1");
        Files.createDirectory(folder1);
        Path dotGit = tempDirectory.resolve(".git");
        Files.createDirectory(dotGit);
        Path expectedFile = dotGit.resolve("expected_file");
        Files.createFile(expectedFile);
        Path file1 = tempDirectory.resolve("file1");
        Files.createFile(file1);
        Path folder2 = tempDirectory.resolve("folder2");
        Files.createDirectory(folder2);
        Path file2 = folder2.resolve("file2");
        Files.createFile(file2);
        Path folder3 = folder2.resolve("folder3");
        Files.createDirectory(folder3);
        Path file3 = folder3.resolve("file3");
        Files.createFile(file3);
        log.info("Prepared test folder structure");

        // find the root folder where file3 locates, and we expect folder3
        Optional<Path> rootPath = FileUtils.findRootPath(tempDirectory, path -> path.getFileName().toString().equals("expected_file"));
        assertFalse(rootPath.isPresent());
    }
}