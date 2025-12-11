package run.halo.app.extension.index;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.extension.Metadata;

@ExtendWith(MockitoExtension.class)
class DefaultIndicesTest {

    @Mock
    Index<Fake, String> index1;

    @Mock
    Index<Fake, String> index2;

    @Mock
    Index<Fake, String> index3;

    @Mock
    Index<Fake, Long> longIndex;

    DefaultIndices<Fake> indices;

    @BeforeEach
    void setUp() {
        when(index1.getName()).thenReturn("index1");
        when(index2.getName()).thenReturn("index2");
        when(index3.getName()).thenReturn("index3");
        indices = new DefaultIndices<>(List.of(index1, index2, index3));
    }

    @Test
    void shouldNotReplaceWhenDuplicateIndexNames() {
        when(longIndex.getName()).thenReturn("index1");
        indices = new DefaultIndices<>(List.of(index1, longIndex));
        // The first index should be retained
        assertEquals(index1, indices.getIndex("index1"));
    }

    @Test
    void shouldInsertCorrectly() {
        var fake = createFake("fake");

        var to = mock(TransactionalOperation.class);
        doNothing().when(to).prepare();
        doNothing().when(to).commit();

        when(index1.prepareInsert(fake)).thenReturn(to);
        when(index2.prepareInsert(fake)).thenReturn(to);
        when(index3.prepareInsert(fake)).thenReturn(to);

        indices.insert(fake);

        verify(index1).prepareInsert(fake);
        verify(index2).prepareInsert(fake);
        verify(index3).prepareInsert(fake);

        verify(to, times(3)).prepare();
        verify(to, times(3)).commit();
        verify(to, never()).rollback();
    }

    @Test
    void shouldUpdateCorrectly() {
        var fake = createFake("fake");

        var to = mock(TransactionalOperation.class);
        doNothing().when(to).prepare();
        doNothing().when(to).commit();

        when(index1.prepareUpdate(fake)).thenReturn(to);
        when(index2.prepareUpdate(fake)).thenReturn(to);
        when(index3.prepareUpdate(fake)).thenReturn(to);

        indices.update(fake);

        verify(index1).prepareUpdate(fake);
        verify(index2).prepareUpdate(fake);
        verify(index3).prepareUpdate(fake);

        verify(to, times(3)).prepare();
        verify(to, times(3)).commit();
        verify(to, never()).rollback();
    }

    @Test
    void shouldDeleteCorrectly() {
        var to = mock(TransactionalOperation.class);
        doNothing().when(to).prepare();
        doNothing().when(to).commit();

        var name = "fake";
        when(index1.prepareDelete(name)).thenReturn(to);
        when(index2.prepareDelete(name)).thenReturn(to);
        when(index3.prepareDelete(name)).thenReturn(to);

        var fake = createFake(name);
        indices.delete(fake);

        verify(index1).prepareDelete(name);
        verify(index2).prepareDelete(name);
        verify(index3).prepareDelete(name);

        verify(to, times(3)).prepare();
        verify(to, times(3)).commit();
        verify(to, never()).rollback();
    }

    @Test
    void shouldRollbackOnInsertFailure() {
        var fake = createFake("fake");

        var to1 = mock(TransactionalOperation.class);
        var to2 = mock(TransactionalOperation.class);
        var to3 = mock(TransactionalOperation.class);

        doNothing().when(to1).prepare();
        doNothing().when(to1).commit();

        doNothing().when(to2).prepare();
        // Simulate failure on second index
        doThrow(new RuntimeException("Insert failed")).when(to2).commit();

        doNothing().when(to3).prepare();

        when(index1.prepareInsert(fake)).thenReturn(to1);
        when(index2.prepareInsert(fake)).thenReturn(to2);
        when(index3.prepareInsert(fake)).thenReturn(to3);

        assertThrows(RuntimeException.class, () -> indices.insert(fake));

        verify(to1).prepare();
        verify(to1).commit();
        verify(to1).rollback();

        verify(to2).prepare();
        verify(to2).commit();
        verify(to2).rollback();

        verify(to3).prepare();
        // to3 not committed, so no rollback
        verify(to3, never()).commit();
        verify(to3).rollback();
    }

    @Test
    void shouldRollbackOnUpdateFailure() {
        var fake = createFake("fake");

        var to1 = mock(TransactionalOperation.class);
        var to2 = mock(TransactionalOperation.class);
        var to3 = mock(TransactionalOperation.class);

        doNothing().when(to1).prepare();
        doNothing().when(to1).commit();

        doNothing().when(to2).prepare();
        // Simulate failure on second index
        doThrow(new RuntimeException("Update failed")).when(to2).commit();

        doNothing().when(to3).prepare();

        when(index1.prepareUpdate(fake)).thenReturn(to1);
        when(index2.prepareUpdate(fake)).thenReturn(to2);
        when(index3.prepareUpdate(fake)).thenReturn(to3);

        assertThrows(RuntimeException.class, () -> indices.update(fake));

        verify(to1).prepare();
        verify(to1).commit();
        verify(to1).rollback();

        verify(to2).prepare();
        verify(to2).commit();
        verify(to2).rollback();

        verify(to3).prepare();
        // to3 not committed, so no rollback
        verify(to3, never()).commit();
        verify(to3).rollback();
    }

    @Test
    void shouldRollbackOnDeleteFailure() {
        var to1 = mock(TransactionalOperation.class);
        var to2 = mock(TransactionalOperation.class);
        var to3 = mock(TransactionalOperation.class);

        doNothing().when(to1).prepare();
        doNothing().when(to1).commit();

        doNothing().when(to2).prepare();
        // Simulate failure on second index
        doThrow(new RuntimeException("Delete failed")).when(to2).commit();

        doNothing().when(to3).prepare();

        var name = "fake";
        when(index1.prepareDelete(name)).thenReturn(to1);
        when(index2.prepareDelete(name)).thenReturn(to2);
        when(index3.prepareDelete(name)).thenReturn(to3);

        var fake = createFake(name);
        assertThrows(RuntimeException.class, () -> indices.delete(fake));

        verify(to1).prepare();
        verify(to1).commit();
        verify(to1).rollback();

        verify(to2).prepare();
        verify(to2).commit();
        verify(to2).rollback();

        verify(to3).prepare();
        // to3 not committed, so no rollback
        verify(to3, never()).commit();
        verify(to3).rollback();
    }

    @Test
    void shouldGetIndexCorrectly() {
        assertEquals(index1, indices.getIndex("index1"));
        assertEquals(index2, indices.getIndex("index2"));
        assertEquals(index3, indices.getIndex("index3"));

        assertThrows(IllegalArgumentException.class, () -> indices.getIndex("non-existent"));
    }

    @Test
    void shouldCloseIndicesCorrectly() throws Exception {
        doNothing().when(index1).close();
        doNothing().when(index2).close();
        doNothing().when(index3).close();

        indices.close();

        verify(index1).close();
        verify(index2).close();
        verify(index3).close();

        var fake = createFake("fake");
        assertThrows(IllegalStateException.class, () -> indices.insert(fake));
        assertThrows(IllegalStateException.class, () -> indices.update(fake));
        assertThrows(IllegalStateException.class, () -> indices.delete(fake));
        assertThrows(IllegalStateException.class, () -> indices.getIndex("index1"));
    }

    Fake createFake(String name) {
        var fake = new Fake();
        fake.setMetadata(new Metadata());
        fake.getMetadata().setName(name);
        return fake;
    }

}