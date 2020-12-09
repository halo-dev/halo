package run.halo.app.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttachmentHandlerCovertUtilsTest {
    char[] splitChars = "~@$^&*()-_/!+={}[]|:;\"'<>,".toCharArray();
    String[] imageExtensions = ".jpg,.jpeg,.png,.gif,.bmp,.webp,.ico,.tiff,.tif,.svg,.emf".split(",");

    @Test
    void getBaseNameFromUrl() throws UnsupportedEncodingException {

        for (char splitChar : splitChars) {
            for (String imageExtension : imageExtensions) {
                String url = "http://te.st.com/te.st" + imageExtension + splitChar + "stylerule";
                assertEquals("te.st", AttachmentHandlerCovertUtils.getBaseNameFromUrl(url));
            }
        }

        String url = "";
        assertEquals(StringUtils.EMPTY, AttachmentHandlerCovertUtils.getBaseNameFromUrl(url));

        url = "http://te.st.com/test";
        assertEquals(StringUtils.EMPTY, AttachmentHandlerCovertUtils.getBaseNameFromUrl(url));

        url = "http://te.st.com/%E4%BD%A0%E5%A5%BD%E4%BD%A0%E5%A5%BD.pdf";
        assertEquals("你好你好", AttachmentHandlerCovertUtils.getBaseNameFromUrl(url));

        url = "http://te.st.com/123456789123456789123456789123456789123456789123456789123456789123456789.png";
        assertEquals("1234567891234567891234567891234567891234567891234567891234567891",
                AttachmentHandlerCovertUtils.getBaseNameFromUrl(url));
    }

    @Test
    void getImageExtension() {
        for (String imageExtension : imageExtensions) {
            String url = "http://te.st.com/te.st" + imageExtension;
            assertEquals(imageExtension.substring(1), AttachmentHandlerCovertUtils.getImageExtension(url));
        }

        String url = "";
        assertEquals("", AttachmentHandlerCovertUtils.getImageExtension(url));

        url = "http://te.st.com/test";
        assertEquals("", AttachmentHandlerCovertUtils.getImageExtension(url));

        url = "http://te.st.com/test.pdf";
        assertEquals("", AttachmentHandlerCovertUtils.getImageExtension(url));
    }

    @Test
    void encodeFileBaseName() throws UnsupportedEncodingException {
        String baseUrl = "http://te.st.com/";
        String noEncodeUrl = baseUrl + "你好你好.png";
        String encodeUrl = baseUrl + "%E4%BD%A0%E5%A5%BD%E4%BD%A0%E5%A5%BD.png";
        assertEquals(encodeUrl, AttachmentHandlerCovertUtils.encodeFileBaseName(false, noEncodeUrl));
        assertEquals(encodeUrl, AttachmentHandlerCovertUtils.encodeFileBaseName(true, encodeUrl));
        assertEquals(encodeUrl, AttachmentHandlerCovertUtils.encodeFileBaseName(true, noEncodeUrl));

        encodeUrl = baseUrl + "%e4%bd%a0%e5%a5%bd%20%e4%bd%a0%e5%a5%bd.png";
        assertEquals(AttachmentHandlerCovertUtils.encodeFileBaseName(false, baseUrl + "你好 你好.png"),
                AttachmentHandlerCovertUtils.encodeFileBaseName(true, encodeUrl));
    }

    @Test
    void splitStyleRule() {
        String url = "http://te.st.com/te.st.png";
        assertEquals(url, AttachmentHandlerCovertUtils.splitStyleRule(url));

        for (char splitChar : splitChars) {
            for (String imageExtension : imageExtensions) {
                url = "http://te.st.com/te.st" + imageExtension + splitChar + "stylerule";
                assertEquals("http://te.st.com/te.st" + imageExtension, AttachmentHandlerCovertUtils.splitStyleRule(url));
            }
        }

        for (char splitChar : splitChars) {
            url = "http://te.st.com/te.st" + ".pdf" + splitChar + "stylerule";
            assertEquals(url, AttachmentHandlerCovertUtils.splitStyleRule(url));
        }
    }

    @Test
    void getImageUrl() {

        String postMD = "![a](b.png)";
        List<String> urls = AttachmentHandlerCovertUtils.getImageUrl(postMD);
        assertEquals(1, urls.size());
        assertEquals("b.png", urls.get(0));

        postMD = "![]()";
        urls = AttachmentHandlerCovertUtils.getImageUrl(postMD);
        assertEquals(0, urls.size());

        postMD = "![](b)![](c)";
        urls = AttachmentHandlerCovertUtils.getImageUrl(postMD);
        assertEquals(2, urls.size());
        assertEquals("b", urls.get(0));
        assertEquals("c", urls.get(1));

        postMD = "![[a]()]([]b(c))";
        urls = AttachmentHandlerCovertUtils.getImageUrl(postMD);
        assertEquals(1, urls.size());
        assertEquals("[]b(c)", urls.get(0));

        postMD = "![a](b)![x](x";
        urls = AttachmentHandlerCovertUtils.getImageUrl(postMD);
        assertEquals(1, urls.size());
        assertEquals("b", urls.get(0));

        postMD = "![a](b)![x]x)";
        urls = AttachmentHandlerCovertUtils.getImageUrl(postMD);
        assertEquals(1, urls.size());
        assertEquals("b", urls.get(0));
    }

    @Test
    void downloadFile() throws IOException {
        String downloadPath = "https://halo.run/upload/2020/2/image-0c7a018e73f74634a534fa3ba8806628.png";
        String tmpPath = Paths.get(FileUtils.getTempDirectoryPath(), "a.png").toString();
        File tmpFile = new File(tmpPath);
        try {
            AttachmentHandlerCovertUtils.downloadFile(downloadPath, tmpPath);
            assertTrue(tmpFile.exists());
        } finally {
            FileUtils.forceDeleteOnExit(tmpFile);
        }
    }
}