package run.halo.app.core.attachment;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ThumbnailSigner}.
 *
 * @author guqing
 * @since 2.19.0
 */
class ThumbnailSignerTest {

    @Test
    void generateSignature() {
        var signature = ThumbnailSigner.generateSignature("example.jpg");
        assertThat(signature).isEqualTo(
            "ed2e575a1d298e08df19dca9733da3f46eab5b157b1b8f9ed02f31b2f907ec79");
    }
}