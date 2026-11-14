package run.halo.app.extension;

import org.springframework.stereotype.Component;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.reactive.TransactionSynchronization;
import org.springframework.transaction.reactive.TransactionSynchronizationManager;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * Registers index operations to be applied after the current transaction commits, so that index mutations never precede
 * the database changes they reflect. If there is no active transaction (e.g. in tests with a pass-through operator),
 * the operation is applied immediately.
 *
 * @since 2.26.0
 */
@Component
public class IndexOperationRegistrar {

    private final Scheduler scheduler;

    public IndexOperationRegistrar() {
        this(Schedulers.boundedElastic());
    }

    /** Also usable in tests with an arbitrary scheduler. */
    IndexOperationRegistrar(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Registers an index operation to be applied after the current transaction commits. If there is no active
     * transaction, the operation is applied immediately, preserving the previous behavior.
     *
     * @param indexOperation the index operation to apply
     * @return a {@link Mono} that completes once the operation has been registered (or applied, when no transaction is
     *     active)
     */
    public Mono<Void> afterCommit(Runnable indexOperation) {
        return TransactionSynchronizationManager.forCurrentTransaction()
                .doOnNext(tsm -> tsm.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public Mono<Void> afterCommit() {
                        return Mono.fromRunnable(indexOperation)
                                .subscribeOn(scheduler)
                                .then();
                    }
                }))
                .then()
                .onErrorResume(
                        NoTransactionException.class,
                        e -> Mono.fromRunnable(indexOperation)
                                .subscribeOn(scheduler)
                                .then());
    }
}
