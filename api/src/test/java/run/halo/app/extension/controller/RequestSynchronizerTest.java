package run.halo.app.extension.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.function.Predicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.FakeExtension;
import run.halo.app.extension.Watcher;

@ExtendWith(MockitoExtension.class)
class RequestSynchronizerTest {

    @Mock
    ExtensionClient client;

    @Mock
    Watcher watcher;

    @Mock
    Predicate<Extension> listPredicate;

    RequestSynchronizer synchronizer;

    @BeforeEach
    void setUp() {
        synchronizer =
            new RequestSynchronizer(true, client, new FakeExtension(), watcher, listPredicate);
        assertFalse(synchronizer.isDisposed());
        assertFalse(synchronizer.isStarted());
    }

    @Test
    void shouldStartCorrectlyWhenSyncingAllOnStart() {
        when(client.list(same(FakeExtension.class), any(), any())).thenReturn(
            List.of(FakeExtension.createFake("fake-01"), FakeExtension.createFake("fake-02")));

        synchronizer.start();

        assertTrue(synchronizer.isStarted());
        assertFalse(synchronizer.isDisposed());

        verify(client, times(1)).list(same(FakeExtension.class), any(), any());
        verify(watcher, times(2)).onAdd(any());
        verify(client, times(1)).watch(same(watcher));
    }

    @Test
    void shouldStartCorrectlyWhenNotSyncingAllOnStart() {
        synchronizer =
            new RequestSynchronizer(false, client, new FakeExtension(), watcher, listPredicate);
        assertFalse(synchronizer.isDisposed());
        assertFalse(synchronizer.isStarted());

        synchronizer.start();

        assertTrue(synchronizer.isStarted());
        assertFalse(synchronizer.isDisposed());

        verify(client, times(0)).list(any(), any(), any());
        verify(watcher, times(0)).onAdd(any());
        verify(client, times(1)).watch(any(Watcher.class));
    }

    @Test
    void shouldDisposeCorrectly() {
        synchronizer.start();
        assertFalse(synchronizer.isDisposed());
        assertTrue(synchronizer.isStarted());

        synchronizer.dispose();

        assertTrue(synchronizer.isDisposed());
        assertTrue(synchronizer.isStarted());
        verify(watcher, times(1)).dispose();
    }

    @Test
    void shouldNotStartAfterDisposing() {
        synchronizer.dispose();
        synchronizer.start();

        verify(client, times(0)).list(any(), any(), any());
        verify(watcher, times(0)).onAdd(any());
        verify(client, times(0)).watch(any());
    }

}