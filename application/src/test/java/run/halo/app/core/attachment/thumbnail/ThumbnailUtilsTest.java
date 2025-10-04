package run.halo.app.core.attachment.thumbnail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import run.halo.app.core.attachment.ThumbnailSize;

class ThumbnailUtilsTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "image/jpg", "image/jpeg", "image/png", "image/bmp", "image/vnd.wap.wbmp",
    })
    void shouldBeSupportedImageType(String mimeType) {
        Assertions.assertTrue(ThumbnailUtils.isSupportedImage(MediaType.parseMediaType(mimeType)));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "image/svg+xml", "image/gif", "image/webp", "image/x-icon", "image/avif", "image/tiff",
        "application/json", "text/plain", "application/octet-stream"
    })
    void shouldNotBeSupportedImageType(String mimeType) {
        assertFalse(ThumbnailUtils.isSupportedImage(MediaType.parseMediaType(mimeType)));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "jpg", "jpeg", "png", "bmp", "wbmp", "JPG", "JPEG", "PNG", "BMP", "WBMP"
    })
    void shouldBeSupportedImageSuffix(String suffix) {
        assertTrue(ThumbnailUtils.isSupportedImage(suffix));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "svg", "avif", "gif", "webp", "x-icon", "tiff", "json", "txt", "", " ", "  ", ".jpg"
    })
    void shouldNotBeSupportedImageSuffix(String suffix) {
        assertFalse(ThumbnailUtils.isSupportedImage(suffix));
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