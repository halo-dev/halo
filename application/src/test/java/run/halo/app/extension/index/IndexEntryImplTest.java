package run.halo.app.extension.index;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link IndexEntryImpl}.
 *
 * @author guqing
 * @since 2.12.0
 */
@ExtendWith(MockitoExtension.class)
class IndexEntryImplTest {

    @Test
    void add() {
        var spec =
            PrimaryKeySpecUtils.primaryKeyIndexSpec(IndexEntryContainerTest.FakeExtension.class);
        var descriptor = new IndexDescriptor(spec);
        var entry = new IndexEntryImpl(descriptor);
        entry.addEntry(List.of("slug-1"), "fake-name-1");
        assertThat(entry.indexedKeys()).containsExactly("slug-1");
    }

    @Test
    void remove() {
        var spec =
            PrimaryKeySpecUtils.primaryKeyIndexSpec(IndexEntryContainerTest.FakeExtension.class);
        var descriptor = new IndexDescriptor(spec);
        var entry = new IndexEntryImpl(descriptor);
        entry.addEntry(List.of("slug-1"), "fake-name-1");
        assertThat(entry.indexedKeys()).containsExactly("slug-1");
        assertThat(entry.entries()).hasSize(1);

        entry.removeEntry("slug-1", "fake-name-1");
        assertThat(entry.indexedKeys()).isEmpty();
        assertThat(entry.entries()).isEmpty();
    }

    @Test
    void removeByIndex() {
        var spec =
            PrimaryKeySpecUtils.primaryKeyIndexSpec(IndexEntryContainerTest.FakeExtension.class);
        var descriptor = new IndexDescriptor(spec);
        var entry = new IndexEntryImpl(descriptor);
        entry.addEntry(List.of("slug-1", "slug-2"), "fake-name-1");
        assertThat(entry.indexedKeys()).containsExactly("slug-1", "slug-2");
        assertThat(entry.entries()).hasSize(2);

        entry.remove("fake-name-1");
        assertThat(entry.indexedKeys()).isEmpty();
        assertThat(entry.entries()).isEmpty();
    }

    @Test
    void getByIndexKey() {
        var spec =
            PrimaryKeySpecUtils.primaryKeyIndexSpec(IndexEntryContainerTest.FakeExtension.class);
        var descriptor = new IndexDescriptor(spec);
        var entry = new IndexEntryImpl(descriptor);
        entry.addEntry(List.of("slug-1", "slug-2"), "fake-name-1");
        assertThat(entry.indexedKeys()).containsExactly("slug-1", "slug-2");
        assertThat(entry.entries()).hasSize(2);

        assertThat(entry.getByIndexKey("slug-1")).isEqualTo(List.of("fake-name-1"));
    }

    @Test
    void keyOrder() {
        var spec =
            PrimaryKeySpecUtils.primaryKeyIndexSpec(IndexEntryContainerTest.FakeExtension.class);
        spec.setOrder(IndexSpec.OrderType.DESC);
        var descriptor = new IndexDescriptor(spec);
        var entry = new IndexEntryImpl(descriptor);
        entry.addEntry(List.of("slug-1", "slug-2"), "fake-name-1");
        entry.addEntry(List.of("slug-3"), "fake-name-2");
        entry.addEntry(List.of("slug-4"), "fake-name-3");
        entry.addEntry(List.of("slug-5"), "fake-name-1");
        assertThat(entry.entries())
            .containsSequence(
                Map.entry("slug-5", "fake-name-1"),
                Map.entry("slug-4", "fake-name-3"),
                Map.entry("slug-3", "fake-name-2"),
                Map.entry("slug-2", "fake-name-1"),
                Map.entry("slug-1", "fake-name-1"));

        assertThat(entry.indexedKeys()).containsSequence("slug-4", "slug-3", "slug-2", "slug-1");


        spec.setOrder(IndexSpec.OrderType.ASC);
        var descriptor2 = new IndexDescriptor(spec);
        var entry2 = new IndexEntryImpl(descriptor2);
        entry2.addEntry(List.of("slug-1", "slug-2"), "fake-name-1");
        entry2.addEntry(List.of("slug-3"), "fake-name-2");
        entry2.addEntry(List.of("slug-4"), "fake-name-3");
        assertThat(entry2.entries())
            .containsSequence(Map.entry("slug-1", "fake-name-1"),
                Map.entry("slug-2", "fake-name-1"),
                Map.entry("slug-3", "fake-name-2"),
                Map.entry("slug-4", "fake-name-3"));
        assertThat(entry2.indexedKeys()).containsSequence("slug-1", "slug-2", "slug-3", "slug-4");
    }
}
