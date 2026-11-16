package run.halo.app.extension.index;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.reactive.TransactionSynchronization;
import org.springframework.transaction.reactive.TransactionSynchronizationManager;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.extension.store.ExtensionStoreRepository;

/** Coordinates snapshot lifecycles with writes, including writes to unregistered resource types. */
@Component
public class IndexSnapshotWriteBarrier {
    public static final String INDEXED_WRITE = IndexSnapshotWriteBarrier.class.getName() + ".indexed";

    private final ReentrantReadWriteLock lifecycles = new ReentrantReadWriteLock();
    private final Map<String, State> states = new HashMap<>();
    private final Set<String> frozen = new HashSet<>();
    private final Set<Lease> active = new HashSet<>();
    private String invalidationReason;

    public synchronized void invalidate() {
        invalidationReason = "database restoration or unknown index state";
    }

    public synchronized boolean isReusable() {
        return invalidationReason == null && states.values().stream().allMatch(s -> s.reason == null);
    }

    synchronized boolean isReusable(String prefix) {
        return invalidationReason == null && state(prefix).reason == null;
    }

    synchronized boolean isInitialized(String prefix) {
        return state(prefix).initialized;
    }

    synchronized void initialized(String prefix) {
        var state = state(prefix);
        state.initialized = true;
        state.consumed = true;
        state.reason = null;
    }

    synchronized boolean hasCheckpoint(String prefix) {
        return state(prefix).initialized && !state(prefix).consumed;
    }

    // Publication may commit even when its acknowledgement fails or times out.
    synchronized void checkpointPublicationStarted(String prefix) {
        state(prefix).consumed = false;
    }

    synchronized void invalidate(String prefix, String reason) {
        state(prefix).reason = reason;
    }

    synchronized String reason(String prefix) {
        return invalidationReason != null ? invalidationReason : state(prefix).reason;
    }

