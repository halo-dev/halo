package run.halo.app.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import run.halo.app.exception.ForbiddenException;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Directory attack test.
 *
 * @author johnniang
 * @date 4/9/19
 */
@Slf4j
class DirectoryAttackTest {

    String userHome = System.getProperty("user.home");

    @Test
    void compareDirectoryFailureTest() {

        Path workDirPath = Paths.get(userHome + "/halo-test/");

        Path testPath = Paths.get(userHome + "/../../etc/passwd");

        log.debug("Work directory path: [{}]", workDirPath);
        log.debug("Test path: [{}]", testPath);

        assertFalse(testPath.startsWith(workDirPath));
        assertFalse(workDirPath.startsWith(testPath));
    }

    @Test
    void compareDirectorySuccessfullyTest() {
        Path workDirPath = Paths.get(userHome + "/halo-test/");

        Path testPath = Paths.get(userHome + "/halo-test/test.txt");

        log.debug("Work directory path: [{}]", workDirPath);
        log.debug("Test path: [{}]", testPath);

        assertTrue(testPath.startsWith(workDirPath));
        assertFalse(workDirPath.startsWith(testPath));
    }


    @Test
    void compareDirectorySuccessfullyTest2() {
        Path workDirPath = Paths.get(userHome + "/../../etc/").normalize();

        Path testPath = Paths.get("/etc/passwd");

        log.debug("Work directory path: [{}]", workDirPath);
        log.debug("Test path: [{}]", testPath);

        assertTrue(testPath.startsWith(workDirPath));
        assertFalse(workDirPath.startsWith(testPath));
    }

    @Test
    void getRealPathTest() {
        String pathname = "/home/test/../../etc/";
        Path path = Paths.get(pathname);

        log.debug("Path: [{}]", path);
        log.debug("Absolute path: [{}]", path.toAbsolutePath());
        log.debug("Name count: [{}]", path.getNameCount());
        log.debug("Normalized path: [{}]", path.normalize());
    }

    @Test
    void traversalTestWhenSuccess() {
        FileUtils.checkDirectoryTraversal("/etc/", "/etc/halo/halo/../test");
    }

    @Test
    void traversalTestWhenFailure() {
        assertThrows(ForbiddenException.class,
            () -> FileUtils.checkDirectoryTraversal("/etc/", "/etc/../tmp"));
    }
}
