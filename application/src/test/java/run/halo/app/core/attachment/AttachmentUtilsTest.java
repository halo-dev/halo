package run.halo.app.core.attachment;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link AttachmentUtils}.
 *
 * @author guqing
 * @since 2.19.0
 */
class AttachmentUtilsTest {

    @Test
    void encodeUriTest() {
        String urlStr =
            "http://localhost:8090/upload/2022/06/CleanShot 2022-06-12 at 23.30.22@2x.webp";
        var result = AttachmentUtils.encodeUri(urlStr);
        var targetUriStr =
            "http://localhost:8090/upload/2022/06/CleanShot%202022-06-12%20at%2023.30.22@2x.webp";
        assertThat(result.toString()).isEqualTo(targetUriStr);

        result = AttachmentUtils.encodeUri(targetUriStr);
        assertThat(result.toString()).isEqualTo(targetUriStr);
    }
}
