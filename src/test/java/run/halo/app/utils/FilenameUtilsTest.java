package run.halo.app.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import run.halo.app.exception.FileOperationException;

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
        String filename1 = "abcde";
        String filteredFilename1 = FilenameUtils.sanitizeFilename(filename1);
        assertEquals("abcde", filteredFilename1);

        String filename2 = "abcde|字符替换\\星号*大于>小于<slash/中文字符、";
        String filteredFilename2 = FilenameUtils.sanitizeFilename(filename2);
        assertEquals("abcde字符替换星号大于小于slash中文字符、", filteredFilename2);
    }

    @Test
    public void fileNameWithReversedNameWillBeReplaced() {
        String filename1 = "CON ";
        String sanitizedName = FilenameUtils.sanitizeFilename(filename1);
        assertEquals("CON_file", sanitizedName);

        String filename2 = "LPT19";
        sanitizedName = FilenameUtils.sanitizeFilename(filename2);
        assertEquals("LPT19", sanitizedName);

        String filename3 = "CON 12345";
        sanitizedName = FilenameUtils.sanitizeFilename(filename3);
        assertEquals("CON 12345", sanitizedName);

        String filename4 = "COM3";
        sanitizedName = FilenameUtils.sanitizeFilename(filename4);
        assertEquals("COM3_file", sanitizedName);
    }

    @Test
    public void filenameLengthLimit() {
        StringBuilder filename = new StringBuilder("haloe");
        while (filename.length() < 300) {
            filename.append("haloe");
        }
        assertEquals(300, filename.length());
        String sanitizedName = FilenameUtils.sanitizeFilename(filename.toString());
        assertEquals(200, sanitizedName.length());
    }

    @Test
    public void filenameMixTest() {
        String filename = "halo |I'd like.to $be a: CONtributor。。...";
        String sanitizedName = FilenameUtils.sanitizeFilename(filename);
        assertEquals("halo I'd liketo $be a CONtributor。。", sanitizedName);
    }

    @Test
    public void filenameWithDotsWillBeSanitized() {
        String filename = "pls.approve...my..PR..... ";
        String sanitizedName = FilenameUtils.sanitizeFilename(filename);
        assertEquals("plsapprovemyPR", sanitizedName);
    }

    @Test
    public void fileNameExtremelyInvalid() {
        String filename = "?<>\\:*|...";
        FileOperationException exception = assertThrows(FileOperationException.class,
            () -> FilenameUtils.sanitizeFilename(filename));
        assertEquals("文件名不合法: ?<>\\:*|...", exception.getMessage());
    }
}
