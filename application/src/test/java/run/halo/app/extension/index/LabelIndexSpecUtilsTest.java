package run.halo.app.extension.index;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import org.junit.jupiter.api.Test;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.Metadata;

/**
 * Tests for {@link LabelIndexSpecUtils}.
 *
 * @author guqing
 * @since 2.12.0
 */
class LabelIndexSpecUtilsTest {

    @Test
    void labelKeyValuePair() {
        var pair = LabelIndexSpecUtils.labelKeyValuePair("key=value");
        assertThat(pair.getFirst()).isEqualTo("key");
        assertThat(pair.getSecond()).isEqualTo("value");

        pair = LabelIndexSpecUtils.labelKeyValuePair("key=value=1");
        assertThat(pair.getFirst()).isEqualTo("key");
        assertThat(pair.getSecond()).isEqualTo("value=1");

        assertThatThrownBy(() -> LabelIndexSpecUtils.labelKeyValuePair("key"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid label key-value pair: key");
    }

    @Test
    void labelIndexValueFunc() {
        var ext = new TestExtension();
        ext.setMetadata(new Metadata());
        assertThat(LabelIndexSpecUtils.labelIndexValueFunc(ext)).isEmpty();

        ext.getMetadata().setLabels(Map.of("key", "value", "key1", "value1"));
        assertThat(LabelIndexSpecUtils.labelIndexValueFunc(ext)).containsExactlyInAnyOrder(
            "key=value", "key1=value1");
    }

    static class TestExtension extends AbstractExtension {

    }
}
