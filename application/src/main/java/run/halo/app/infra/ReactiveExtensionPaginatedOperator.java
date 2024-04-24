package run.halo.app.infra;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ListOptions;

/**
 * Reactive extension paginated operator to handle extensions by pagination.
 *
 * @author guqing
 * @since 2.15.0
 */
public interface ReactiveExtensionPaginatedOperator {

    /**
     * <p>Deletes all data, including any new entries added during the execution of this method.</p>
     * <p>This method continuously monitors and removes data that appears throughout its runtime,
     * ensuring that even data created during the deletion process is also removed.</p>
     */
    <E extends Extension> Mono<Void> deleteContinuously(Class<E> type,
        ListOptions listOptions);

    /**
     * <p>Deletes only the data that existed at the start of the operation.</p>
     * <p>This method takes a snapshot of the data at the beginning and deletes only that dataset;
     * any data added after the method starts will not be affected or removed.</p>
     */
    <E extends Extension> Flux<E> deleteInitialBatch(Class<E> type,
        ListOptions listOptions);

    /**
     * <p>Note that: This method can not be used for <code>deletion</code> operation, because
     * deletion operation will change the total records.</p>
     */
    <E extends Extension> Flux<E> list(Class<E> type, ListOptions listOptions);
}
