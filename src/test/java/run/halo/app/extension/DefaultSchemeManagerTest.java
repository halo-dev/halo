package run.halo.app.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.extension.SchemeWatcherManager.SchemeRegistered;
import run.halo.app.extension.SchemeWatcherManager.SchemeUnregistered;
import run.halo.app.extension.SchemeWatcherManager.SchemeWatcher;
import run.halo.app.extension.exception.SchemeNotFoundException;

@ExtendWith(MockitoExtension.class)
class DefaultSchemeManagerTest {

    @Mock
    SchemeWatcherManager watcherManager;

    @InjectMocks
    DefaultSchemeManager schemeManager;

    @Test
    void shouldThrowExceptionWhenNoGvkAnnotation() {
        class WithoutGvkExtension extends AbstractExtension {
        }

        assertThrows(IllegalArgumentException.class,
            () -> schemeManager.register(WithoutGvkExtension.class));
    }

    @Test
    void shouldGetNothingWhenUnregistered() {
        final var gvk = new GroupVersionKind("fake.halo.run", "v1alpha1", "Fake");
        var scheme = schemeManager.fetch(gvk);
        assertFalse(scheme.isPresent());

        assertThrows(SchemeNotFoundException.class, () -> schemeManager.get(gvk));
        assertThrows(SchemeNotFoundException.class, () -> schemeManager.get(FakeExtension.class));
        assertThrows(SchemeNotFoundException.class, () -> schemeManager.get(new FakeExtension()));
    }

    @Test
    void shouldGetSchemeWhenRegistered() {
        schemeManager.register(FakeExtension.class);
        final var gvk = new GroupVersionKind("fake.halo.run", "v1alpha1", "Fake");
        var scheme = schemeManager.fetch(gvk);
        assertTrue(scheme.isPresent());

        assertEquals(gvk, schemeManager.get(gvk).groupVersionKind());
        assertEquals(gvk, schemeManager.get(FakeExtension.class).groupVersionKind());
        assertEquals(gvk, schemeManager.get(new FakeExtension()).groupVersionKind());
    }

    @Test
    void shouldUnregisterSuccessfully() {
        schemeManager.register(FakeExtension.class);
        Scheme scheme = schemeManager.get(FakeExtension.class);
        assertNotNull(scheme);

        schemeManager.unregister(scheme);
        assertThrows(SchemeNotFoundException.class, () -> schemeManager.get(FakeExtension.class));
    }

    @Test
    void shouldTriggerOnChangeOnlyOnceWhenRegisterTwice() {
        final var watcher = mock(SchemeWatcher.class);
        when(watcherManager.watchers()).thenReturn(List.of(watcher));

        schemeManager.register(FakeExtension.class);
        verify(watcherManager, times(1)).watchers();
        verify(watcher, times(1)).onChange(isA(SchemeRegistered.class));

        schemeManager.register(FakeExtension.class);
        verify(watcherManager, times(1)).watchers();
        verify(watcher, times(1)).onChange(isA(SchemeRegistered.class));

    }

    @Test
    void shouldTriggerOnChangeOnlyOnceWhenUnregisterTwice() {

        final var watcher = mock(SchemeWatcher.class);
        when(watcherManager.watchers()).thenReturn(List.of(watcher));

        schemeManager.register(FakeExtension.class);

        var scheme = schemeManager.get(FakeExtension.class);

        schemeManager.unregister(scheme);
        verify(watcherManager, times(2)).watchers();
        verify(watcher, times(1)).onChange(isA(SchemeUnregistered.class));

        schemeManager.unregister(scheme);
        verify(watcherManager, times(2)).watchers();
        verify(watcher, times(1)).onChange(isA(SchemeUnregistered.class));
    }

    @Test
    void getSizeOfSchemes() {
        assertEquals(0, schemeManager.size());
        schemeManager.register(FakeExtension.class);
        assertEquals(1, schemeManager.size());
        schemeManager.unregister(schemeManager.get(FakeExtension.class));
        assertEquals(0, schemeManager.size());
    }

    @Test
    void shouldReturnCopyOnWriteList() {
        schemeManager.register(FakeExtension.class);
        var schemes = schemeManager.schemes();
        schemes.forEach(scheme -> {
            // make sure concurrent modification won't happen
            schemeManager.register(FooExtension.class);
        });
    }

    @GVK(group = "fake.halo.run", version = "v1alpha1", kind = "Foo",
        plural = "foos", singular = "foo")
    static class FooExtension extends AbstractExtension {
    }
}

