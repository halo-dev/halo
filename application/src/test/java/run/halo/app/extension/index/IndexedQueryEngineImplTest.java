package run.halo.app.extension.index;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static run.halo.app.extension.index.query.QueryFactory.equal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.PageRequest;
import run.halo.app.extension.router.selector.EqualityMatcher;
import run.halo.app.extension.router.selector.FieldSelector;
import run.halo.app.extension.router.selector.LabelSelector;
import run.halo.app.extension.router.selector.SelectorMatcher;

/**
 * Tests for {@link IndexedQueryEngineImpl}.
 *
 * @author guqing
 * @since 2.12.0
 */
@ExtendWith(MockitoExtension.class)
class IndexedQueryEngineImplTest {

    @Mock
    private IndexerFactory indexerFactory;

    @InjectMocks
    private IndexedQueryEngineImpl indexedQueryEngine;

    @Test
    void getIndexEntry() {
        Map<String, IndexEntry> indexMap = new HashMap<>();
        assertThatThrownBy(() -> IndexedQueryEngineImpl.getIndexEntry("field1", indexMap))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(
                "No index found for fieldPath: field1, make sure you have created an index for "
                    + "this field.");
    }

    @Test
    void retrieve() {
        var spyIndexedQueryEngine = spy(indexedQueryEngine);
        doReturn(List.of("object1", "object2", "object3")).when(spyIndexedQueryEngine)
            .doRetrieve(any(), any(), eq(Sort.unsorted()));

        var gvk = GroupVersionKind.fromExtension(DemoExtension.class);

        when(indexerFactory.getIndexer(eq(gvk))).thenReturn(mock(Indexer.class));

        var pageRequest = mock(PageRequest.class);
        when(pageRequest.getPageNumber()).thenReturn(1);
        when(pageRequest.getPageSize()).thenReturn(2);
        when(pageRequest.getSort()).thenReturn(Sort.unsorted());

        var result = spyIndexedQueryEngine.retrieve(gvk, new ListOptions(), pageRequest);
        assertThat(result.getItems()).containsExactly("object1", "object2");
        assertThat(result.getTotal()).isEqualTo(3);

        verify(spyIndexedQueryEngine).doRetrieve(any(), any(), eq(Sort.unsorted()));
        verify(indexerFactory).getIndexer(eq(gvk));
        verify(pageRequest, times(2)).getPageNumber();
        verify(pageRequest, times(2)).getPageSize();
        verify(pageRequest).getSort();
    }

    @Test
    void retrieveAll() {
        var spyIndexedQueryEngine = spy(indexedQueryEngine);
        doReturn(List.of()).when(spyIndexedQueryEngine)
            .doRetrieve(any(), any(), eq(Sort.unsorted()));

        var gvk = GroupVersionKind.fromExtension(DemoExtension.class);

        when(indexerFactory.getIndexer(eq(gvk))).thenReturn(mock(Indexer.class));

        var result = spyIndexedQueryEngine.retrieveAll(gvk, new ListOptions());
        assertThat(result).isEmpty();

        verify(spyIndexedQueryEngine).doRetrieve(any(), any(), eq(Sort.unsorted()));
        verify(indexerFactory).getIndexer(eq(gvk));
    }

    @Test
    void doRetrieve() {
        var indexer = mock(Indexer.class);
        var labelEntry = mock(IndexEntry.class);
        var fieldSlugEntry = mock(IndexEntry.class);
        var nameEntry = mock(IndexEntry.class);
        when(indexer.readyIndexesIterator()).thenReturn(
            List.of(labelEntry, nameEntry, fieldSlugEntry).iterator());

        when(nameEntry.getIndexDescriptor())
            .thenReturn(new IndexDescriptor(
                PrimaryKeySpecUtils.primaryKeyIndexSpec(DemoExtension.class)));
        when(nameEntry.indexedKeys()).thenReturn(Set.of("object1", "object2", "object3"));

        when(fieldSlugEntry.getIndexDescriptor())
            .thenReturn(new IndexDescriptor(new IndexSpec()
                .setName("slug")
                .setOrder(IndexSpec.OrderType.ASC)));
        when((fieldSlugEntry.immutableEntries())).thenReturn(
            List.of(Map.entry("slug1", "object1"), Map.entry("slug2", "object2")));

        when(labelEntry.getIndexDescriptor())
            .thenReturn(
                new IndexDescriptor(LabelIndexSpecUtils.labelIndexSpec(DemoExtension.class)));
        when(labelEntry.entries()).thenReturn(List.of(
            Map.entry("key1=value1", "object1"),
            Map.entry("key2=value2", "object1"),
            Map.entry("key1=value1", "object2"),
            Map.entry("key2=value2", "object2"),
            Map.entry("key1=value1", "object3")
        ));
        var listOptions = new ListOptions();
        listOptions.setLabelSelector(LabelSelector.builder()
            .eq("key1", "value1").build());
        listOptions.setFieldSelector(FieldSelector.of(equal("slug", "slug1")));
        var result = indexedQueryEngine.doRetrieve(indexer, listOptions, Sort.unsorted());
        assertThat(result).containsExactly("object1");
    }

