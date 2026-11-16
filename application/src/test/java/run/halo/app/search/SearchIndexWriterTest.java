package run.halo.app.search;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class SearchIndexWriterTest {

    private static final Duration WRITE_TIMEOUT = Duration.ofMillis(250);

    private final SearchIndexWriter writer = new SearchIndexWriter(WRITE_TIMEOUT);

    @AfterEach
    void close() {
        writer.close();
    }

    @Test
    void interruptsTimedOutWriteAndAllowsNextWrite() throws Exception {
        var entered = new CountDownLatch(1);
        var interrupted = new CountDownLatch(1);
        var nextWrite = new AtomicBoolean();

        StepVerifier.create(writer.write(() -> {
                    entered.countDown();
                    try {
                        new CountDownLatch(1).await();
                    } catch (InterruptedException error) {
                        interrupted.countDown();
                        Thread.currentThread().interrupt();
                    }
                }))
                .expectError(TimeoutException.class)
                .verify(Duration.ofSeconds(3));

        assertThat(entered.getCount()).isZero();
        assertThat(interrupted.await(3, TimeUnit.SECONDS)).isTrue();
        StepVerifier.create(writer.write(() -> nextWrite.set(true))).verifyComplete();
        assertThat(nextWrite).isTrue();
    }

    @Test
    void queuedWriteDoesNotExpireOrOvertakeWriteIgnoringInterruption() throws Exception {
        var entered = new CountDownLatch(1);
        var interrupted = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        var timedOut = new CountDownLatch(1);
        var nextFinished = new CountDownLatch(1);
        var firstFailure = new AtomicReference<Throwable>();
        var nextFailure = new AtomicReference<Throwable>();
        var order = new CopyOnWriteArrayList<String>();
        try {
            writer.write(() -> {
                        order.add("old-start");
                        entered.countDown();
                        awaitIgnoringInterruption(release, interrupted);
                        order.add("old-end");
                    })
                    .subscribe(ignored -> {}, error -> {
                        firstFailure.set(error);
                        timedOut.countDown();
                    });
            assertThat(entered.await(3, TimeUnit.SECONDS)).isTrue();
            writer.write(() -> order.add("next"))
                    .subscribe(
                            ignored -> {},
                            error -> {
                                nextFailure.set(error);
                                nextFinished.countDown();
                            },
                            nextFinished::countDown);

            assertThat(timedOut.await(3, TimeUnit.SECONDS)).isTrue();
            assertThat(firstFailure.get()).isInstanceOf(TimeoutException.class);
            assertThat(interrupted.await(3, TimeUnit.SECONDS)).isTrue();
            assertThat(nextFinished.await(WRITE_TIMEOUT.toMillis() * 3, TimeUnit.MILLISECONDS))
                    .isFalse();
            assertThat(order).containsExactly("old-start");

            release.countDown();
            assertThat(nextFinished.await(3, TimeUnit.SECONDS)).isTrue();
            assertThat(nextFailure).hasNullValue();
            assertThat(order).containsExactly("old-start", "old-end", "next");
        } finally {
            release.countDown();
        }
    }

    @Test
    void eachWriteGetsItsOwnTimeoutInsteadOfSharingAnOverallDeadline() {
        var started = System.nanoTime();
        var completed = new CopyOnWriteArrayList<Integer>();
        for (int index = 0; index < 4; index++) {
            int item = index;
            StepVerifier.create(writer.write(() -> {
                        try {
                            new CountDownLatch(1).await(100, TimeUnit.MILLISECONDS);
                        } catch (InterruptedException error) {
                            Thread.currentThread().interrupt();
                            throw new IllegalStateException(error);
                        }
                        completed.add(item);
                    }))
                    .expectComplete()
                    .verify(Duration.ofSeconds(3));
        }

        assertThat(completed).containsExactly(0, 1, 2, 3);
        assertThat(Duration.ofNanos(System.nanoTime() - started)).isGreaterThan(WRITE_TIMEOUT);
    }

    @Test
    void cancelledPendingWriteNeverInvokesItsOperation() throws Exception {
        var entered = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        var pendingCalled = new AtomicBoolean();
        try {
            writer.write(() -> {
                        entered.countDown();
                        awaitIgnoringInterruption(release, new CountDownLatch(1));
                    })
                    .subscribe(ignored -> {}, error -> {});
            assertThat(entered.await(3, TimeUnit.SECONDS)).isTrue();
            var pending = writer.write(() -> pendingCalled.set(true)).subscribe(ignored -> {}, error -> {});

            pending.dispose();
            release.countDown();
            StepVerifier.create(writer.write(() -> {})).expectComplete().verify(Duration.ofSeconds(3));
            assertThat(pendingCalled).isFalse();
        } finally {
            release.countDown();
        }
    }

    @Test
    void closeDoesNotResumePendingWritesWhenOldOperationEventuallyExits() throws Exception {
        var entered = new CountDownLatch(1);
        var interrupted = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        var exited = new CountDownLatch(1);
        var calls = new CopyOnWriteArrayList<String>();
        try {
            writer.write(() -> {
                        entered.countDown();
                        awaitIgnoringInterruption(release, interrupted);
                        calls.add("old-finished");
                        exited.countDown();
                    })
                    .subscribe(ignored -> {}, error -> {});
            assertThat(entered.await(3, TimeUnit.SECONDS)).isTrue();
            writer.write(() -> calls.add("pending")).subscribe(ignored -> {}, error -> {});

            writer.close();
            assertThat(interrupted.await(3, TimeUnit.SECONDS)).isTrue();
            release.countDown();
            assertThat(exited.await(3, TimeUnit.SECONDS)).isTrue();
            StepVerifier.create(writer.write(() -> calls.add("after-close")))
                    .expectError()
                    .verify(Duration.ofSeconds(3));
            assertThat(calls).containsExactly("old-finished");
        } finally {
            release.countDown();
        }
    }

    @Test
    void operationFailureIsPreservedAndDoesNotBlockFollowingWrite() {
        var failure = new IllegalArgumentException("Invalid search document");
        var calls = new CopyOnWriteArrayList<String>();

        StepVerifier.create(writer.write(() -> {
                    throw failure;
                }))
                .expectErrorSatisfies(error -> assertThat(error).isSameAs(failure))
                .verify(Duration.ofSeconds(3));
        StepVerifier.create(writer.write(() -> calls.add("next"))).verifyComplete();

        assertThat(calls).isEqualTo(List.of("next"));
    }

    private static void awaitIgnoringInterruption(CountDownLatch release, CountDownLatch interrupted) {
        while (true) {
            try {
                release.await();
                return;
            } catch (InterruptedException error) {
                interrupted.countDown();
            }
        }
    }
}
