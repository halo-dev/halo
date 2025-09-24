package run.halo.app.extension.controller;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.system.JavaVersion;
import org.springframework.boot.task.SimpleAsyncTaskExecutorBuilder;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
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

    private final Executor executor;

    @Nullable
    private final Synchronizer<R> synchronizer;

    private final Duration minDelay;

    private final Duration maxDelay;

    private final int workerCount;

    private final AtomicLong workerCounter;

    public DefaultController(String name,
        Reconciler<R> reconciler,
        RequestQueue<R> queue,
        Synchronizer<R> synchronizer,
        Supplier<Instant> nowSupplier,
        Duration minDelay,
        Duration maxDelay,
        ExecutorService executor, int workerCount) {
        this(name, reconciler, queue, synchronizer, nowSupplier, minDelay, maxDelay,
            (Executor) executor, workerCount);
    }

    public DefaultController(String name,
        Reconciler<R> reconciler,
        RequestQueue<R> queue,
        Synchronizer<R> synchronizer,
        Supplier<Instant> nowSupplier,
        Duration minDelay,
        Duration maxDelay,
        Executor executor, int workerCount) {
        Assert.isTrue(workerCount > 0, "Worker count must not be less than 1");
        this.name = name;
        this.reconciler = reconciler;
        this.nowSupplier = nowSupplier;
        this.queue = queue;
        this.synchronizer = synchronizer;
        this.minDelay = minDelay;
        this.maxDelay = maxDelay;
        this.executor = executor;
        this.workerCount = workerCount;
        this.workerCounter = new AtomicLong();
    }

    public DefaultController(String name,
        Reconciler<R> reconciler,
        RequestQueue<R> queue,
        Synchronizer<R> synchronizer,
        Duration minDelay,
        Duration maxDelay) {
        this(name, reconciler, queue, synchronizer, Instant::now, minDelay, maxDelay, 1);
    }

    public DefaultController(String name,
        Reconciler<R> reconciler,
        RequestQueue<R> queue,
        Synchronizer<R> synchronizer,
        Duration minDelay,
        Duration maxDelay, int workerCount) {
        this(name, reconciler, queue, synchronizer, Instant::now, minDelay, maxDelay, workerCount);
    }

    public DefaultController(String name,
        Reconciler<R> reconciler,
        RequestQueue<R> queue,
        Synchronizer<R> synchronizer,
        Supplier<Instant> nowSupplier,
        Duration minDelay,
        Duration maxDelay, int workerCount) {
        this(name, reconciler, queue, synchronizer, nowSupplier, minDelay, maxDelay,
            executor(workerCount, name), workerCount);
    }

    private static SimpleAsyncTaskExecutor executor(int workerCount, String name) {
        boolean virtualThreads =
            JavaVersion.getJavaVersion().isEqualOrNewerThan(JavaVersion.TWENTY_ONE);
        return new SimpleAsyncTaskExecutorBuilder()
            .virtualThreads(virtualThreads)
            .concurrencyLimit(workerCount)
            // See https://github.com/spring-projects/spring-framework/issues/35254#issuecomment-3212944107
            // for more
            // After the problem resolved, we can set the task termination timeout
            // .taskTerminationTimeout(Duration.ofSeconds(10))
            .threadNamePrefix(name + "-")
            .build();
    }

    @Override
    public String getName() {
        return name;
    }

    public int getWorkerCount() {
        return workerCount;
    }

    @Override
    public void start() {
        if (isStarted() || isDisposed()) {
            log.warn("Controller {} is already started or disposed.", getName());
            return;
        }
        this.started = true;
        if (synchronizer != null) {
            synchronizer.start();
        }
        log.info("Starting controller {}", name);
        IntStream.range(0, getWorkerCount())
            .mapToObj(i -> new Worker())
            .forEach(executor::execute);
    }

    /**
     * Worker for controller.
     *
     * @author johnniang
     */
    class Worker implements Runnable {

        private final String name;

        Worker() {
            this.name =
                DefaultController.this.getName() + "-worker-" + workerCounter.incrementAndGet();
        }

        public String getName() {
            return name;
        }

        @Override
        public void run() {
            log.info("Controller worker {} started", this.name);
            while (!isDisposed() && !Thread.currentThread().isInterrupted()) {
                try {
                    var entry = queue.take();
                    Reconciler.Result result;
                    try {
                        log.debug("{} >>> Reconciling request {} at {}", this.name,
                            entry.getEntry(),
                            nowSupplier.get());
                        var watch = new StopWatch(this.name + ":reconcile: " + entry.getEntry());
                        watch.start("reconciliation");
                        result = reconciler.reconcile(entry.getEntry());
                        watch.stop();
                        log.debug("{} >>> Reconciled request: {} with result: {}, usage: {}",
                            this.name, entry.getEntry(), result, watch.getTotalTimeMillis());
                    } catch (Throwable t) {
                        result = new Reconciler.Result(true, null);
                        if (t instanceof OptimisticLockingFailureException) {
                            log.warn("Optimistic locking failure when reconciling request: {}/{}",
                                this.name, entry.getEntry());
                        } else if (t instanceof RequeueException re) {
                            log.warn("{}: Requeue {} due to {}",
                                this.name, entry.getEntry(), re.getMessage());
                            result = re.getResult();
                        } else {
                            log.error("Reconciler in " + this.name
                                    + " aborted with an error, re-enqueuing...",
                                t);
                        }
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
                    log.info("Controller worker {} interrupted", name);
                }
            }
            log.info("Controller worker {} is stopped", name);
        }
    }

    @Override
    public void dispose() {
        disposed = true;
        log.info("Disposing controller {}", name);

        if (synchronizer != null) {
            synchronizer.dispose();
        }

        try {
            // we have to check if the executor is an instance of ExecutorService at first.
            // Because ExecutorService extends AutoCloseable interface in Java 21
            if (executor instanceof ExecutorService executorService) {
                closeExecutorService(executorService);
            } else if (executor instanceof AutoCloseable closeable) {
                closeable.close();
                if (Thread.currentThread().isInterrupted()) {
                    log.warn("Wait timeout for controller {} shutdown", name);
                } else {
                    log.info("Controller {} is disposed", name);
                }
            }
        } catch (Exception e) {
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

    /**
     * Close executor service.
     * <br>
     * This method copied from
     * <a href="https://github.com/openjdk/jdk/blob/890adb6410dab4606a4f26a942aed02fb2f55387/src/java.base/share/classes/java/util/concurrent/ExecutorService.java#L410-L429">ExecutorService#close implemented in JDK 21</a>
     *
     * @param executorService executor service to be closed
     */
    // TODO Remove this method and use ExecutorService#close instead after using JDK 21 as the
    // minimum version
    private static void closeExecutorService(ExecutorService executorService) {
        boolean terminated = executorService.isTerminated();
        if (!terminated) {
            executorService.shutdown();
            boolean interrupted = false;
            while (!terminated) {
                try {
                    terminated = executorService.awaitTermination(1L, TimeUnit.MINUTES);
                } catch (InterruptedException e) {
                    if (!interrupted) {
                        executorService.shutdownNow();
                        interrupted = true;
                    }
                }
            }
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