    @Test
    void intersection() {
        var list1 = Arrays.asList(1, 2, 3, 4);
        var list2 = Arrays.asList(3, 4, 5, 6);
        var expected = Arrays.asList(3, 4);
        assertThat(IndexedQueryEngineImpl.intersection(list1, list2)).isEqualTo(expected);

        list1 = Arrays.asList(1, 2, 3);
        list2 = Arrays.asList(4, 5, 6);
        expected = List.of();
        assertThat(IndexedQueryEngineImpl.intersection(list1, list2)).isEqualTo(expected);

        list1 = List.of();
        list2 = Arrays.asList(1, 2, 3);
        expected = List.of();
        assertThat(IndexedQueryEngineImpl.intersection(list1, list2)).isEqualTo(expected);

        list1 = Arrays.asList(1, 2, 3);
        list2 = List.of();
        expected = List.of();
        assertThat(IndexedQueryEngineImpl.intersection(list1, list2)).isEqualTo(expected);

        list1 = List.of();
        list2 = List.of();
        expected = List.of();
        assertThat(IndexedQueryEngineImpl.intersection(list1, list2)).isEqualTo(expected);

        list1 = Arrays.asList(1, 2, 2, 3);
        list2 = Arrays.asList(2, 3, 3, 4);
        expected = Arrays.asList(2, 3);
        assertThat(IndexedQueryEngineImpl.intersection(list1, list2)).isEqualTo(expected);
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class LabelMatcherTest {
        @InjectMocks
        private IndexedQueryEngineImpl indexedQueryEngine;

        @Test
        void testRetrieveForLabelMatchers() {
            // Setup mocks
            IndexEntry indexEntryMock = mock(IndexEntry.class);
            Map<String, IndexEntry> fieldPathEntryMap =
                Map.of(LabelIndexSpecUtils.LABEL_PATH, indexEntryMock);
            List<String> allMetadataNames = Arrays.asList("object1", "object2", "object3");

            // Setup mock behavior
            when(indexEntryMock.entries())
                .thenReturn(List.of(Map.entry("key1=value1", "object1"),
                    Map.entry("key2=value2", "object1"),
                    Map.entry("key1=value1", "object2"),
                    Map.entry("key2=value2", "object2"),
                    Map.entry("key1=value1", "object3")));

            var matcher1 = EqualityMatcher.equal("key1", "value1");
            var matcher2 = EqualityMatcher.equal("key2", "value2");

            List<SelectorMatcher> labelMatchers = Arrays.asList(matcher1, matcher2);

            // Expected results
            List<String> expected = Arrays.asList("object1", "object2");

            // Test
            assertThat(indexedQueryEngine.retrieveForLabelMatchers(labelMatchers, fieldPathEntryMap,
                allMetadataNames))
                .isEqualTo(expected);
        }

        @Test
        void testRetrieveForLabelMatchersNoMatch() {
            // Setup mocks
            IndexEntry indexEntryMock = mock(IndexEntry.class);
            Map<String, IndexEntry> fieldPathEntryMap =
                Map.of(LabelIndexSpecUtils.LABEL_PATH, indexEntryMock);
            List<String> allMetadataNames = Arrays.asList("object1", "object2", "object3");

            // Setup mock behavior
            when(indexEntryMock.entries())
                .thenReturn(List.of(Map.entry("key1=value1", "object1"),
                    Map.entry("key2=value2", "object2"),
                    Map.entry("key1=value3", "object3"))
                );

            var matcher1 = EqualityMatcher.equal("key3", "value3");
            List<SelectorMatcher> labelMatchers = List.of(matcher1);

            // Expected results
            List<String> expected = List.of();

            // Test
            assertThat(indexedQueryEngine.retrieveForLabelMatchers(labelMatchers, fieldPathEntryMap,
                allMetadataNames))
                .isEqualTo(expected);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @GVK(group = "test", version = "v1", kind = "demo", plural = "demos", singular = "demo")
    static class DemoExtension extends AbstractExtension {

    }
}
