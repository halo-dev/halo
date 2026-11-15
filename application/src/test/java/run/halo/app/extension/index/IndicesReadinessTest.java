package run.halo.app.extension.index;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import run.halo.app.extension.Extension;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.MetadataOperator;

class IndicesReadinessTest {

    @Test
    void awaitReadyShouldReturnImmediatelyWhenNotBuilding() {
        var manager = new DefaultIndicesManager();
        manager.add(FakeExt.class, List.of());
        // no markBuilding called: freshly added (empty) indices are safe to query
        manager.awaitReady(FakeExt.class);
    }

    @Test
    void awaitReadyShouldBlockUntilMarkReady() throws InterruptedException {
        var manager = new DefaultIndicesManager();
        manager.add(FakeExt.class, List.of());
        manager.markBuilding(FakeExt.class);
        var queried = new AtomicBoolean(false);
        var threadStarted = new CountDownLatch(1);
        var thread = new Thread(() -> {
            threadStarted.countDown();
            manager.awaitReady(FakeExt.class);
            queried.set(true);
        });
        thread.start();
        assertThat(threadStarted.await(5, TimeUnit.SECONDS)).isTrue();
        Thread.sleep(200);
        assertThat(queried).isFalse();
        manager.markReady(FakeExt.class);
        thread.join(5000);
        assertThat(queried).isTrue();
    }

    @Test
    void awaitReadyShouldThrowForUnknownType() {
        var manager = new DefaultIndicesManager();
        assertThatThrownBy(() -> manager.awaitReady(FakeExt.class)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void markBuildingShouldThrowForUnknownType() {
        var manager = new DefaultIndicesManager();
        assertThatThrownBy(() -> manager.markBuilding(FakeExt.class)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void markReadyShouldBeIdempotent() {
        var manager = new DefaultIndicesManager();
        manager.add(FakeExt.class, List.of());
        manager.markBuilding(FakeExt.class);
        manager.markReady(FakeExt.class);
        manager.markReady(FakeExt.class);
        manager.awaitReady(FakeExt.class); // returns immediately
    }

    @Test
    void markBuildingShouldStartANewBuildCycle() {
        var manager = new DefaultIndicesManager();
        manager.add(FakeExt.class, List.of());
        manager.markBuilding(FakeExt.class);
        manager.markReady(FakeExt.class);
        manager.markBuilding(FakeExt.class);
        manager.setReadyTimeout(Duration.ofMillis(100));
        // still building: await must time out rather than see the previous cycle's readiness
        assertThatThrownBy(() -> manager.awaitReady(FakeExt.class)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void removeShouldDiscardLatch() {
        var manager = new DefaultIndicesManager();
        manager.add(FakeExt.class, List.of());
        manager.markBuilding(FakeExt.class);
        manager.markReady(FakeExt.class);
        manager.remove(FakeExt.class);
        assertThatThrownBy(() -> manager.awaitReady(FakeExt.class)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void removingFailedIndicesMustReleaseAlreadyBlockedQueries() throws Exception {
        var manager = new DefaultIndicesManager();
        manager.add(FakeExt.class, List.of());
        manager.markBuilding(FakeExt.class);
        var outcome = new java.util.concurrent.CompletableFuture<Throwable>();
        var thread = new Thread(() -> {
            try {
                manager.awaitReady(FakeExt.class);
                manager.get(FakeExt.class);
                outcome.complete(null);
            } catch (Throwable failure) {
                outcome.complete(failure);
            }
        });
        thread.start();
        try {
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (thread.getState() != Thread.State.TIMED_WAITING
                    && thread.isAlive()
                    && System.nanoTime() < deadline) {
                Thread.yield();
            }
            assertThat(thread.getState()).isEqualTo(Thread.State.TIMED_WAITING);
            manager.remove(FakeExt.class);
            assertThat(outcome.get(5, TimeUnit.SECONDS))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("initialization was aborted");
        } finally {
            thread.interrupt();
            thread.join(5000);
        }
    }

    @Test
    void failedBuildWaiterMustNotReadPartialIndicesFromARetry() throws Exception {
        var manager = new DefaultIndicesManager();
        manager.add(FakeExt.class, List.of());
        manager.markBuilding(FakeExt.class);
        var exitedWait = new CountDownLatch(1);
        var resumeQuery = new CountDownLatch(1);
        var outcome = new java.util.concurrent.CompletableFuture<java.util.Set<String>>();
        var thread = new Thread(() -> {
            try {
                try {
                    manager.awaitReady(FakeExt.class);
                } finally {
                    exitedWait.countDown();
                    resumeQuery.await();
                }
                outcome.complete(
                        ((ValueIndexQuery<String>) manager.get(FakeExt.class).<String>getIndex("metadata.name")).all());
            } catch (Throwable failure) {
                outcome.completeExceptionally(failure);
            }
        });
        thread.start();
        try {
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (thread.getState() != Thread.State.TIMED_WAITING
                    && thread.isAlive()
                    && System.nanoTime() < deadline) {
                Thread.yield();
            }
            assertThat(thread.getState()).isEqualTo(Thread.State.TIMED_WAITING);
            manager.remove(FakeExt.class);
            assertThat(exitedWait.await(5, TimeUnit.SECONDS)).isTrue();
            manager.add(FakeExt.class, List.of());
            manager.markBuilding(FakeExt.class);
            var partial = new FakeExt();
            partial.getMetadata().setName("partial");
            partial.getMetadata().setCreationTimestamp(java.time.Instant.EPOCH);
            manager.get(FakeExt.class).insert(partial);
            resumeQuery.countDown();
            assertThatThrownBy(() -> outcome.get(5, TimeUnit.SECONDS))
                    .isInstanceOf(java.util.concurrent.ExecutionException.class)
                    .hasCauseInstanceOf(IllegalStateException.class);
            manager.setReadyTimeout(Duration.ofMillis(20));
            assertThatThrownBy(() -> manager.awaitReady(FakeExt.class))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not ready");
            manager.markReady(FakeExt.class);
            manager.awaitReady(FakeExt.class);
        } finally {
            resumeQuery.countDown();
            thread.interrupt();
            thread.join(5000);
            manager.close();
        }
    }

    @Test
    void awaitReadyShouldTimeoutWhileBuilding() {
        var manager = new DefaultIndicesManager();
        manager.setReadyTimeout(Duration.ofMillis(100));
        manager.add(FakeExt.class, List.of());
        manager.markBuilding(FakeExt.class);
        assertThatThrownBy(() -> manager.awaitReady(FakeExt.class))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not ready");
    }

    static class FakeExt implements Extension {
        private final Metadata metadata = new Metadata();

        @Override
        public Metadata getMetadata() {
            return metadata;
        }

        @Override
        public void setMetadata(MetadataOperator metadata) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setApiVersion(String apiVersion) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setKind(String kind) {
            throw new UnsupportedOperationException();
        }
    }
}
