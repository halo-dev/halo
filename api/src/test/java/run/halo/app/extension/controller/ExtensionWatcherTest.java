package run.halo.app.extension.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static run.halo.app.extension.FakeExtension.createFake;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.extension.WatcherPredicates;
import run.halo.app.extension.controller.Reconciler.Request;

@ExtendWith(MockitoExtension.class)
class ExtensionWatcherTest {

    @Mock
    RequestQueue<Request> queue;

    @Mock
    WatcherPredicates predicates;

    @InjectMocks
    ExtensionWatcher watcher;

    @Test
    void shouldAddExtensionWhenAddPredicateAlwaysTrue() {
        when(predicates.onAddPredicate()).thenReturn(e -> true);
        watcher.onAdd(createFake("fake-name"));

        verify(predicates, times(1)).onAddPredicate();
        verify(queue, times(1)).addImmediately(
            argThat(request -> request.name().equals("fake-name")));
        verify(queue, times(0)).add(any());
    }

    @Test
    void shouldNotAddExtensionWhenAddPredicateAlwaysFalse() {
        when(predicates.onAddPredicate()).thenReturn(e -> false);
        watcher.onAdd(createFake("fake-name"));

        verify(predicates, times(1)).onAddPredicate();
        verify(queue, times(0)).add(any());
        verify(queue, times(0)).addImmediately(any());
    }

    @Test
    void shouldNotAddExtensionWhenWatcherIsDisposed() {
        watcher.dispose();
        watcher.onAdd(createFake("fake-name"));

        verify(predicates, times(0)).onAddPredicate();
        verify(queue, times(0)).addImmediately(any());
        verify(queue, times(0)).add(any());
    }

    @Test
    void shouldUpdateExtensionWhenUpdatePredicateAlwaysTrue() {
        when(predicates.onUpdatePredicate()).thenReturn((e1, e2) -> true);
        watcher.onUpdate(createFake("old-fake-name"), createFake("new-fake-name"));

        verify(predicates, times(1)).onUpdatePredicate();
        verify(queue, times(1)).addImmediately(
            argThat(request -> request.name().equals("new-fake-name")));
        verify(queue, times(0)).add(any());
    }

    @Test
    void shouldUpdateExtensionWhenUpdatePredicateAlwaysFalse() {
        when(predicates.onUpdatePredicate()).thenReturn((e1, e2) -> false);
        watcher.onUpdate(createFake("old-fake-name"), createFake("new-fake-name"));

        verify(predicates, times(1)).onUpdatePredicate();
        verify(queue, times(0)).add(any());
        verify(queue, times(0)).addImmediately(any());
    }

    @Test
    void shouldNotUpdateExtensionWhenWatcherIsDisposed() {
        watcher.dispose();
        watcher.onUpdate(createFake("old-fake-name"), createFake("new-fake-name"));

        verify(predicates, times(0)).onUpdatePredicate();
        verify(queue, times(0)).add(any());
        verify(queue, times(0)).addImmediately(any());
    }

    @Test
    void shouldDeleteExtensionWhenDeletePredicateAlwaysTrue() {
        when(predicates.onDeletePredicate()).thenReturn(e -> true);
        watcher.onDelete(createFake("fake-name"));

        verify(predicates, times(1)).onDeletePredicate();
        verify(queue, times(1)).addImmediately(
            argThat(request -> request.name().equals("fake-name")));
        verify(queue, times(0)).add(any());
    }

    @Test
    void shouldDeleteExtensionWhenDeletePredicateAlwaysFalse() {
        when(predicates.onDeletePredicate()).thenReturn(e -> false);
        watcher.onDelete(createFake("fake-name"));

        verify(predicates, times(1)).onDeletePredicate();
        verify(queue, times(0)).add(any());
        verify(queue, times(0)).addImmediately(any());
    }

    @Test
    void shouldNotDeleteExtensionWhenWatcherIsDisposed() {
        watcher.dispose();
        watcher.onDelete(createFake("fake-name"));

        verify(predicates, times(0)).onDeletePredicate();
        verify(queue, times(0)).add(any());
        verify(queue, times(0)).addImmediately(any());
    }

    @Test
    void shouldInvokeDisposeHookIfRegistered() {
        var mockHook = mock(Runnable.class);
        watcher.registerDisposeHook(mockHook);
        verify(mockHook, times(0)).run();

        watcher.dispose();
        verify(mockHook, times(1)).run();
    }
}