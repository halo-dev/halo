package run.halo.app.extension.router.selector;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link LabelSelector}.
 *
 * @author guqing
 * @since 2.17.0
 */
class LabelSelectorTest {

    @Test
    void builderTest() {
        var labelSelector = LabelSelector.builder()
            .eq("a", "v1")
            .in("b", "v2", "v3")
            .build();
        assertThat(labelSelector.toString())
            .isEqualTo("""
                (metadata.labels['a'] = 'v1' AND metadata.labels['b'] IN ('v2', 'v3'))\
                """);
    }
}