    /** Prevent publication before a restore can modify the database, including captures already in progress. */
    public Mono<Void> invalidateAll() {
        return Mono.fromRunnable(() -> {
                    invalidate();
                    lifecycles.writeLock().lock();
                    try {
                        // Acquiring the lock waits for any earlier capture/registration to finish.
                    } finally {
                        lifecycles.writeLock().unlock();
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    /** The lifecycle thread may wait; reactive writers never block on the gate. */
    Gate freeze(String prefix, Duration timeout) throws InterruptedException {
        lifecycles.readLock().lockInterruptibly();
        boolean added = false;
        try {
            synchronized (this) {
                if (!frozen.add(prefix)) {
                    throw new IllegalStateException("Concurrent index lifecycle for " + prefix);
                }
                added = true;
                long deadline = System.nanoTime() + timeout.toNanos();
                while (active.stream().anyMatch(lease -> matches(prefix, lease.name))) {
                    long remaining = deadline - System.nanoTime();
                    if (remaining <= 0) {
                        invalidate(prefix, "timed out while draining transactions");
                        throw new IllegalStateException("Timed out draining index writes for " + prefix);
                    }
                    wait(Math.max(1, Duration.ofNanos(remaining).toMillis()));
                }
            }
            return new Gate(prefix);
        } catch (RuntimeException | InterruptedException e) {
            synchronized (this) {
                if (added) {
                    frozen.remove(prefix);
                }
            }
            lifecycles.readLock().unlock();
            throw e;
        }
    }

    final class Gate implements AutoCloseable {
        private final String prefix;

        Gate(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public void close() {
            synchronized (IndexSnapshotWriteBarrier.this) {
                frozen.remove(prefix);
            }
            lifecycles.readLock().unlock();
        }
    }

    public <T> Mono<T> track(String name, ExtensionStoreRepository repository, Supplier<Mono<T>> operation) {
        return Mono.deferContextual(context -> Mono.usingWhen(
                Mono.fromSupplier(() -> acquire(name)),
                lease -> TransactionSynchronizationManager.forCurrentTransaction()
                        .filter(TransactionSynchronizationManager::isSynchronizationActive)
                        .doOnNext(manager -> {
                            manager.registerSynchronization(new TransactionSynchronization() {
                                @Override
                                public Mono<Void> afterCompletion(int status) {
                                    return Mono.fromRunnable(() -> {
                                        if (status == STATUS_UNKNOWN) {
                                            lease.invalidate("transaction completion is unknown");
                                        } else if (status == STATUS_ROLLED_BACK && lease.storeEmitted) {
                                            lease.invalidate("transaction rolled back after a store result");
                                        } else if (status == STATUS_COMMITTED) {
                                            lease.consumed();
                                        }
                                        lease.release();
                                    });
                                }
                            });
                            lease.transactional = true;
                        })
                        .thenReturn(true)
                        .onErrorResume(NoTransactionException.class, ignored -> Mono.just(false))
                        .flatMap(ignored -> {
                            if (!lease.transactional || !context.getOrDefault(INDEXED_WRITE, false)) {
                                lease.invalidate("store write without tracked index maintenance");
                            }
                            return invalidateCheckpoint(name, repository)
                                    .then(Mono.defer(operation))
                                    .doOnNext(result -> lease.storeEmitted = true);
                        }),
                Lease::releaseUnlessTransactional,
                (lease, error) -> lease.releaseUnlessTransactional(),
                Lease::releaseUnlessTransactional));
    }

    private synchronized Lease acquire(String name) {
        if (frozen.stream().anyMatch(prefix -> matches(prefix, name))) {
            throw new IllegalStateException("Index lifecycle is draining writes for " + name);
        }
        var lease = new Lease(name);
        active.add(lease);
        return lease;
    }

    private Mono<Void> invalidateCheckpoint(String name, ExtensionStoreRepository repository) {
        synchronized (this) {
            var matching = states.entrySet().stream()
                    .filter(e -> matches(e.getKey(), name))
                    .toList();
            if (!matching.isEmpty() && matching.stream().allMatch(e -> e.getValue().consumed)) {
                return Mono.empty();
            }
        }
        // A resource name may itself contain slashes. Invalidate every possible storage prefix,
        // including types not registered in this process (e.g. a disabled plugin after restart).
        var checkpoints = new ArrayList<String>();
        for (int end = name.lastIndexOf('/'); end > 0; end = name.lastIndexOf('/', end - 1)) {
            checkpoints.add(IndexSnapshotManager.checkpointName(name.substring(0, end)));
        }
        return checkpoints.isEmpty() ? Mono.empty() : repository.deleteAllById(checkpoints);
    }

    private State state(String prefix) {
        return states.computeIfAbsent(prefix, ignored -> new State());
    }

    private static boolean matches(String prefix, String name) {
        return name.startsWith(prefix + "/");
    }

    private static class State {
        boolean initialized;
        boolean consumed;
        String reason;
    }

    private class Lease {
        final String name;
        final AtomicBoolean released = new AtomicBoolean();
        boolean transactional;
        volatile boolean storeEmitted;

        Lease(String name) {
            this.name = name;
        }

        void invalidate(String reason) {
            synchronized (IndexSnapshotWriteBarrier.this) {
                states.forEach((prefix, state) -> {
                    if (matches(prefix, name)) {
                        state.reason = reason;
                    }
                });
            }
        }

        void consumed() {
            synchronized (IndexSnapshotWriteBarrier.this) {
                states.forEach((prefix, state) -> {
                    if (matches(prefix, name)) {
                        state.consumed = true;
                    }
                });
            }
        }

        Mono<Void> releaseUnlessTransactional() {
            return Mono.fromRunnable(() -> {
                if (!transactional) {
                    release();
                }
            });
        }

        void release() {
            if (released.compareAndSet(false, true)) {
                synchronized (IndexSnapshotWriteBarrier.this) {
                    active.remove(this);
                    IndexSnapshotWriteBarrier.this.notifyAll();
                }
            }
        }
    }
}
