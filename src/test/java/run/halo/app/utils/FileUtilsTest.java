package run.halo.app.utils;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

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

        System.out.println("Walk path list");
        List<Path> walkList = Files.walk(tempDirectory).collect(Collectors.toList());
        walkList.forEach(System.out::println);
        Assert.assertThat(walkList.size(), equalTo(4));


        System.out.println("Walk 1 deep path list");
        List<Path> walk1DeepList = Files.walk(tempDirectory, 1).collect(Collectors.toList());
        walk1DeepList.forEach(System.out::println);
        Assert.assertThat(walk1DeepList.size(), equalTo(2));

        System.out.println("List path list");
        List<Path> listList = Files.list(tempDirectory).collect(Collectors.toList());
        listList.forEach(System.out::println);
        Assert.assertThat(listList.size(), equalTo(1));

        System.out.println("List test path list");
        List<Path> testPathList = Files.list(testPath).collect(Collectors.toList());
        testPathList.forEach(System.out::println);
        Assert.assertThat(testPathList.size(), equalTo(0));

        // Delete it
        FileUtils.deleteFolder(tempDirectory);

        Assert.assertTrue(Files.notExists(tempDirectory));
    }
}