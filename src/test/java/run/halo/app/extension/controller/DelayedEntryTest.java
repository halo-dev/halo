package run.halo.app.extension.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import run.halo.app.extension.controller.RequestQueue.DelayedEntry;

class DelayedEntryTest {

    Instant now = Instant.now();

    @Test
    void createDelayedEntry() {
        var delayedEntry = new DelayedEntry<>("fake", Duration.ofMillis(100), () -> now);
        assertEquals(100, delayedEntry.getDelay(TimeUnit.MILLISECONDS));
        assertEquals(Duration.ofMillis(100), delayedEntry.getRetryAfter());
        assertEquals("fake", delayedEntry.getEntry());

        delayedEntry = new DelayedEntry<>("fake", now.plus(Duration.ofSeconds(1)), () -> now);
        assertEquals(1000, delayedEntry.getDelay(TimeUnit.MILLISECONDS));
        assertEquals(Duration.ofMillis(1000), delayedEntry.getRetryAfter());
    }

    @Test
    void compareWithGreaterDelay() {
        var firstDelayEntry = new DelayedEntry<>("fake", Duration.ofMillis(100), () -> now);
        var secondDelayEntry = new DelayedEntry<>("fake", Duration.ofMillis(200), () -> now);

        assertTrue(firstDelayEntry.compareTo(secondDelayEntry) < 0);
    }

    @Test
    void compareWithSameDelay() {
        var firstDelayEntry = new DelayedEntry<>("fake", Duration.ofMillis(100), () -> now);
        var secondDelayEntry = new DelayedEntry<>("fake", Duration.ofMillis(100), () -> now);

        assertEquals(0, firstDelayEntry.compareTo(secondDelayEntry));
    }

    @Test
    void compareWithLessDelay() {
        var firstDelayEntry = new DelayedEntry<>("fake", Duration.ofMillis(200), () -> now);
        var secondDelayEntry = new DelayedEntry<>("fake", Duration.ofMillis(100), () -> now);

        assertTrue(firstDelayEntry.compareTo(secondDelayEntry) > 0);
    }

    @Test
    void shouldBeEqualWithNameOnly() {
        var firstDelayEntry = new DelayedEntry<>("fake", Duration.ofMillis(200), () -> now);
        var secondDelayEntry = new DelayedEntry<>("fake", Duration.ofMillis(100), Instant::now);

        assertEquals(firstDelayEntry, secondDelayEntry);
    }
}