package run.halo.app.utils;

import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Paths test.
 *
 * @author johnniang
 * @date 3/27/19
 */
public class PathsTest {

    @Test
    @Ignore
    public void getTest() {
        Path path = Paths.get("/home/test/", "/upload/test.txt");
        assertThat(path.toString(), equalTo("/home/test/upload/test.txt"));
        assertThat(path.getParent().toString(), equalTo("/home/test/upload"));
        assertThat(path.getFileName().toString(), equalTo("test.txt"));
    }

    @Test
    public void startWithTest() {
        Path path = Paths.get("/test/test.txt");
        assertThat(path.getFileName().toString(), equalTo("test.txt"));
        boolean isStartWith = FilenameUtils.getBasename(path.toString()).equalsIgnoreCase("test");
        assertTrue(isStartWith);
    }
}
