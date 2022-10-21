package run.halo.app.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import run.halo.app.extension.SchemeWatcherManager.SchemeWatcher;

class DefaultSchemeWatcherManagerTest {

    DefaultSchemeWatcherManager watcherManager;

    @BeforeEach
    void setUp() {
        watcherManager = new DefaultSchemeWatcherManager();
    }

    @Test
    void shouldThrowExceptionWhenRegisterNullWatcher() {
        assertThrows(IllegalArgumentException.class, () -> watcherManager.register(null));
    }

    @Test
    void shouldThrowExceptionWhenUnregisterNullWatcher() {
        assertThrows(IllegalArgumentException.class, () -> watcherManager.unregister(null));
    }

    @Test
    void shouldRegisterSuccessfully() {
        var watcher = mock(SchemeWatcher.class);
        watcherManager.register(watcher);

        assertEquals(watcherManager.watchers(), List.of(watcher));
    }

    @Test
    void shouldUnregisterSuccessfully() {
        var watcher = mock(SchemeWatcher.class);
        watcherManager.register(watcher);
        assertEquals(List.of(watcher), watcherManager.watchers());

        watcherManager.unregister(watcher);
        assertEquals(Collections.emptyList(), watcherManager.watchers());
    }

    @Test
    void shouldReturnCopyOfWatchers() {
        var firstWatcher = mock(SchemeWatcher.class);
        var secondWatcher = mock(SchemeWatcher.class);
        watcherManager.register(firstWatcher);

        var watchers = watcherManager.watchers();
        watchers.forEach(watcher -> watcherManager.register(secondWatcher));
    }
}