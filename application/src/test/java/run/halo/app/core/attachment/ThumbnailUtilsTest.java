package run.halo.app.core.attachment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import org.junit.jupiter.api.Test;
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

    @Test
    void shouldBuildSrcSetWithUriWithSpecialCharacters() {
        var permalink = URI.create("/中文.png").toASCIIString();
        var srcsetMap = ThumbnailUtils.buildSrcsetMap(URI.create(permalink));
        assertEquals("/%E4%B8%AD%E6%96%87.png?width=400",
            srcsetMap.get(ThumbnailSize.S).toString());
        assertEquals("/%E4%B8%AD%E6%96%87.png?width=800",
            srcsetMap.get(ThumbnailSize.M).toString());
        assertEquals("/%E4%B8%AD%E6%96%87.png?width=1200",
            srcsetMap.get(ThumbnailSize.L).toString());
        assertEquals("/%E4%B8%AD%E6%96%87.png?width=1600",
            srcsetMap.get(ThumbnailSize.XL).toString());
    }
}