package run.halo.app.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import run.halo.app.model.support.HaloConst;

import java.io.File;
import java.io.IOException;
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
            walkList.forEach(System.out::println);
            Assert.assertThat(walkList.size(), equalTo(4));
        }

        try (Stream<Path> pathStream = Files.walk(tempDirectory, 1)) {
            List<Path> walkList = pathStream.collect(Collectors.toList());
            walkList.forEach(System.out::println);
            Assert.assertThat(walkList.size(), equalTo(2));
        }

        try (Stream<Path> pathStream = Files.list(tempDirectory)) {
            List<Path> walkList = pathStream.collect(Collectors.toList());
            walkList.forEach(System.out::println);
            Assert.assertThat(walkList.size(), equalTo(1));
        }

        try (Stream<Path> pathStream = Files.list(testPath)) {
            List<Path> walkList = pathStream.collect(Collectors.toList());
            walkList.forEach(System.out::println);
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
}