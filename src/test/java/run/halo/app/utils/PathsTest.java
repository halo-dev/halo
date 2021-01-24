package run.halo.app.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Paths test.
 *
 * @author johnniang
 * @date 3/27/19
 */
class PathsTest {

    @Test
    @Disabled("Due to platform dependent")
    void getTest() {
        Path path = Paths.get("/home/test/", "/upload/test.txt");
        assertEquals("/home/test/upload/test.txt", path.toString());
        assertEquals("/home/test/upload", path.getParent().toString());
        assertEquals("test.txt", path.getFileName().toString());
    }

    @Test
    void startWithTest() {
        Path path = Paths.get("/test/test.txt");
        assertEquals("test.txt", path.getFileName().toString());
        boolean isStartWith = FilenameUtils.getBasename(path.toString()).equalsIgnoreCase("test");
        assertTrue(isStartWith);
    }
}
