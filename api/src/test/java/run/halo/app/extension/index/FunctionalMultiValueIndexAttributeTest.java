package run.halo.app.extension.index;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.Test;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.extension.Unstructured;

/**
 * Tests for {@link FunctionalMultiValueIndexAttribute}.
 *
 * @author guqing
 * @since 2.12.0
 */
class FunctionalMultiValueIndexAttributeTest {

    @Test
    void create() {
        var attribute = new FunctionalMultiValueIndexAttribute<>(FakeExtension.class,
            FakeExtension::getCategories);
        assertThat(attribute).isNotNull();
    }

    @Test
    void getValues() {
        var attribute = new FunctionalMultiValueIndexAttribute<>(FakeExtension.class,
            FakeExtension::getCategories);
        var fake = new FakeExtension();
        fake.setCategories(Set.of("test", "halo"));
        assertThat(attribute.getValues(fake)).isEqualTo(fake.getCategories());

        var unstructured = Unstructured.OBJECT_MAPPER.convertValue(fake, Unstructured.class);
        assertThatThrownBy(() -> attribute.getValues(unstructured))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Object type does not match");

        var demoExt = new DemoExtension();
        assertThatThrownBy(() -> attribute.getValues(demoExt))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Object type does not match");
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @GVK(group = "test.halo.run", version = "v1", kind = "FakeExtension", plural = "fakes",
        singular = "fake")
    static class FakeExtension extends AbstractExtension {
        Set<String> categories;
    }

    class DemoExtension extends AbstractExtension {
    }
}
