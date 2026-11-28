package run.halo.app.extension;

import java.util.Comparator;
import java.util.function.Predicate;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive read-only extension operations shared by source and presentation clients.
 *
 * <p>Source and presentation clients implement the same read contract so presentation support cannot silently omit
 * newly added read operations. Mutation, lifecycle and indexing operations remain on {@link ReactiveExtensionClient}
 * and are deliberately unavailable through presentation clients.
 *
 * <p>This contract is a shared API boundary rather than an application bean injection point. Public display consumers
 * should inject {@link ReactivePresentationExtensionClient}; administration and internal services should inject
 * {@link ReactiveExtensionClient}.
 *
 * @since 2.28.0
 */
public interface ReactiveExtensionReadClient {
    /**
     * Lists extensions by type, filter and sorter.
     *
     * @param type is the class type of the extension
     * @param predicate filters the result
     * @param comparator sorts the result
     * @param <E> is extension type
     * @return all filtered and sorted extensions
     */
    <E extends Extension> Flux<E> list(Class<E> type, Predicate<E> predicate, Comparator<E> comparator);

    /**
     * Lists a page of extensions by type, filter and sorter.
     *
     * @param type is the class type of the extension
     * @param predicate filters the result
     * @param comparator sorts the result
     * @param page is page number which starts from 0
     * @param size is page size
     * @param <E> is extension type
     * @return a page of extensions
     * @deprecated use {@link #listAll(Class, ListOptions, Sort)} instead
     */
    @Deprecated(since = "2.22.0")
    <E extends Extension> Mono<ListResult<E>> list(
            Class<E> type, Predicate<E> predicate, Comparator<E> comparator, int page, int size);

    /**
     * Lists extensions matching the supplied options and sort.
     *
     * @param type is the class type of the extension
     * @param options query options
     * @param sort sort order
     * @param <E> is extension type
     * @return matching extensions
     */
    <E extends Extension> Flux<E> listAll(Class<E> type, ListOptions options, Sort sort);

    /**
     * Lists names of extensions matching the supplied options and sort.
     *
     * @param type is the class type of the extension
     * @param options query options
     * @param sort sort order
     * @param <E> is extension type
     * @return matching extension names
     */
    <E extends Extension> Flux<String> listAllNames(Class<E> type, ListOptions options, Sort sort);

    /**
     * Lists the first names of extensions matching the supplied options and sort.
     *
     * @param type is the class type of the extension
     * @param options query options
     * @param sort sort order
     * @param topN maximum number of names
     * @param <E> is extension type
     * @return matching extension names
     */
    <E extends Extension> Flux<String> listTopNames(Class<E> type, ListOptions options, Sort sort, int topN);

    /**
     * Lists a page of extensions matching the supplied options.
     *
     * @param type is the class type of the extension
     * @param options query options
     * @param page page request
     * @param <E> is extension type
     * @return matching extensions
     */
    <E extends Extension> Mono<ListResult<E>> listBy(Class<E> type, ListOptions options, PageRequest page);

    /**
     * Lists a page of extension names matching the supplied options.
     *
     * @param type is the class type of the extension
     * @param options query options
     * @param page page request
     * @param <E> is extension type
     * @return matching extension names
     */
    <E extends Extension> Mono<ListResult<String>> listNamesBy(Class<E> type, ListOptions options, PageRequest page);

    /**
     * Counts extensions matching the supplied options.
     *
     * @param type is the class type of the extension
     * @param options query options
     * @param <E> is extension type
     * @return matching extension count
     */
    <E extends Extension> Mono<Long> countBy(Class<E> type, ListOptions options);

    /**
     * Fetches an extension by type and name.
     *
     * @param type is the extension type
     * @param name is the extension name
     * @param <E> is extension type
     * @return the extension when present
     */
    <E extends Extension> Mono<E> fetch(Class<E> type, String name);

    /**
     * Fetches an unstructured extension by group, version and kind.
     *
     * @param gvk extension identity
     * @param name extension name
     * @return the extension when present
     */
    Mono<Unstructured> fetch(GroupVersionKind gvk, String name);

    /**
     * Gets an extension by type and name, preserving not-found errors.
     *
     * @param type is the extension type
     * @param name is the extension name
     * @param <E> is extension type
     * @return the extension
     */
    <E extends Extension> Mono<E> get(Class<E> type, String name);
}
