package run.halo.app.content;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link AbstractContentService}.
 *
 * @author guqing
 * @since 2.16.0
 */
class AbstractContentServiceTest {

    @Test
    void generateContentUnifiedDiff() {
        List<String> diff = AbstractContentService.generateContentUnifiedDiff("line1\nline2", "");
        var result = String.join(PatchUtils.DELIMITER, diff);
        assertThat(result).isEqualToIgnoringNewLines("""
            @@ -1,2 +1,0 @@
            -line1
            -line2
            """);
    }
}
