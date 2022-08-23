package run.halo.app.extension.controller;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.DelayQueue;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultDelayQueue<R>
    extends DelayQueue<DefaultDelayQueue.DelayedEntry<R>> implements RequestQueue<R> {

    private final Supplier<Instant> nowSupplier;

    private volatile boolean disposed = false;

    private final Duration minDelay;

    private final Set<R> processing;

    public DefaultDelayQueue(Supplier<Instant> nowSupplier) {
        this(nowSupplier, Duration.ZERO);
    }

    public DefaultDelayQueue(Supplier<Instant> nowSupplier, Duration minDelay) {
        this.nowSupplier = nowSupplier;
        this.minDelay = minDelay;
        this.processing = new HashSet<>();
    }

    @Override
    public boolean addImmediately(R request) {
        log.debug("Adding request {} immediately", request);
        var delayedEntry = new DelayedEntry<>(request, minDelay, nowSupplier);
        return offer(delayedEntry);
    }

    @Override
    public boolean add(DelayedEntry<R> entry) {
        if (entry.getRetryAfter().compareTo(minDelay) < 0) {
            log.warn("Request {} will be retried after {} ms, but minimum delay is {} ms",
                entry.getEntry(), entry.getRetryAfter().toMillis(), minDelay.toMillis());
            entry = new DelayedEntry<>(entry.getEntry(), minDelay, nowSupplier);
        }
        return super.add(entry);
    }

    @Override
    public DelayedEntry<R> take() throws InterruptedException {
        var entry = super.take();
        processing.add(entry.getEntry());
        return entry;
    }

    @Override
    public void done(R request) {
        processing.remove(request);
    }

    @Override
    public boolean offer(DelayedEntry<R> entry) {
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
