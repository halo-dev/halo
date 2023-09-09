package run.halo.app.extension.gc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.SchemeWatcherManager;
import run.halo.app.extension.SchemeWatcherManager.SchemeWatcher;

@ExtendWith(MockitoExtension.class)
class GcSynchronizerTest {

    @Mock
    ExtensionClient client;

    @Mock
    SchemeManager schemeManager;

    @Mock
    SchemeWatcherManager schemeWatcherManager;

    @InjectMocks
    GcSynchronizer synchronizer;

    @Test
    void shouldStartNormally() {
        synchronizer.start();

        assertFalse(synchronizer.isDisposed());
        verify(schemeWatcherManager).register(any(SchemeWatcher.class));
        verify(client).watch(isA(GcWatcher.class));
        verify(schemeManager).schemes();
    }

    @Test
    void shouldDisposeSuccessfully() {
        assertFalse(synchronizer.isDisposed());

        synchronizer.dispose();

        assertTrue(synchronizer.isDisposed());
    }
}