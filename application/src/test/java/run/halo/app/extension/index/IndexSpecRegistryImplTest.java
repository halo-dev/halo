package run.halo.app.extension.index;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.extension.Scheme;

/**
 * Tests for {@link IndexSpecRegistryImpl}.
 *
 * @author guqing
 * @since 2.12.0
 */
@ExtendWith(MockitoExtension.class)
class IndexSpecRegistryImplTest {
    @InjectMocks
    private IndexSpecRegistryImpl indexSpecRegistry;

    private final Scheme scheme = Scheme.buildFromType(FakeExtension.class);

    @AfterEach
    void tearDown() {
        indexSpecRegistry.removeIndexSpecs(scheme);
    }

    @Test
    void indexFor() {
        var specs = indexSpecRegistry.indexFor(scheme);
        assertThat(specs).isNotNull();
        assertThat(specs.getIndexSpecs()).hasSize(4);
    }

    @Test
    void contains() {
        indexSpecRegistry.indexFor(scheme);
        assertThat(indexSpecRegistry.contains(scheme)).isTrue();
    }

    @Test
    void getKeySpace() {
        var keySpace = indexSpecRegistry.getKeySpace(scheme);
        assertThat(keySpace).isEqualTo("/registry/test.halo.run/fakes");
    }

    @Test
    void getIndexSpecs() {
        assertThatThrownBy(() -> indexSpecRegistry.getIndexSpecs(scheme))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("No index specs found for extension type: ");

        indexSpecRegistry.indexFor(scheme);
        var specs = indexSpecRegistry.getIndexSpecs(scheme);
        assertThat(specs).isNotNull();
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @GVK(group = "test.halo.run", version = "v1", kind = "FakeExtension", plural = "fakes",
        singular = "fake")
    static class FakeExtension extends AbstractExtension {
        Set<String> tags;
    }
}
