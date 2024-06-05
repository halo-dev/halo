package run.halo.app.extension.index;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static run.halo.app.extension.index.query.IndexViewDataSet.createCommentIndexView;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.extension.index.query.IndexViewDataSet;
import run.halo.app.extension.index.query.QueryIndexView;

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
    void getObjectIdsTest() {
        var spec =
            PrimaryKeySpecUtils.primaryKeyIndexSpec(IndexEntryContainerTest.FakeExtension.class);
        var descriptor = new IndexDescriptor(spec);
        var entry = new IndexEntryImpl(descriptor);
        entry.addEntry(List.of("slug-1", "slug-2"), "fake-name-1");
        assertThat(entry.indexedKeys()).containsExactly("slug-1", "slug-2");
        assertThat(entry.entries()).hasSize(2);

        assertThat(entry.getObjectNamesBy("slug-1")).isEqualTo(List.of("fake-name-1"));
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

    @Test
    void getIdPositionMapTest() {
        var indexView = createCommentIndexView();
        var topIndexEntry = prepareForPositionMapTest(indexView, "spec.top");
        var topIndexEntryFromView = indexView.getIndexEntry("spec.top");
        assertThat(topIndexEntry.getIdPositionMap())
            .isEqualTo(topIndexEntryFromView.getIdPositionMap());

        var creationTimeIndexEntry = prepareForPositionMapTest(indexView, "spec.creationTime");
        var creationTimeIndexEntryFromView = indexView.getIndexEntry("spec.creationTime");
        assertThat(creationTimeIndexEntry.getIdPositionMap())
            .isEqualTo(creationTimeIndexEntryFromView.getIdPositionMap());

        var priorityIndexEntry = prepareForPositionMapTest(indexView, "spec.priority");
        var priorityIndexEntryFromView = indexView.getIndexEntry("spec.priority");
        assertThat(priorityIndexEntry.getIdPositionMap())
            .isEqualTo(priorityIndexEntryFromView.getIdPositionMap());
    }

    IndexEntry prepareForPositionMapTest(QueryIndexView indexView, String property) {
        var indexSpec = mock(IndexSpec.class);
        var descriptor = mock(IndexDescriptor.class);
        when(descriptor.getSpec()).thenReturn(indexSpec);
        var indexEntry = new IndexEntryImpl(descriptor);

        var indexEntryFromView = indexView.getIndexEntry(property);
        var sortedEntries = IndexViewDataSet.sortEntries(indexEntryFromView.entries());

        var spyIndexEntry = spy(indexEntry);

        doReturn(IndexViewDataSet.toKeyObjectMap(sortedEntries))
            .when(spyIndexEntry).getKeyObjectMap();

        return spyIndexEntry;
    }
}
