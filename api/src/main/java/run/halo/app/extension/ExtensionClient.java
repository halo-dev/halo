package run.halo.app.extension;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * ExtensionClient is an interface which contains some operations on Extension instead of
 * ExtensionStore.
 * <br/><br/>
 * Please note that this client can only use in non-reactive environment. If you want to
 * use Extension client in reactive environment, please use {@link ReactiveExtensionClient} instead.
 *
 * @author johnniang
 */
public interface ExtensionClient {

    /**
     * Lists Extensions by Extension type, filter and sorter.
     *
     * @param type       is the class type of Extension.
     * @param predicate  filters the reEnqueue.
     * @param comparator sorts the reEnqueue.
     * @param <E>        is Extension type.
     * @return all filtered and sorted Extensions.
     */
    <E extends Extension> List<E> list(Class<E> type, Predicate<E> predicate,
        Comparator<E> comparator);

    /**
     * Lists Extensions by Extension type, filter, sorter and page info.
     *
     * @param type       is the class type of Extension.
     * @param predicate  filters the reEnqueue.
     * @param comparator sorts the reEnqueue.
     * @param page       is page number which starts from 0.
     * @param size       is page size.
     * @param <E>        is Extension type.
     * @return a list of Extensions.
     */
    <E extends Extension> ListResult<E> list(Class<E> type, Predicate<E> predicate,
        Comparator<E> comparator, int page, int size);

    /**
     * Fetches Extension by its type and name.
     *
     * @param type is Extension type.
     * @param name is Extension name.
     * @param <E>  is Extension type.
     * @return an optional Extension.
     */
    <E extends Extension> Optional<E> fetch(Class<E> type, String name);

    Optional<Unstructured> fetch(GroupVersionKind gvk, String name);


    /**
     * Creates an Extension.
     *
     * @param extension is fresh Extension to be created. Please make sure the Extension name does
     *                  not exist.
     * @param <E>       is Extension type.
     */
    <E extends Extension> void create(E extension);

    /**
     * Updates an Extension.
     *
     * @param extension is an Extension to be updated. Please make sure the resource version is
     *                  latest.
     * @param <E>       is Extension type.
     */
    <E extends Extension> void update(E extension);

    /**
     * Deletes an Extension.
     *
     * @param extension is an Extension to be deleted. Please make sure the resource version is
     *                  latest.
     * @param <E>       is Extension type.
     */
    <E extends Extension> void delete(E extension);

    void watch(Watcher watcher);

}
