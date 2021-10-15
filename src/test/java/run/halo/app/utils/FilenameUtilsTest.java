package run.halo.app.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

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
    // he/ll/o.tar.gz --> "o"
    // h/i.tar.bz2 --> "i"
    @Test
    void getBasename() {
        assertEquals("c", FilenameUtils.getBasename("a/b/c.txt"));
        assertEquals("a", FilenameUtils.getBasename("a.txt"));
        assertEquals("c", FilenameUtils.getBasename("a/b/c"));
        assertEquals("", FilenameUtils.getBasename("a/b/c/"));
        assertEquals("o", FilenameUtils.getBasename("he/ll/o.tar.gz"));
        assertEquals("i", FilenameUtils.getBasename("h/i.tar.bz2"));
    }

    // foo.txt      --> "txt"
    // a/b/c.jpg    --> "jpg"
    // a/b.txt/c    --> ""
    // a/b/c        --> ""
    // he/ll/o.tar.gz --> "tar.gz"
    // he/ll/o.tar.bz2 --> "tar.bz2"
    @Test
    void getExtension() {
        assertEquals("txt", FilenameUtils.getExtension("foo.txt"));
        assertEquals("jpg", FilenameUtils.getExtension("a/b/c.jpg"));
        assertEquals("", FilenameUtils.getExtension("a/b.txt/c"));
        assertEquals("", FilenameUtils.getExtension("a/b/c"));
        assertEquals("", FilenameUtils.getExtension("a/b/c/"));
        assertEquals("tar.gz", FilenameUtils.getExtension("he/ll/o.tar.gz"));
        assertEquals("tar.bz2", FilenameUtils.getExtension("he/ll/o.tar.bz2"));
    }
}
