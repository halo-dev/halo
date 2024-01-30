package run.halo.app.extension.index;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static run.halo.app.extension.index.PrimaryKeySpecUtils.primaryKeyIndexSpec;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.Test;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * Test for {@link DefaultIndexSpecs}.
 *
 * @author guqing
 * @since 2.12.0
 */
class DefaultIndexSpecsTest {

    @Test
    void add() {
        var specs = new DefaultIndexSpecs();
        specs.add(primaryKeyIndexSpec(FakeExtension.class));
        assertThat(specs.contains(PrimaryKeySpecUtils.PRIMARY_INDEX_NAME)).isTrue();
    }

    @Test
    void addWithException() {
        var specs = new DefaultIndexSpecs();
        var nameSpec = new IndexSpec().setName("test");
        assertThatThrownBy(() -> specs.add(nameSpec))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("IndexSpec indexFunc must not be null");
        nameSpec.setIndexFunc(IndexAttributeFactory.simpleAttribute(FakeExtension.class,
            e -> e.getMetadata().getName()));
        specs.add(nameSpec);
        assertThatThrownBy(() -> specs.add(nameSpec))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("IndexSpec with name test already exists");
    }

    @Test
    void getIndexSpecs() {
        var specs = new DefaultIndexSpecs();
        specs.add(primaryKeyIndexSpec(FakeExtension.class));
        assertThat(specs.getIndexSpecs()).hasSize(1);
    }

    @Test
    void getIndexSpec() {
        var specs = new DefaultIndexSpecs();
        var nameSpec = primaryKeyIndexSpec(FakeExtension.class);
        specs.add(nameSpec);
        assertThat(specs.getIndexSpec(PrimaryKeySpecUtils.PRIMARY_INDEX_NAME)).isEqualTo(nameSpec);
    }

    @Test
    void remove() {
        var specs = new DefaultIndexSpecs();
        var nameSpec = primaryKeyIndexSpec(FakeExtension.class);
        specs.add(nameSpec);
        assertThat(specs.contains(PrimaryKeySpecUtils.PRIMARY_INDEX_NAME)).isTrue();
        specs.remove(PrimaryKeySpecUtils.PRIMARY_INDEX_NAME);
        assertThat(specs.contains(PrimaryKeySpecUtils.PRIMARY_INDEX_NAME)).isFalse();
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @GVK(group = "test", version = "v1", kind = "FakeExtension", plural = "fakeextensions",
        singular = "fakeextension")
    static class FakeExtension extends AbstractExtension {
        private String email;
    }
}
