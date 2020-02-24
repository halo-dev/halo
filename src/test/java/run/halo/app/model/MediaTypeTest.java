package run.halo.app.model;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

/**
 * @author johnniang
 * @date 3/26/19
 */
@Slf4j
public class MediaTypeTest {

    @Test
    public void toStringTest() {
        MediaType mediaType = MediaType.IMAGE_GIF;

        assertThat(mediaType.toString(), equalTo("image/gif"));
    }

    @Test
    public void parseTest() {
        MediaType mediaType = MediaType.valueOf("image/gif");

        assertNotNull(mediaType);
        assertThat(mediaType, equalTo(MediaType.IMAGE_GIF));
    }

    @Test
    public void includesTest() {
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
