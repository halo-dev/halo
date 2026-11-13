package run.halo.app.extension.index;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Sort;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.index.query.Queries;

@ExtendWith(MockitoExtension.class)
class DefaultIndexEngineTest {

    @Mock
    ConversionService conversionService;

    @Mock
    IndicesManager indicesManager;

    @Mock
    Indices<Fake> indices;

    @Mock
    SingleValueIndex<Fake, String> singleValueIndex;

    @Mock
    SingleValueIndex<Fake, Instant> creationTimestampIndex;

    @Mock
    MultiValueIndex<Fake, String> multiValueIndex;

    @InjectMocks
    DefaultIndexEngine engine;

    @BeforeEach
    void setUp() {
        lenient().when(indicesManager.get(Fake.class)).thenReturn(indices);
        engine.setIndicesManager(indicesManager);
        assertEquals(indicesManager, engine.getIndicesManager());
    }

    @Test
    void shouldDestroyIndicesOnClose() throws Exception {
        engine.destroy();
        verify(indicesManager).close();
    }

    @Test
    void shouldInsertExtensions() {
        var fake = createFake("fake");

        engine.insert(List.of(fake));

        verify(indices).insert(fake);
    }

    @Test
    void shouldUpdateExtensions() {
        var fake = createFake("fake");

        engine.update(List.of(fake));

        verify(indices).update(fake);
    }

    @Test
    void shouldDeleteExtensions() {
        var fake = createFake("fake");

        engine.delete(List.of(fake));

        verify(indices).delete(fake);
    }

    @Test
    void shouldRetrieveWithConditionsAndPage() {
        var options =
                ListOptions.builder().andQuery(Queries.all("metadata.name")).build();
        var page = PageRequestImpl.of(2, 2);
        when(indices.<String>getIndex("metadata.name")).thenReturn(singleValueIndex);
        when(singleValueIndex.all()).thenReturn(Set.of("1", "2", "3", "4", "5", "6"));
        var result = engine.retrieve(Fake.class, options, page);
        assertEquals(6, result.getTotal());
        assertEquals(List.of("3", "4"), result.getItems());
    }

    @Test
    void shouldRetrieveWithConditionsAndFirstPage() {
        when(indices.<String>getIndex("metadata.name")).thenReturn(singleValueIndex);
        when(singleValueIndex.all()).thenReturn(Set.of("1", "2", "3", "4", "5", "6"));
        var options =
                ListOptions.builder().andQuery(Queries.all("metadata.name")).build();
        var page = PageRequestImpl.of(1, 4);
        var result = engine.retrieve(Fake.class, options, page);
        assertEquals(6, result.getTotal());
        assertEquals(List.of("1", "2", "3", "4"), result.getItems());
    }

    @Test
    void shouldRetrieveWithConditionsAndLastPage() {
        when(indices.<String>getIndex("metadata.name")).thenReturn(singleValueIndex);
        when(singleValueIndex.all()).thenReturn(Set.of("1", "2", "3", "4", "5", "6"));
        var options =
                ListOptions.builder().andQuery(Queries.all("metadata.name")).build();
        var page = PageRequestImpl.of(2, 4);
        var result = engine.retrieve(Fake.class, options, page);
        assertEquals(6, result.getTotal());
        assertEquals(List.of("5", "6"), result.getItems());
    }

    @Test
    void shouldRetrieveWithConditionsAndExceededPage() {
        when(indices.<String>getIndex("metadata.name")).thenReturn(singleValueIndex);
        when(singleValueIndex.all()).thenReturn(Set.of("1", "2", "3", "4", "5", "6"));
        var options =
                ListOptions.builder().andQuery(Queries.all("metadata.name")).build();
        var page = PageRequestImpl.of(4, 2);
        var result = engine.retrieve(Fake.class, options, page);
        assertEquals(6, result.getTotal());
        assertEquals(List.of(), result.getItems());
    }

    @Test
    void shouldRetrieveWithMultipleSortOrders() {
        when(indices.<String>getIndex("metadata.name")).thenReturn(singleValueIndex);
        when(indices.<Instant>getIndex("metadata.creationTimestamp")).thenReturn(creationTimestampIndex);
        when(singleValueIndex.all()).thenReturn(Set.of("a", "b", "c"));
        when(singleValueIndex.getKey("a")).thenReturn("a");
        when(singleValueIndex.getKey("b")).thenReturn("b");
        when(singleValueIndex.getKey("c")).thenReturn("c");
        when(creationTimestampIndex.getKey("a")).thenReturn(Instant.ofEpochSecond(2));
        when(creationTimestampIndex.getKey("b")).thenReturn(Instant.ofEpochSecond(1));
        when(creationTimestampIndex.getKey("c")).thenReturn(Instant.ofEpochSecond(2));

        var options =
                ListOptions.builder().andQuery(Queries.all("metadata.name")).build();
        var sort = Sort.by(DESC, "metadata.creationTimestamp").and(Sort.by(ASC, "metadata.name"));
        var result = engine.retrieve(Fake.class, options, PageRequestImpl.of(1, 3, sort));

        assertEquals(List.of("a", "c", "b"), result.getItems());
        // sort keys are resolved once per candidate instead of once per comparison
        verify(singleValueIndex, times(3)).getKey(anyString());
        verify(creationTimestampIndex, times(3)).getKey(anyString());
    }

