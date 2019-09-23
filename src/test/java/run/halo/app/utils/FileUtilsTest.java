package run.halo.app.utils;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;

/**
 * @author johnniang
 * @date 19-4-19
 */
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
}