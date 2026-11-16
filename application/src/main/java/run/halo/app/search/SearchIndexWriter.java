package run.halo.app.search;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.core.scheduler.Schedulers;

/** Serializes actual engine calls, including calls that ignore a timeout interrupt. */
final class SearchIndexWriter implements AutoCloseable {
    private final Duration timeout;

    private final Set<WriteTask> pending = ConcurrentHashMap.newKeySet();

    private final ExecutorService executor = Executors.newSingleThreadExecutor(Thread.ofVirtual()
            .name("search-index-write")
            .inheritInheritableThreadLocals(false)
            .factory());

    SearchIndexWriter(Duration timeout) {
        this.timeout = timeout;
    }

    Mono<Void> write(Runnable operation) {
        return Mono.create(sink -> {
            var task = new WriteTask(operation, sink);
            pending.add(task);
            sink.onCancel(() -> task.cancel(true));
            try {
                executor.execute(task);
            } catch (RejectedExecutionException error) {
                task.stop(error);
            }
        });
    }

    @Override
    public void close() {
        executor.shutdownNow();
        for (var task : pending) {
            task.stop(new CancellationException("Search index writer closed"));
        }
    }

    private final class WriteTask extends FutureTask<Void> {
        private final MonoSink<Void> sink;

        private WriteTask(Runnable operation, MonoSink<Void> sink) {
            super(operation, null);
            this.sink = sink;
        }

        @Override
        public void run() {
            if (isCancelled()) {
                return;
            }
            // Queued writes get their full timeout only after the previous call has exited.
            var deadline = Schedulers.parallel()
                    .schedule(
                            () -> stop(new TimeoutException("Search index write timed out after " + timeout)),
                            timeout.toNanos(),
                            TimeUnit.NANOSECONDS);
            try {
                super.run();
                if (isCancelled()) {
                    return;
                }
                get();
                sink.success();
            } catch (ExecutionException error) {
                sink.error(error.getCause());
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt();
                sink.error(error);
            } finally {
                deadline.dispose();
            }
        }

        private void stop(Exception reason) {
            // FutureTask ties the interrupt to this call; a late deadline cannot interrupt its successor.
            if (cancel(true)) {
                sink.error(reason);
            }
        }

        @Override
        protected void done() {
            pending.remove(this);
        }
    }
}
