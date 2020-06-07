package run.halo.app.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
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

import static org.hamcrest.Matchers.equalTo;

/**
 * @author johnniang
 * @date 19-4-19
 */
@Slf4j
public class FileUtilsTest {

    @Test
    public void deleteFolder() throws IOException {
        // Create a temp folder
        Path tempDirectory = Files.createTempDirectory("halo-test");

        Path testPath = tempDirectory.resolve("test/test/test");

        // Create test folders
        Files.createDirectories(testPath);

        try (Stream<Path> pathStream = Files.walk(tempDirectory)) {
            List<Path> walkList = pathStream.collect(Collectors.toList());
            walkList.forEach(path -> log.debug(path.toString()));
            Assert.assertThat(walkList.size(), equalTo(4));
        }

        try (Stream<Path> pathStream = Files.walk(tempDirectory, 1)) {
            List<Path> walkList = pathStream.collect(Collectors.toList());
            walkList.forEach(path -> log.debug(path.toString()));
            Assert.assertThat(walkList.size(), equalTo(2));
        }

        try (Stream<Path> pathStream = Files.list(tempDirectory)) {
            List<Path> walkList = pathStream.collect(Collectors.toList());
            walkList.forEach(path -> log.debug(path.toString()));
            Assert.assertThat(walkList.size(), equalTo(1));
        }

        try (Stream<Path> pathStream = Files.list(testPath)) {
            List<Path> walkList = pathStream.collect(Collectors.toList());
            walkList.forEach(path -> log.debug(path.toString()));
            Assert.assertThat(walkList.size(), equalTo(0));
        }

        // Delete it
        FileUtils.deleteFolder(tempDirectory);

        Assert.assertTrue(Files.notExists(tempDirectory));
    }

    @Test
    public void zipFolderTest() throws IOException {
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
    public void tempFolderTest() {
        log.debug(HaloConst.TEMP_DIR);
    }

    @Test
    @Ignore
    public void dbFileReadTest() throws IOException {
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
    public void testRenameFile() throws IOException {
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

        Assert.assertFalse(Files.exists(filePath));
        Assert.assertTrue(Files.isRegularFile(newPath));
        Assert.assertEquals(new String(Files.readAllBytes(newPath)), content);

        FileUtils.deleteFolder(tempDirectory);
    }

    @Test
    public void testRenameFolder() throws IOException {
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

        Assert.assertTrue(Files.isDirectory(newPath));
        Assert.assertTrue(Files.isRegularFile(newPath.resolve("test.file")));

        FileUtils.deleteFolder(tempDirectory);
    }

    @Test
    public void testRenameRepeat() throws IOException {
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
            Assert.assertTrue(e instanceof FileAlreadyExistsException);
        }

        try {
            FileUtils.rename(filePathOne, "testTwo.file");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof FileAlreadyExistsException);
        }

        try {
            FileUtils.rename(filePathOne, "testOne");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof FileAlreadyExistsException);
        }

        FileUtils.deleteFolder(tempDirectory);
    }
}