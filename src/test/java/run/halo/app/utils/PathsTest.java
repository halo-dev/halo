package run.halo.app.utils;

import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Paths test.
 *
 * @author johnniang
 * @date 3/27/19
 */
public class PathsTest {


    @Test
    public void getTest() {
        Path path = Paths.get("/home/test/", "/upload/test.txt");
        assertThat(path.toString(), equalTo("/home/test/upload/test.txt"));
        assertThat(path.getParent().toString(), equalTo("/home/test/upload"));
    }
}
