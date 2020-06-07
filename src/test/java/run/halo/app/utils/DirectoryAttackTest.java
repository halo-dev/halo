package run.halo.app.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import run.halo.app.exception.ForbiddenException;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Directory attack test.
 *
 * @author johnniang
 * @date 4/9/19
 */
@Slf4j
public class DirectoryAttackTest {

    private String userHome = System.getProperty("user.home");

    @Test
    public void compareDirectoryFailureTest() {

        Path workDirPath = Paths.get(userHome + "/halo-test/");

        Path testPath = Paths.get(userHome + "/../../etc/passwd");

        log.debug("Work directory path: [{}]", workDirPath);
        log.debug("Test path: [{}]", testPath);

        Assert.assertFalse(testPath.startsWith(workDirPath));
        Assert.assertFalse(workDirPath.startsWith(testPath));
    }

    @Test
    public void compareDirectorySuccessfullyTest() {
        Path workDirPath = Paths.get(userHome + "/halo-test/");

        Path testPath = Paths.get(userHome + "/halo-test/test.txt");

        log.debug("Work directory path: [{}]", workDirPath);
        log.debug("Test path: [{}]", testPath);

        Assert.assertTrue(testPath.startsWith(workDirPath));
        Assert.assertFalse(workDirPath.startsWith(testPath));
    }


    @Test
    public void compareDirectorySuccessfullyTest2() {
        Path workDirPath = Paths.get(userHome + "/../../etc/").normalize();

        Path testPath = Paths.get("/etc/passwd");

        log.debug("Work directory path: [{}]", workDirPath);
        log.debug("Test path: [{}]", testPath);

        Assert.assertTrue(testPath.startsWith(workDirPath));
        Assert.assertFalse(workDirPath.startsWith(testPath));
    }

    @Test
    public void getRealPathTest() {
        String pathname = "/home/test/../../etc/";
        Path path = Paths.get(pathname);

        log.debug("Path: [{}]", path);
        log.debug("Absolute path: [{}]", path.toAbsolutePath());
        log.debug("Name count: [{}]", path.getNameCount());
        log.debug("Normalized path: [{}]", path.normalize());
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
