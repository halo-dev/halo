package run.halo.app.infra.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
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
    void detectFileExtensionTest() throws MimeTypeException {
        var ext = FileTypeDetectUtils.detectFileExtension("application/x-x509-key; format=pem");
        assertThat(ext).isEqualTo("");

        ext = FileTypeDetectUtils.detectFileExtension("text/plain");
        assertThat(ext).isEqualTo(".txt");

        ext = FileTypeDetectUtils.detectFileExtension("application/zip");
        assertThat(ext).isEqualTo(".zip");
    }
}
