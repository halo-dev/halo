package run.halo.app.extension.index;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.Test;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * Tests for {@link IndexEntryContainer}.
 *
 * @author guqing
 * @since 2.12.0
 */
class IndexEntryContainerTest {

    @Test
    void add() {
        IndexEntryContainer indexEntry = new IndexEntryContainer();
        var spec = PrimaryKeySpecUtils.primaryKeyIndexSpec(FakeExtension.class);
        var descriptor = new IndexDescriptor(spec);
        var entry = new IndexEntryImpl(descriptor);
        indexEntry.add(entry);
        assertThat(indexEntry.contains(descriptor)).isTrue();

        assertThatThrownBy(() -> indexEntry.add(entry))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Index entry already exists for " + descriptor);
    }

    @Test
    void remove() {
        IndexEntryContainer indexEntry = new IndexEntryContainer();
        var spec = PrimaryKeySpecUtils.primaryKeyIndexSpec(FakeExtension.class);
        var descriptor = new IndexDescriptor(spec);
        var entry = new IndexEntryImpl(descriptor);
        indexEntry.add(entry);
        assertThat(indexEntry.contains(descriptor)).isTrue();
        assertThat(indexEntry.size()).isEqualTo(1);

        indexEntry.remove(descriptor);
        assertThat(indexEntry.contains(descriptor)).isFalse();
        assertThat(indexEntry.size()).isEqualTo(0);
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @GVK(group = "test", version = "v1", kind = "FakeExtension", plural = "fakes",
        singular = "fake")
    static class FakeExtension extends AbstractExtension {

    }
}
