package run.halo.app.extension.gc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.extension.FakeExtension;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.controller.RequestQueue;

@ExtendWith(MockitoExtension.class)
class GcWatcherTest {

    @Mock
    RequestQueue<GcRequest> queue;

    @InjectMocks
    GcWatcher watcher;

    @Test
    void shouldAddIntoQueueWhenDeletionTimestampSet() {
        var fake = createExtension();
        fake.getMetadata().setDeletionTimestamp(Instant.now());

        watcher.onAdd(fake);
        verify(queue).addImmediately(any(GcRequest.class));

        watcher.onUpdate(fake, fake);
        verify(queue, times(2)).addImmediately(any(GcRequest.class));

        watcher.onDelete(fake);
        verify(queue, times(3)).addImmediately(any(GcRequest.class));
    }

    @Test
    void shouldNotAddIntoQueueWhenDeletionTimestampNotSet() {
        var fake = createExtension();
        watcher.onAdd(fake);
        verify(queue, never()).addImmediately(any(GcRequest.class));

        watcher.onUpdate(fake, fake);
        verify(queue, never()).addImmediately(any(GcRequest.class));

        watcher.onDelete(fake);
        verify(queue, never()).addImmediately(any(GcRequest.class));
    }

    @Test
    void shouldNotAddIntoQueueWhenDisposed() {
        var fake = createExtension();
        fake.getMetadata().setDeletionTimestamp(Instant.now());
        watcher.dispose();

        watcher.onAdd(fake);
        verify(queue, never()).addImmediately(any(GcRequest.class));

        watcher.onUpdate(fake, fake);
        verify(queue, never()).addImmediately(any(GcRequest.class));

        watcher.onDelete(fake);
        verify(queue, never()).addImmediately(any(GcRequest.class));
    }

    @Test
    void shouldDisposeHookCorrectly() {
        var run = mock(Runnable.class);
        watcher.registerDisposeHook(run);
        assertFalse(watcher.isDisposed());
        watcher.dispose();
        assertTrue(watcher.isDisposed());
        verify(run).run();
    }


    FakeExtension createExtension() {
        var fake = new FakeExtension();
        Metadata metadata = new Metadata();
        metadata.setName("fake");
        fake.setMetadata(metadata);
        return fake;
    }
}
