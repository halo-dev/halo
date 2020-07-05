package run.halo.app.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Filename utilities test.
 *
 * @author johnniang
 * @date 3/26/19
 */
class FilenameUtilsTest {

    // a/b/c.txt --> c.txt
    // a.txt     --> a.txt
    // a/b/c     --> c
    // a/b/c/    --> ""
    @Test
    void getBasename() {
        assertEquals("c", FilenameUtils.getBasename("a/b/c.txt"));
        assertEquals("a", FilenameUtils.getBasename("a.txt"));
        assertEquals("c", FilenameUtils.getBasename("a/b/c"));
        assertEquals("", FilenameUtils.getBasename("a/b/c/"));
    }

    // foo.txt      --> "txt"
    // a/b/c.jpg    --> "jpg"
    // a/b.txt/c    --> ""
    // a/b/c        --> ""
    @Test
    void getExtension() {
        assertEquals("txt", FilenameUtils.getExtension("foo.txt"));
        assertEquals("jpg", FilenameUtils.getExtension("a/b/c.jpg"));
        assertEquals("", FilenameUtils.getExtension("a/b.txt/c"));
        assertEquals("", FilenameUtils.getExtension("a/b/c"));
        assertEquals("", FilenameUtils.getExtension("a/b/c/"));
    }
}
