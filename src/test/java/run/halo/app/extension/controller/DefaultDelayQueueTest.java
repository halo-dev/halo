package run.halo.app.extension.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import run.halo.app.extension.controller.Reconciler.Request;
import run.halo.app.extension.controller.RequestQueue.DelayedEntry;

class DefaultDelayQueueTest {

    Instant now = Instant.now();

    DefaultDelayQueue<Request> queue;

    final Duration minDelay = Duration.ofMillis(1);

    @BeforeEach
    void setUp() {
        queue = new DefaultDelayQueue<>(() -> now, minDelay);
    }

    @Test
    void addImmediatelyTest() {
        var request = newRequest("fake-name");
        var added = queue.addImmediately(request);
        assertTrue(added);
        assertEquals(1, queue.size());
        var delayedEntry = queue.peek();
        assertNotNull(delayedEntry);
        assertEquals(newRequest("fake-name"), delayedEntry.getEntry());
        assertEquals(minDelay, delayedEntry.getRetryAfter());
        assertEquals(minDelay.toMillis(), delayedEntry.getDelay(TimeUnit.MILLISECONDS));
    }

    @Test
    void addWithDelaySmallerThanMinDelay() {
        var request = newRequest("fake-name");
        var added = queue.add(new DelayedEntry<>(request, Duration.ofNanos(1), () -> now));
        assertTrue(added);
        assertEquals(1, queue.size());
        var delayedEntry = queue.peek();
        assertNotNull(delayedEntry);
        assertEquals(newRequest("fake-name"), delayedEntry.getEntry());
        assertEquals(minDelay, delayedEntry.getRetryAfter());
        assertEquals(minDelay.toMillis(), delayedEntry.getDelay(TimeUnit.MILLISECONDS));
    }

    @Test
    void addWithDelayGreaterThanMinDelay() {
        var request = newRequest("fake-name");
        var added = queue.add(new DelayedEntry<>(request, minDelay.plusMillis(1), () -> now));
        assertTrue(added);
        assertEquals(1, queue.size());
        var delayedEntry = queue.peek();
        assertNotNull(delayedEntry);
        assertEquals(newRequest("fake-name"), delayedEntry.getEntry());
        assertEquals(minDelay.plusMillis(1), delayedEntry.getRetryAfter());
        assertEquals(minDelay.plusMillis(1).toMillis(),
            delayedEntry.getDelay(TimeUnit.MILLISECONDS));
    }

    @Test
    void shouldNotAddAfterDisposing() {
        assertFalse(queue.isDisposed());
        queue.dispose();
        assertTrue(queue.isDisposed());
        var request = newRequest("fake-name");
        var added = queue.add(new DelayedEntry<>(request, minDelay, () -> now));
        assertFalse(added);
        assertEquals(0, queue.size());
    }

    @Test
    void shouldNotAddRepeatedlyIfNotDone() throws InterruptedException {
        var entrySpy = spy(new DelayedEntry<>(newRequest("fake-name"), minDelay, () -> now));

        doReturn(0L).when(entrySpy).getDelay(any());

        queue.add(entrySpy);
        assertEquals(1, queue.size());
        assertEquals(entrySpy, queue.peek());
        queue.take();
        assertEquals(0, queue.size());

        queue.add(entrySpy);
        assertEquals(0, queue.size());

        queue.done(newRequest("fake-name"));
        queue.add(entrySpy);
        assertEquals(1, queue.size());
        assertEquals(entrySpy, queue.peek());
    }

    Request newRequest(String name) {
        return new Request(name);
    }

}