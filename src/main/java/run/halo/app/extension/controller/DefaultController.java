package run.halo.app.extension.controller;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.util.StopWatch;
import run.halo.app.extension.controller.RequestQueue.DelayedEntry;

@Slf4j
public class DefaultController<R> implements Controller {

    private final String name;

    private final Reconciler<R> reconciler;

    private final Supplier<Instant> nowSupplier;

    private final RequestQueue<R> queue;

    private volatile boolean disposed = false;

    private volatile boolean started = false;

    private final ExecutorService executor;

    private final Synchronizer<R> synchronizer;

    private final Duration minDelay;

    private final Duration maxDelay;

    public DefaultController(String name,
        Reconciler<R> reconciler,
        RequestQueue<R> queue,
        Synchronizer<R> synchronizer,
        Duration minDelay,
        Duration maxDelay) {
        this(name, reconciler, queue, synchronizer, Instant::now, minDelay, maxDelay);
    }

    public DefaultController(String name,
        Reconciler<R> reconciler,
        RequestQueue<R> queue,
        Synchronizer<R> synchronizer,
        Supplier<Instant> nowSupplier,
        Duration minDelay,
        Duration maxDelay,
        ExecutorService executor) {
        this.name = name;
        this.reconciler = reconciler;
        this.nowSupplier = nowSupplier;
        this.queue = queue;
        this.synchronizer = synchronizer;
        this.minDelay = minDelay;
        this.maxDelay = maxDelay;
        this.executor = executor;
    }

    public DefaultController(String name,
        Reconciler<R> reconciler,
        RequestQueue<R> queue,
        Synchronizer<R> synchronizer,
        Supplier<Instant> nowSupplier,
        Duration minDelay,
        Duration maxDelay) {
        this(name, reconciler, queue, synchronizer, nowSupplier, minDelay, maxDelay,
            newSingleThreadExecutor(threadFactory()));
    }

    private static ThreadFactory threadFactory() {
        return new BasicThreadFactory.Builder()
            .namingPattern("reconciler-thread-%d")
            .daemon(false)
            .uncaughtExceptionHandler((t, e) ->
                log.error("Controller " + t.getName() + " encountered an error unexpectedly", e))
            .build();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void start() {
        if (isStarted() || isDisposed()) {
            log.warn("Controller {} is already started or disposed.", getName());
            return;
        }
        this.started = true;
        log.info("Starting controller {}", name);
        // TODO Make more workers run the reconciler.
        executor.submit(this::run);
    }

    protected void run() {
        log.info("Controller {} started", name);
        synchronizer.start();
        while (!this.isDisposed() && !Thread.currentThread().isInterrupted()) {
            try {
                var entry = queue.take();
                Reconciler.Result result;
                try {
                    log.debug("Reconciling request {} at {}", entry.getEntry(), nowSupplier.get());
                    StopWatch watch = new StopWatch("Reconcile: " + entry.getEntry());
                    watch.start("reconciliation");
                    result = this.reconciler.reconcile(entry.getEntry());
                    watch.stop();
                    log.debug("Reconciled request: {} with result: {}", entry.getEntry(), result);
                    if (log.isDebugEnabled()) {
                        log.debug(watch.toString());
                    }
                } catch (Throwable t) {
                    log.error("Reconciler aborted with an error, re-enqueuing...", t);
                    result = new Reconciler.Result(true, null);
                } finally {
                    queue.done(entry.getEntry());
                }
                if (result == null) {
                    result = new Reconciler.Result(false, null);
                }
                if (!result.reEnqueue()) {
                    continue;
                }
                var retryAfter = result.retryAfter();
                if (retryAfter == null) {
                    retryAfter = entry.getRetryAfter();
                    if (retryAfter == null
                        || retryAfter.isNegative()
                        || retryAfter.isZero()
                        || retryAfter.compareTo(minDelay) < 0) {
                        // set min retry after
                        retryAfter = minDelay;
                    } else {
                        try {
                            // TODO Refactor the retryAfter with ratelimiter
                            retryAfter = retryAfter.multipliedBy(2);
                        } catch (ArithmeticException e) {
                            retryAfter = maxDelay;
                        }
                    }
                    if (retryAfter.compareTo(maxDelay) > 0) {
                        retryAfter = maxDelay;
                    }
                }
                queue.add(
                    new DelayedEntry<>(entry.getEntry(), retryAfter, nowSupplier));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info("Controller {} interrupted", name);
            }
        }
        log.info("Controller {} is stopped", name);
    }

    @Override
    public void dispose() {
        disposed = true;
        log.info("Disposing controller {}", name);

        synchronizer.dispose();

        executor.shutdownNow();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                log.warn("Wait timeout for controller {} shutdown", name);
            } else {
                log.info("Controller {} is disposed", name);
            }
        } catch (InterruptedException e) {
            log.warn("Interrupted while waiting for controller {} shutdown", name);
        } finally {
            queue.dispose();
        }
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    public boolean isStarted() {
        return started;
    }
}
