package run.halo.app.infra.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import org.apache.tika.mime.MimeTypeException;
import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;

/**
 * Test for {@link FileTypeDetectUtils}.
 *
 * @author guqing
 * @since 2.18.0
 */
class FileTypeDetectUtilsTest {

    @Test
    void detectMimeTypeTest() throws IOException {
        var file = ResourceUtils.getFile("classpath:app.key");
        String mimeType = FileTypeDetectUtils.detectMimeType(Files.newInputStream(file.toPath()));
        assertThat(mimeType).isEqualTo("application/x-x509-key; format=pem");

        file = ResourceUtils.getFile("classpath:console/index.html");
        mimeType = FileTypeDetectUtils.detectMimeType(Files.newInputStream(file.toPath()));
        assertThat(mimeType).isEqualTo("text/plain");

        file = ResourceUtils.getFile("classpath:themes/test-theme.zip");
        mimeType = FileTypeDetectUtils.detectMimeType(Files.newInputStream(file.toPath()));
        assertThat(mimeType).isEqualTo("application/zip");
    }

    @Test
    void detectMimeTypeWithNameTest() throws IOException {
        var stream = getFileInputStream("classpath:file-type-detect/index.js");
        String mimeType = FileTypeDetectUtils.detectMimeType(stream, "index.js");
        assertThat(mimeType).isEqualTo("application/javascript");

        stream = getFileInputStream("classpath:file-type-detect/index.html");
        mimeType =
            FileTypeDetectUtils.detectMimeType(stream, "index.html");
        assertThat(mimeType).isEqualTo("text/html");

        stream = getFileInputStream("classpath:file-type-detect/test.json");
        mimeType = FileTypeDetectUtils.detectMimeType(stream, "test.json");
        assertThat(mimeType).isEqualTo("application/json");

        stream = getFileInputStream("classpath:file-type-detect/other.xlsx");
        mimeType = FileTypeDetectUtils.detectMimeType(stream, "other.xlsx");
        assertThat(mimeType).isEqualTo(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        // other.xlsx detect without name
        stream = getFileInputStream("classpath:file-type-detect/other.xlsx");
        mimeType = FileTypeDetectUtils.detectMimeType(stream);
        assertThat(mimeType).isEqualTo("application/zip");

        // other.xlsx detect with wrong name
        stream = getFileInputStream("classpath:file-type-detect/other.xlsx");
        mimeType = FileTypeDetectUtils.detectMimeType(stream, "other.txt");
        assertThat(mimeType).isEqualTo("application/zip");

        stream = getFileInputStream("classpath:file-type-detect/test.docx");
        mimeType = FileTypeDetectUtils.detectMimeType(stream, "test.docx");
        assertThat(mimeType).isEqualTo(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

        // docx detect without file name
        stream = getFileInputStream("classpath:file-type-detect/test.docx");
        mimeType = FileTypeDetectUtils.detectMimeType(stream);
        assertThat(mimeType).isEqualTo("application/zip");

        stream = getFileInputStream("classpath:file-type-detect/test.svg");
        mimeType = FileTypeDetectUtils.detectMimeType(stream, "test.svg");
        assertThat(mimeType).isEqualTo("image/svg+xml");

        stream = getFileInputStream("classpath:file-type-detect/test.png");
        mimeType = FileTypeDetectUtils.detectMimeType(stream, "test.png");
        assertThat(mimeType).isEqualTo("image/png");
    }

    private static InputStream getFileInputStream(String location) throws IOException {
        var file = ResourceUtils.getFile(location);
        return Files.newInputStream(file.toPath());
    }

    @Test
    void detectFileExtensionTest() throws MimeTypeException {
        var ext = FileTypeDetectUtils.detectFileExtension("application/x-x509-key; format=pem");
        assertThat(ext).isEqualTo("");

        ext = FileTypeDetectUtils.detectFileExtension("text/plain");
        assertThat(ext).isEqualTo(".txt");

        ext = FileTypeDetectUtils.detectFileExtension("application/zip");
        assertThat(ext).isEqualTo(".zip");

        ext = FileTypeDetectUtils.detectFileExtension("image/bmp");
        assertThat(ext).isEqualTo(".bmp");
    }

    @Test
    void getFileExtensionTest() {
        var ext = FileTypeDetectUtils.getFileExtension("BMP+HTML+JAR.html");
        assertThat(ext).isEqualTo(".html");

        ext = FileTypeDetectUtils.getFileExtension("test.jpg");
        assertThat(ext).isEqualTo(".jpg");

        ext = FileTypeDetectUtils.getFileExtension("hello");
        assertThat(ext).isEqualTo("");
    }

    @Test
    void isValidExtensionForMimeTest() {
        assertThat(FileTypeDetectUtils.isValidExtensionForMime("image/bmp", "hello.html"))
            .isFalse();

        assertThat(FileTypeDetectUtils.isValidExtensionForMime("image/bmp", "hello.bmp"))
            .isTrue();
    }
}
