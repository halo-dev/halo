package run.halo.app.extension.controller;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import reactor.core.Disposable;

public interface RequestQueue<E> extends Disposable {

    boolean addImmediately(E request);

    boolean add(DelayedEntry<E> entry);

    DelayedEntry<E> take() throws InterruptedException;

    void done(E request);

    long size();

    DelayedEntry<E> peek();

    class DelayedEntry<E> implements Delayed {

        private final E entry;

        private final Instant readyAt;

        private final Supplier<Instant> nowSupplier;

        private final Duration retryAfter;

        DelayedEntry(E entry, Duration retryAfter, Supplier<Instant> nowSupplier) {
            this.entry = entry;
            this.readyAt = nowSupplier.get().plusMillis(retryAfter.toMillis());
            this.nowSupplier = nowSupplier;
            this.retryAfter = retryAfter;
        }

        public DelayedEntry(E entry, Instant readyAt, Supplier<Instant> nowSupplier) {
            this.entry = entry;
            this.readyAt = readyAt;
            this.nowSupplier = nowSupplier;
            this.retryAfter = Duration.between(nowSupplier.get(), readyAt);
        }

        @Override
        public long getDelay(TimeUnit unit) {
            Duration diff = Duration.between(nowSupplier.get(), readyAt);
            return unit.convert(diff);
        }

        public Duration getRetryAfter() {
            return retryAfter;
        }

        @Override
        public int compareTo(Delayed o) {
            return Long.compare(getDelay(TimeUnit.MILLISECONDS), o.getDelay(TimeUnit.MILLISECONDS));
        }

        public E getEntry() {
            return entry;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            DelayedEntry<?> that = (DelayedEntry<?>) o;
            return Objects.equals(entry, that.entry);
        }

        @Override
        public int hashCode() {
            return Objects.hash(entry);
        }
    }
}
