package run.halo.app.extension.gc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.SchemeManager;

@ExtendWith(MockitoExtension.class)
class GcSynchronizerTest {

    @Mock
    ExtensionClient client;

    @Mock
    SchemeManager schemeManager;

    @InjectMocks
    GcSynchronizer synchronizer;

    @Test
    void shouldStartNormally() {
        synchronizer.start();

        assertFalse(synchronizer.isDisposed());
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