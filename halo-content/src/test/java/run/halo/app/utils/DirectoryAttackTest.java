package run.halo.app.utils;

import org.junit.Assert;
import org.junit.Test;
import run.halo.app.exception.ForbiddenException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Directory attack test.
 *
 * @author johnniang
 * @date 4/9/19
 */
public class DirectoryAttackTest {

    private String userHome = System.getProperty("user.home");

    @Test
    public void compareDirectoryFailureTest() {

        Path workDirPath = Paths.get(userHome + "/halo-test/");

        Path testPath = Paths.get(userHome + "/../../etc/passwd");

        System.out.println("Work directory path: " + workDirPath);
        System.out.println("Test path: " + testPath);

        Assert.assertFalse(testPath.startsWith(workDirPath));
        Assert.assertFalse(workDirPath.startsWith(testPath));
    }

    @Test
    public void compareDirectorySuccessfullyTest() {
        Path workDirPath = Paths.get(userHome + "/halo-test/");

        Path testPath = Paths.get(userHome + "/halo-test/test.txt");

        System.out.println("Work directory path: " + workDirPath);
        System.out.println("Test path: " + testPath);

        Assert.assertTrue(testPath.startsWith(workDirPath));
        Assert.assertFalse(workDirPath.startsWith(testPath));
    }


    @Test
    public void compareDirectorySuccessfullyTest2() {
        Path workDirPath = Paths.get(userHome + "/../../etc/").normalize();

        Path testPath = Paths.get("/etc/passwd");

        System.out.println("Work directory path: " + workDirPath);
        System.out.println("Test path: " + testPath);

        Assert.assertTrue(testPath.startsWith(workDirPath));
        Assert.assertFalse(workDirPath.startsWith(testPath));
    }

    @Test
    public void getRealPathTest() {
        String pathname = "/home/test/../../etc/";
        Path path = Paths.get(pathname);

        System.out.println("Path: " + path);
        System.out.println("Absolute path: " + path.toAbsolutePath());
        System.out.println("Name count: " + path.getNameCount());
        System.out.println("Normalized path: " + path.normalize());
    }

    @Test
    public void traversalTestWhenSuccess() {
        FileUtils.checkDirectoryTraversal("/etc/", "/etc/halo/halo/../test");
    }

    @Test(expected = ForbiddenException.class)
    public void traversalTestWhenFailure() {
        FileUtils.checkDirectoryTraversal("/etc/", "/etc/../tmp");
    }
}