    @Test
    void shouldPaginateStablyWhenSortKeysAreEqual() {
        when(indices.<String>getIndex("metadata.name")).thenReturn(singleValueIndex);
        when(indices.<Instant>getIndex("metadata.creationTimestamp")).thenReturn(creationTimestampIndex);
        when(singleValueIndex.all()).thenReturn(Set.of("a", "b", "c", "d", "e", "f"));
        when(creationTimestampIndex.getKey(anyString())).thenReturn(Instant.EPOCH);

        var options =
                ListOptions.builder().andQuery(Queries.all("metadata.name")).build();
        var sort = Sort.by(ASC, "metadata.creationTimestamp");
        var items = new ArrayList<String>();
        for (var page = 1; page <= 3; page++) {
            var result = engine.retrieve(Fake.class, options, PageRequestImpl.of(page, 2, sort));
            items.addAll(result.getItems());
        }

        assertEquals(List.of("a", "b", "c", "d", "e", "f"), items);
    }

    @Test
    void shouldSortByMultiValueIndexAndTreatEmptyKeysAsNull() {
        when(indices.<String>getIndex("metadata.name")).thenReturn(singleValueIndex);
        when(indices.<String>getIndex("spec.tags")).thenReturn(multiValueIndex);
        when(singleValueIndex.all()).thenReturn(Set.of("a", "b", "c"));
        when(multiValueIndex.getKeys("a")).thenReturn(Set.of("z"));
        when(multiValueIndex.getKeys("b")).thenReturn(Set.of());
        when(multiValueIndex.getKeys("c")).thenReturn(Set.of("m"));

        var options =
                ListOptions.builder().andQuery(Queries.all("metadata.name")).build();
        var result = engine.retrieveAll(Fake.class, options, Sort.by(ASC, "spec.tags"));

        assertEquals(
                List.of("b", "c", "a"),
                StreamSupport.stream(result.spliterator(), false).toList());
        verify(multiValueIndex, times(3)).getKeys(anyString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"page", "all", "topN"})
    void shouldSortWhenCandidateIsDeletedBeforeReadingSortKeys(String retrieval) throws Exception {
        try (var manager = new DefaultIndicesManager()) {
            manager.add(
                    Fake.class,
                    List.of(IndexSpecs.<Fake, String>multi("stringValues", String.class)
                            .indexFunc(Fake::getStringValues)
                            .build()));
            var realIndices = spy(manager.get(Fake.class));
            when(indicesManager.get(Fake.class)).thenReturn(realIndices);
            var deleted = createFake("a");
            deleted.getMetadata().setCreationTimestamp(Instant.EPOCH);
            deleted.setStringValues(Set.of("z"));
            var retained = createFake("b");
            retained.getMetadata().setCreationTimestamp(Instant.EPOCH);
            retained.setStringValues(Set.of("m"));
            engine.insert(List.of(deleted, retained));

            // Sort indexes are resolved after the query has copied the candidate names.
            doAnswer(invocation -> {
                        if ("stringValues".equals(invocation.getArgument(0))) {
                            realIndices.delete(deleted);
                        }
                        return invocation.callRealMethod();
                    })
                    .when(realIndices)
                    .getIndex(anyString());
            var options =
                    ListOptions.builder().andQuery(Queries.all("metadata.name")).build();
            var sort = Sort.by(ASC, "metadata.name", "stringValues");

            Iterable<String> result =
                    switch (retrieval) {
                        case "page" ->
                            engine.retrieve(Fake.class, options, PageRequestImpl.of(1, 2, sort))
                                    .getItems();
                        case "all" -> engine.retrieveAll(Fake.class, options, sort);
                        case "topN" -> engine.retrieveTopN(Fake.class, options, sort, 2);
                        default -> throw new IllegalArgumentException(retrieval);
                    };

            assertEquals(
                    List.of("a", "b"),
                    StreamSupport.stream(result.spliterator(), false).toList());
            verify(realIndices).delete(deleted);
            assertEquals(1, engine.count(Fake.class, options));
        }
    }

