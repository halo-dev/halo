package run.halo.app.core.attachment;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;

class ThumbnailUtilsTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "image/jpg", "image/jpeg", "image/png", "image/bmp", "image/vnd.wap.wbmp",
    })
    void isSupportedImageTestByMimeType(String mimeType) {
        assertTrue(ThumbnailUtils.isSupportedImage(MediaType.parseMediaType(mimeType)));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "image/svg+xml", "image/gif", "image/webp", "image/x-icon", "image/avif", "image/tiff",
        "application/json", "text/plain"
    })
    void isNotSupportedImageTestByMimeType(String mimeType) {
        assertFalse(ThumbnailUtils.isSupportedImage(MediaType.parseMediaType(mimeType)));
    }

}