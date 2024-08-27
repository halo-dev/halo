package run.halo.app.core.attachment;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
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

    @Test
    void generateSignatureWithUri() {
        var uri = URI.create("https://example.com/example.jpg");
        var signature = ThumbnailSigner.generateSignature(uri);
        assertThat(signature).isEqualTo(
            "35b8dc8b5b518222245b3f74ea3c3b292e31eb0d888102b5e5a9cdbe4fb7a976");

        uri = URI.create("/upload/%E4%B8%AD%E6%96%87%E5%A4%B4%E5%83%8F%20hello%25avatar.png");
        signature = ThumbnailSigner.generateSignature(uri);
        var avatarPngHash = "7dc5019aea284094c5ba9c4404e3cedf779a2fec0d836cbdf542da3cb373dd3c";
        assertThat(signature).isEqualTo(avatarPngHash);

        uri = URI.create("/upload/中文头像%20hello%25avatar.png");
        signature = ThumbnailSigner.generateSignature(uri);
        assertThat(signature).isEqualTo(avatarPngHash);
    }
}
