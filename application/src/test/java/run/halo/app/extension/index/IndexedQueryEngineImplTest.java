package run.halo.app.extension.index;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static run.halo.app.extension.index.query.IndexViewDataSet.pileForIndexer;
import static run.halo.app.extension.index.query.QueryFactory.equal;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
    void retrieve() {
        var spyIndexedQueryEngine = spy(indexedQueryEngine);
        doReturn(List.of("object1", "object2", "object3")).when(spyIndexedQueryEngine)
            .doRetrieve(any(), any(), eq(Sort.unsorted()));

        var gvk = GroupVersionKind.fromExtension(DemoExtension.class);

        var pageRequest = mock(PageRequest.class);
        when(pageRequest.getPageNumber()).thenReturn(1);
        when(pageRequest.getPageSize()).thenReturn(2);
        when(pageRequest.getSort()).thenReturn(Sort.unsorted());

        var result = spyIndexedQueryEngine.retrieve(gvk, new ListOptions(), pageRequest);
        assertThat(result.getItems()).containsExactly("object1", "object2");
        assertThat(result.getTotal()).isEqualTo(3);

        verify(spyIndexedQueryEngine).doRetrieve(eq(gvk), any(), eq(Sort.unsorted()));
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

        var result = spyIndexedQueryEngine.retrieveAll(gvk, new ListOptions(), Sort.unsorted());
        assertThat(result).isEmpty();

        verify(spyIndexedQueryEngine).doRetrieve(eq(gvk), any(), eq(Sort.unsorted()));
    }

    @Test
    void doRetrieve() {
        var indexer = mock(Indexer.class);

        var gvk = GroupVersionKind.fromExtension(DemoExtension.class);

        when(indexerFactory.getIndexer(eq(gvk))).thenReturn(indexer);

        pileForIndexer(indexer, PrimaryKeySpecUtils.PRIMARY_INDEX_NAME, List.of(
            Map.entry("object1", "object1"),
            Map.entry("object2", "object2"),
            Map.entry("object3", "object3")
        ));

        pileForIndexer(indexer, LabelIndexSpecUtils.LABEL_PATH, List.of(
            Map.entry("key1=value1", "object1"),
            Map.entry("key2=value2", "object1"),
            Map.entry("key1=value1", "object2"),
            Map.entry("key2=value2", "object2"),
            Map.entry("key1=value1", "object3")
        ));

        pileForIndexer(indexer, "slug", List.of(
            Map.entry("slug1", "object1"),
            Map.entry("slug2", "object2")
        ));

        var listOptions = new ListOptions();
        listOptions.setLabelSelector(LabelSelector.builder()
            .eq("key1", "value1").build());
        listOptions.setFieldSelector(FieldSelector.of(equal("slug", "slug1")));

        var result = indexedQueryEngine.doRetrieve(gvk, listOptions, Sort.unsorted());
        assertThat(result).containsExactly("object1");
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @GVK(group = "test", version = "v1", kind = "demo", plural = "demos", singular = "demo")
    static class DemoExtension extends AbstractExtension {

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

            var indexer = mock(Indexer.class);
            when(indexer.getIndexEntry(eq(LabelIndexSpecUtils.LABEL_PATH)))
                .thenReturn(indexEntryMock);
            var nameIndexEntry = mock(IndexEntry.class);
            when(indexer.getIndexEntry(eq(PrimaryKeySpecUtils.PRIMARY_INDEX_NAME)))
                .thenReturn(nameIndexEntry);
            when(nameIndexEntry.entries()).thenReturn(List.of(Map.entry("object1", "object1"),
                Map.entry("object2", "object2"), Map.entry("object3", "object3")));
            // Test
            assertThat(indexedQueryEngine.retrieveForLabelMatchers(indexer, labelMatchers))
                .containsSequence("object1", "object2");
        }

        @Test
        void testRetrieveForLabelMatchersNoMatch() {
            IndexEntry indexEntryMock = mock(IndexEntry.class);
            // Setup mock behavior
            when(indexEntryMock.entries())
                .thenReturn(List.of(Map.entry("key1=value1", "object1"),
                    Map.entry("key2=value2", "object2"),
                    Map.entry("key1=value3", "object3"))
                );

            var matcher1 = EqualityMatcher.equal("key3", "value3");
            List<SelectorMatcher> labelMatchers = List.of(matcher1);

            var indexer = mock(Indexer.class);
            when(indexer.getIndexEntry(eq(LabelIndexSpecUtils.LABEL_PATH)))
                .thenReturn(indexEntryMock);
            var nameIndexEntry = mock(IndexEntry.class);
            when(indexer.getIndexEntry(eq(PrimaryKeySpecUtils.PRIMARY_INDEX_NAME)))
                .thenReturn(nameIndexEntry);
            when(nameIndexEntry.entries()).thenReturn(List.of(Map.entry("object1", "object1"),
                Map.entry("object2", "object2"), Map.entry("object3", "object3")));
            // Test
            assertThat(
                indexedQueryEngine.retrieveForLabelMatchers(indexer, labelMatchers)).isEmpty();
        }
    }
}
