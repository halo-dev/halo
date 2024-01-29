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
import run.halo.app.extension.DefaultExtensionMatcher;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.FakeExtension;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.WatcherExtensionMatchers;
import run.halo.app.extension.controller.Reconciler.Request;

@ExtendWith(MockitoExtension.class)
class ExtensionWatcherTest {

    @Mock
    RequestQueue<Request> queue;

    @Mock
    ExtensionClient client;

    @Mock
    WatcherExtensionMatchers matchers;

    @InjectMocks
    ExtensionWatcher watcher;

    private DefaultExtensionMatcher getEmptyMatcher() {
        return DefaultExtensionMatcher.builder(client,
                GroupVersionKind.fromExtension(FakeExtension.class))
            .build();
    }

    @Test
    void shouldAddExtensionWhenAddPredicateAlwaysTrue() {
        when(matchers.onAddMatcher()).thenReturn(getEmptyMatcher());
        watcher.onAdd(createFake("fake-name"));

        verify(matchers, times(1)).onAddMatcher();
        verify(queue, times(1)).addImmediately(
            argThat(request -> request.name().equals("fake-name")));
        verify(queue, times(0)).add(any());
    }

    @Test
    void shouldNotAddExtensionWhenAddPredicateAlwaysFalse() {
        var type = GroupVersionKind.fromAPIVersionAndKind("v1alpha1", "User");
        when(matchers.onAddMatcher()).thenReturn(
            DefaultExtensionMatcher.builder(client, type).build());
        watcher.onAdd(createFake("fake-name"));

        verify(matchers, times(1)).onAddMatcher();
        verify(queue, times(0)).add(any());
        verify(queue, times(0)).addImmediately(any());
    }

    @Test
    void shouldNotAddExtensionWhenWatcherIsDisposed() {
        watcher.dispose();
        watcher.onAdd(createFake("fake-name"));

        verify(matchers, times(0)).onAddMatcher();
        verify(queue, times(0)).addImmediately(any());
        verify(queue, times(0)).add(any());
    }

    @Test
    void shouldUpdateExtensionWhenUpdatePredicateAlwaysTrue() {
        when(matchers.onUpdateMatcher()).thenReturn(getEmptyMatcher());
        watcher.onUpdate(createFake("old-fake-name"), createFake("new-fake-name"));

        verify(matchers, times(1)).onUpdateMatcher();
        verify(queue, times(1)).addImmediately(
            argThat(request -> request.name().equals("new-fake-name")));
        verify(queue, times(0)).add(any());
    }

    @Test
    void shouldUpdateExtensionWhenUpdatePredicateAlwaysFalse() {
        var type = GroupVersionKind.fromAPIVersionAndKind("v1alpha1", "User");
        when(matchers.onUpdateMatcher()).thenReturn(
            DefaultExtensionMatcher.builder(client, type).build());
        watcher.onUpdate(createFake("old-fake-name"), createFake("new-fake-name"));

        verify(matchers, times(1)).onUpdateMatcher();
        verify(queue, times(0)).add(any());
        verify(queue, times(0)).addImmediately(any());
    }

    @Test
    void shouldNotUpdateExtensionWhenWatcherIsDisposed() {
        watcher.dispose();
        watcher.onUpdate(createFake("old-fake-name"), createFake("new-fake-name"));

        verify(matchers, times(0)).onUpdateMatcher();
        verify(queue, times(0)).add(any());
        verify(queue, times(0)).addImmediately(any());
    }

    @Test
    void shouldDeleteExtensionWhenDeletePredicateAlwaysTrue() {
        when(matchers.onDeleteMatcher()).thenReturn(getEmptyMatcher());
        watcher.onDelete(createFake("fake-name"));

        verify(matchers, times(1)).onDeleteMatcher();
        verify(queue, times(1)).addImmediately(
            argThat(request -> request.name().equals("fake-name")));
        verify(queue, times(0)).add(any());
    }

    @Test
    void shouldDeleteExtensionWhenDeletePredicateAlwaysFalse() {
        var type = GroupVersionKind.fromAPIVersionAndKind("v1alpha1", "User");
        when(matchers.onDeleteMatcher()).thenReturn(
            DefaultExtensionMatcher.builder(client, type).build());
        watcher.onDelete(createFake("fake-name"));

        verify(matchers, times(1)).onDeleteMatcher();
        verify(queue, times(0)).add(any());
        verify(queue, times(0)).addImmediately(any());
    }

    @Test
    void shouldNotDeleteExtensionWhenWatcherIsDisposed() {
        watcher.dispose();
        watcher.onDelete(createFake("fake-name"));

        verify(matchers, times(0)).onDeleteMatcher();
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