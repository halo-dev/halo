package run.halo.app.extension.controller;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultQueue<R> implements RequestQueue<R> {

    private final Lock lock;

    private final DelayQueue<DelayedEntry<R>> queue;

    private final Supplier<Instant> nowSupplier;

    private volatile boolean disposed = false;

    private final Duration minDelay;

    private final Set<R> processing;

    private final Set<R> dirty;

    public DefaultQueue(Supplier<Instant> nowSupplier) {
        this(nowSupplier, Duration.ZERO);
    }

    public DefaultQueue(Supplier<Instant> nowSupplier, Duration minDelay) {
        this.lock = new ReentrantLock();
        this.nowSupplier = nowSupplier;
        this.minDelay = minDelay;
        this.processing = new HashSet<>();
        this.dirty = new HashSet<>();
        this.queue = new DelayQueue<>();
    }

    @Override
    public boolean addImmediately(R request) {
        log.debug("Adding request {} immediately", request);
        var delayedEntry = new DelayedEntry<>(request, minDelay, nowSupplier);
        return add(delayedEntry);
    }

    @Override
    public boolean add(DelayedEntry<R> entry) {
        lock.lock();
        try {
            if (isDisposed()) {
                return false;
            }
            log.debug("Adding request {} after {}", entry.getEntry(), entry.getRetryAfter());
            if (entry.getRetryAfter().compareTo(minDelay) < 0) {
                log.warn("Request {} will be retried after {} ms, but minimum delay is {} ms",
                    entry.getEntry(), entry.getRetryAfter().toMillis(), minDelay.toMillis());
                entry = new DelayedEntry<>(entry.getEntry(), minDelay, nowSupplier);
            }
            if (dirty.contains(entry.getEntry())) {
                return false;
            }
            dirty.add(entry.getEntry());
            if (processing.contains(entry.getEntry())) {
                return false;
            }

            boolean added = queue.add(entry);
            log.debug("Added request {} after {}", entry.getEntry(), entry.getRetryAfter());
            return added;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public DelayedEntry<R> take() throws InterruptedException {
        var entry = queue.take();
        log.debug("Take request {} at {}", entry.getEntry(), Instant.now());
        lock.lockInterruptibly();
        try {
            if (isDisposed()) {
                throw new InterruptedException(
                    "Queue has been disposed. Cannot take any elements now");
            }
            processing.add(entry.getEntry());
            dirty.remove(entry.getEntry());
            return entry;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void done(R request) {
        lock.lock();
        try {
            if (isDisposed()) {
                return;
            }
            processing.remove(request);
            if (dirty.contains(request)) {
                queue.add(new DelayedEntry<>(request, minDelay, nowSupplier));
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public long size() {
        return queue.size();
    }

    @Override
    public DelayedEntry<R> peek() {
        return queue.peek();
    }

    @Override
    public void dispose() {
        lock.lock();
        try {
            disposed = true;
            queue.clear();
            processing.clear();
            dirty.clear();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isDisposed() {
        return this.disposed;
    }

}
