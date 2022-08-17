package run.halo.app.extension;

import java.util.Comparator;
import java.util.function.Predicate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * ExtensionClient is an interface which contains some operations on Extension instead of
 * ExtensionStore.
 *
 * @author johnniang
 */
public interface ReactiveExtensionClient {

    /**
     * Lists Extensions by Extension type, filter and sorter.
     *
     * @param type is the class type of Extension.
     * @param predicate filters the reEnqueue.
     * @param comparator sorts the reEnqueue.
     * @param <E> is Extension type.
     * @return all filtered and sorted Extensions.
     */
    <E extends Extension> Flux<E> list(Class<E> type, Predicate<E> predicate,
        Comparator<E> comparator);

    /**
     * Lists Extensions by Extension type, filter, sorter and page info.
     *
     * @param type is the class type of Extension.
     * @param predicate filters the reEnqueue.
     * @param comparator sorts the reEnqueue.
     * @param page is page number which starts from 0.
     * @param size is page size.
     * @param <E> is Extension type.
     * @return a list of Extensions.
     */
    <E extends Extension> Mono<ListResult<E>> list(Class<E> type, Predicate<E> predicate,
        Comparator<E> comparator, int page, int size);

    /**
     * Fetches Extension by its type and name.
     *
     * @param type is Extension type.
     * @param name is Extension name.
     * @param <E> is Extension type.
     * @return an optional Extension.
     */
    <E extends Extension> Mono<E> fetch(Class<E> type, String name);

    Mono<Unstructured> fetch(GroupVersionKind gvk, String name);

    <E extends Extension> Mono<E> get(Class<E> type, String name);

    /**
     * Creates an Extension.
     *
     * @param extension is fresh Extension to be created. Please make sure the Extension name does
     * not exist.
     * @param <E> is Extension type.
     */
    <E extends Extension> Mono<E> create(E extension);

    /**
     * Updates an Extension.
     *
     * @param extension is an Extension to be updated. Please make sure the resource version is
     * latest.
     * @param <E> is Extension type.
     */
    <E extends Extension> Mono<E> update(E extension);

    /**
     * Deletes an Extension.
     *
     * @param extension is an Extension to be deleted. Please make sure the resource version is
     * latest.
     * @param <E> is Extension type.
     */
    <E extends Extension> Mono<E> delete(E extension);

    void watch(Watcher watcher);

}
