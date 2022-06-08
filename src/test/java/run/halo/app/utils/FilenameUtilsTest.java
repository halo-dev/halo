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
        assertEquals("1.4.9", FilenameUtils.getBasename("1.4.9.png"));
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

    @Test
    public void fileNameWithReservedCharsWillBeReplaced() {
        String fileName1 = "abcde";
        String filteredFileName1 = FilenameUtils.filterReservedCharsInFileName(fileName1) + ".md";
        assertEquals("abcde.md", filteredFileName1);

        String fileName2 = "abcde|字符替换\\星号*大于>小于<slash/中文字符、";
        String filteredFileName2 = FilenameUtils.filterReservedCharsInFileName(fileName2) + ".md";
        assertEquals("abcde字符替换星号大于小于slash中文字符、.md", filteredFileName2);
    }
}
