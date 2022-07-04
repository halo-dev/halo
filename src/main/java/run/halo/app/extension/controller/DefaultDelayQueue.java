package run.halo.app.extension.controller;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.DelayQueue;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import run.halo.app.extension.controller.Reconciler.Request;

@Slf4j
public class DefaultDelayQueue
    extends DelayQueue<DefaultDelayQueue.DelayedEntry<Request>> implements RequestQueue {

    private final Supplier<Instant> nowSupplier;

    private volatile boolean disposed = false;

    private final Duration minDelay;

    private final Set<Request> processing;

    public DefaultDelayQueue(Supplier<Instant> nowSupplier) {
        this(nowSupplier, Duration.ZERO);
    }

    public DefaultDelayQueue(Supplier<Instant> nowSupplier, Duration minDelay) {
        this.nowSupplier = nowSupplier;
        this.minDelay = minDelay;
        this.processing = new HashSet<>();
    }

    @Override
    public boolean addImmediately(Request request) {
        var delayedEntry = new DelayedEntry<>(request, minDelay, nowSupplier);
        return offer(delayedEntry);
    }

    @Override
    public boolean add(DelayedEntry<Request> entry) {
        if (entry.getRetryAfter().compareTo(minDelay) < 0) {
            log.warn("Request {} will be retried after {} ms, but minimum delay is {} ms",
                entry.getEntry(), entry.getRetryAfter().toMillis(), minDelay.toMillis());
            entry = new DelayedEntry<>(entry.getEntry(), minDelay, nowSupplier);
        }
        return super.add(entry);
    }

    @Override
    public DelayedEntry<Request> take() throws InterruptedException {
        var entry = super.take();
        processing.add(entry.getEntry());
        return entry;
    }

    @Override
    public void done(Request request) {
        processing.remove(request);
    }

    @Override
    public boolean offer(DelayedEntry<Request> entry) {
        if (this.isDisposed() || processing.contains(entry.getEntry())) {
            return false;
        }
        // remove the existing entry before adding the new one
        // to refresh the delay.
        this.remove(entry);
        return super.offer(entry);
    }

    @Override
    public void dispose() {
        this.disposed = true;
        this.clear();
        this.processing.clear();
    }

    @Override
    public boolean isDisposed() {
        return this.disposed;
    }

}
