package run.halo.app.utils;

import org.junit.Assert;
import org.junit.Test;

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
    public void compareDirectoryTest() {

        String workDir = userHome + "/halo-test/";
        Path workDirPath = Paths.get(workDir);

        Path testPath = Paths.get(userHome + "/../../etc/passwd");

        System.out.println("Work directory path: " + workDirPath);
        System.out.println("Test path: " + testPath);

        Assert.assertFalse(testPath.startsWith(workDirPath));
        Assert.assertFalse(workDirPath.startsWith(testPath));
    }
}