    @Test
    void shouldRetrieveSortedPageWhenOffsetExceedsTopNThreshold() {
        var allResult = IntStream.rangeClosed(1, 2001)
                .boxed()
                .map(o -> String.format("%04d", o))
                .collect(Collectors.toSet());
        when(indices.<String>getIndex("metadata.name")).thenReturn(singleValueIndex);
        when(singleValueIndex.all()).thenReturn(allResult);
        when(singleValueIndex.getKey(anyString())).thenAnswer(invocation -> invocation.getArgument(0));

        var options =
                ListOptions.builder().andQuery(Queries.all("metadata.name")).build();
        var result = engine.retrieve(Fake.class, options, PageRequestImpl.of(2, 1000, Sort.by(ASC, "metadata.name")));

        assertEquals(2001, result.getTotal());
        assertEquals(1000, result.getItems().size());
        assertEquals("1001", result.getItems().getFirst());
        assertEquals("2000", result.getItems().getLast());
    }

    @Test
    void shouldRetrieveTopNWithoutSort() {
        when(indices.<String>getIndex("metadata.name")).thenReturn(singleValueIndex);
        when(singleValueIndex.all()).thenReturn(Set.of("3", "1", "2"));

        var options =
                ListOptions.builder().andQuery(Queries.all("metadata.name")).build();
        var result = engine.retrieveTopN(Fake.class, options, Sort.unsorted(), 2);

        assertEquals(
                List.of("1", "2"),
                StreamSupport.stream(result.spliterator(), false).toList());
    }

    @Test
    void shouldRetrieveAllWithConditionsAndNonPositiveSize() {
        var allResult = IntStream.rangeClosed(1, 1001)
                .boxed()
                .map(o -> String.format("%04d", o))
                .collect(Collectors.toSet());
        when(indices.<String>getIndex("metadata.name")).thenReturn(singleValueIndex);
        when(singleValueIndex.all()).thenReturn(allResult);
        var options =
                ListOptions.builder().andQuery(Queries.all("metadata.name")).build();
        var page = PageRequestImpl.of(1, 0);
        var result = engine.retrieve(Fake.class, options, page);
        assertEquals(1001, result.getTotal());
        assertEquals(1000, result.getItems().size());
        assertEquals("0001", result.getItems().getFirst());
        assertEquals("1000", result.getItems().getLast());
    }

    @Test
    void shouldRetrieveAllWithConditions() {
        var options =
                ListOptions.builder().andQuery(Queries.all("metadata.name")).build();
        when(indices.<String>getIndex("metadata.name")).thenReturn(singleValueIndex);
        when(singleValueIndex.all()).thenReturn(Set.of("1", "2", "3"));
        when(singleValueIndex.getKey("1")).thenReturn("1");
        when(singleValueIndex.getKey("2")).thenReturn("2");
        when(singleValueIndex.getKey("3")).thenReturn("3");
        var result = engine.retrieveAll(Fake.class, options, Sort.by(DESC, "metadata.name"));
        assertEquals(
                List.of("3", "2", "1"),
                StreamSupport.stream(result.spliterator(), false).toList());
    }

    @Test
    void shouldRetrieveTopNWithConditions() {
        var options =
                ListOptions.builder().andQuery(Queries.all("metadata.name")).build();
        when(indices.<String>getIndex("metadata.name")).thenReturn(singleValueIndex);
        when(singleValueIndex.all()).thenReturn(Set.of("1", "2", "3", "4", "5"));
        when(singleValueIndex.getKey("1")).thenReturn("1");
        when(singleValueIndex.getKey("2")).thenReturn("2");
        when(singleValueIndex.getKey("3")).thenReturn("3");
        when(singleValueIndex.getKey("4")).thenReturn("4");
        when(singleValueIndex.getKey("5")).thenReturn("5");
        var result = engine.retrieveTopN(Fake.class, options, Sort.by(DESC, "metadata.name"), 3);
        assertEquals(
                List.of("5", "4", "3"),
                StreamSupport.stream(result.spliterator(), false).toList());

        result = engine.retrieveTopN(Fake.class, options, Sort.by(ASC, "metadata.name"), 2);
        assertEquals(
                List.of("1", "2"),
                StreamSupport.stream(result.spliterator(), false).toList());
    }

    @Test
    void shouldCountWithConditions() {
        var options =
                ListOptions.builder().andQuery(Queries.all("metadata.name")).build();
        when(indices.<String>getIndex("metadata.name")).thenReturn(singleValueIndex);
        when(singleValueIndex.all()).thenReturn(Set.of("1", "2", "3", "4"));
        var count = engine.count(Fake.class, options);
        assertEquals(4L, count);
    }

    Fake createFake(String name) {
        var fake = new Fake();
        fake.setMetadata(new Metadata());
        fake.getMetadata().setName(name);
        return fake;
    }
}
