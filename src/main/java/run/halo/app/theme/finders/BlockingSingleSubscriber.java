package run.halo.app.theme.finders;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.Exceptions;
import reactor.core.Scannable;
import reactor.core.scheduler.Schedulers;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

/**
 * see also <code>reactor.core.publisher.BlockingSingleSubscriber</code>.
 *
 * @param <T> value type
 * @author guqing
 * @since 2.0.0
 */
abstract class BlockingSingleSubscriber<T> extends CountDownLatch
    implements CoreSubscriber<T>, Scannable, Disposable {
    T value;
    Throwable error;

    Subscription subscription;

    volatile boolean cancelled;

    BlockingSingleSubscriber() {
        super(1);
    }

    @Override
    public final void onSubscribe(Subscription s) {
        this.subscription = s;
        if (!cancelled) {
            s.request(Long.MAX_VALUE);
        }
    }

    @Override
    public final void onComplete() {
        countDown();
    }

    @Override
    public Context currentContext() {
        return Context.empty();
    }

    @Override
    public final void dispose() {
        cancelled = true;
        Subscription s = this.subscription;
        if (s != null) {
            this.subscription = null;
            s.cancel();
        }
    }

    /**
     * Block until the first value arrives and return it, otherwise
     * return null for an empty source and rethrow any exception.
     *
     * @return the first value or null if the source is empty
     */
    @Nullable
    final T blockingGet() {
        if (getCount() != 0) {
            try {
                await();
            } catch (InterruptedException ex) {
                dispose();
                throw Exceptions.propagate(ex);
            }
        }

        Throwable e = error;
        if (e != null) {
            RuntimeException re = Exceptions.propagate(e);
            //this is ok, as re is always a new non-singleton instance
            re.addSuppressed(new Exception("#block terminated with an error"));
            throw re;
        }
        return value;
    }

    /**
     * Block until the first value arrives and return it, otherwise
     * return null for an empty source and rethrow any exception.
     *
     * @param timeout the timeout to wait
     * @param unit the time unit
     * @return the first value or null if the source is empty
     */
    @Nullable
    final T blockingGet(long timeout, TimeUnit unit) {
        if (Schedulers.isInNonBlockingThread()) {
            throw new IllegalStateException(
                "block()/blockFirst()/blockLast() are blocking, which is not supported in "
                    + "thread "
                    + Thread.currentThread().getName());
        }
        if (getCount() != 0) {
            try {
                if (!await(timeout, unit)) {
                    dispose();
                    throw new IllegalStateException(
                        "Timeout on blocking read for " + timeout + " " + unit);
                }
            } catch (InterruptedException ex) {
                dispose();
                RuntimeException re = Exceptions.propagate(ex);
                //this is ok, as re is always a new non-singleton instance
                re.addSuppressed(new Exception("#block has been interrupted"));
                throw re;
            }
        }

        Throwable e = error;
        if (e != null) {
            RuntimeException re = Exceptions.propagate(e);
            //this is ok, as re is always a new non-singleton instance
            re.addSuppressed(new Exception("#block terminated with an error"));
            throw re;
        }
        return value;
    }


    @Override
    @Nullable
    public Object scanUnsafe(Attr key) {
        if (key == Attr.TERMINATED) {
            return getCount() == 0;
        }
        if (key == Attr.PARENT) {
            return subscription;
        }
        if (key == Attr.CANCELLED) {
            return cancelled;
        }
        if (key == Attr.ERROR) {
            return error;
        }
        if (key == Attr.PREFETCH) {
            return Integer.MAX_VALUE;
        }
        if (key == Attr.RUN_STYLE) {
            return Attr.RunStyle.SYNC;
        }

        return null;
    }

    @Override
    public boolean isDisposed() {
        return cancelled || getCount() == 0;
    }
}
