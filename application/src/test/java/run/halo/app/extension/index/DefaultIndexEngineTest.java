package run.halo.app.extension.index;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
        var options = ListOptions.builder()
            .andQuery(Queries.all("metadata.name"))
            .build();
        var page = PageRequestImpl.of(2, 2);
        when(indices.<String>getIndex("metadata.name")).thenReturn(singleValueIndex);
        when(singleValueIndex.all()).thenReturn(Set.of("1", "2", "3", "4", "5", "6"));
        var result = engine.retrieve(Fake.class, options, page);
        assertEquals(6, result.getTotal());
        assertEquals(List.of("3", "4"), result.getItems());
    }

    @Test
    void shouldRetrieveAllWithConditions() {
        var options = ListOptions.builder()
            .andQuery(Queries.all("metadata.name"))
            .build();
        when(indices.<String>getIndex("metadata.name")).thenReturn(singleValueIndex);
        when(singleValueIndex.all()).thenReturn(Set.of("1", "2", "3"));
        when(singleValueIndex.getKey("1")).thenReturn("1");
        when(singleValueIndex.getKey("2")).thenReturn("2");
        when(singleValueIndex.getKey("3")).thenReturn("3");
        var result = engine.retrieveAll(Fake.class, options, Sort.by(DESC, "metadata.name"));
        assertEquals(List.of("3", "2", "1"),
            StreamSupport.stream(result.spliterator(), false).toList()
        );
    }

    @Test
    void shouldRetrieveTopNWithConditions() {
        var options = ListOptions.builder()
            .andQuery(Queries.all("metadata.name"))
            .build();
        when(indices.<String>getIndex("metadata.name")).thenReturn(singleValueIndex);
        when(singleValueIndex.all()).thenReturn(Set.of("1", "2", "3", "4", "5"));
        when(singleValueIndex.getKey("1")).thenReturn("1");
        when(singleValueIndex.getKey("2")).thenReturn("2");
        when(singleValueIndex.getKey("3")).thenReturn("3");
        when(singleValueIndex.getKey("4")).thenReturn("4");
        when(singleValueIndex.getKey("5")).thenReturn("5");
        var result = engine.retrieveTopN(Fake.class, options, Sort.by(DESC, "metadata.name"), 3);
        assertEquals(List.of("5", "4", "3"),
            StreamSupport.stream(result.spliterator(), false).toList()
        );

        result = engine.retrieveTopN(Fake.class, options, Sort.by(ASC, "metadata.name"), 2);
        assertEquals(List.of("1", "2"), StreamSupport.stream(result.spliterator(), false).toList());
    }

    @Test
    void shouldCountWithConditions() {
        var options = ListOptions.builder()
            .andQuery(Queries.all("metadata.name"))
            .build();
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