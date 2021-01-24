package run.halo.app.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

/**
 * @author johnniang
 * @date 3/26/19
 */
@Slf4j
class MediaTypeTest {

    @Test
    void toStringTest() {
        MediaType mediaType = MediaType.IMAGE_GIF;

        assertEquals("image/gif", mediaType.toString());
    }

    @Test
    void parseTest() {
        MediaType mediaType = MediaType.valueOf("image/gif");

        assertEquals(MediaType.IMAGE_GIF, mediaType);
    }

    @Test
    void includesTest() {
        MediaType mediaType = MediaType.valueOf("image/*");
        boolean isInclude = mediaType.includes(MediaType.IMAGE_GIF);
        assertTrue(isInclude);

        isInclude = mediaType.includes(MediaType.IMAGE_JPEG);
        assertTrue(isInclude);

        isInclude = mediaType.includes(MediaType.IMAGE_PNG);
        assertTrue(isInclude);

        isInclude = mediaType.includes(MediaType.TEXT_HTML);
        assertFalse(isInclude);
    }

}
