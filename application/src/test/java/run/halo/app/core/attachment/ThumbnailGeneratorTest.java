package run.halo.app.core.attachment;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ThumbnailGenerator}.
 *
 * @author guqing
 * @since 2.19.0
 */
class ThumbnailGeneratorTest {

    @Test
    void sanitizeFileName() {
        String sanitizedFileName = ThumbnailGenerator.sanitizeFileName("example.jpg");
        assertThat(sanitizedFileName).isEqualTo("example.jpg");

        sanitizedFileName = ThumbnailGenerator.sanitizeFileName("exampl./e$%^7*—=.jpg");
        assertThat(sanitizedFileName).isEqualTo("exampl.e7.jpg");
    }
}