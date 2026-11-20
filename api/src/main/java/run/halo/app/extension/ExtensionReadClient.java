package run.halo.app.extension;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import org.springframework.data.domain.Sort;

/**
 * Read-only extension operations shared by blocking extension clients.
 *
 * <p>This contract exists so source reads and presentation reads cannot drift while writes remain unavailable to public
 * display clients. It is a shared API contract rather than an application bean injection point; consumers should inject
 * {@link ExtensionClient} for source access.
 *
 * @since 2.28.0
 */
public interface ExtensionReadClient {
    /**
     * Lists extensions by type, filter and sorter.
     *
     * @param type is the class type of the extension
     * @param predicate filters the result
     * @param comparator sorts the result
     * @param <E> is extension type
     * @return all filtered and sorted extensions
     */
    <E extends Extension> List<E> list(Class<E> type, Predicate<E> predicate, Comparator<E> comparator);

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
    <E extends Extension> ListResult<E> list(
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
    <E extends Extension> List<E> listAll(Class<E> type, ListOptions options, Sort sort);

    /**
     * Lists names of extensions matching the supplied options and sort.
     *
     * @param type is the class type of the extension
     * @param options query options
     * @param sort sort order
     * @param <E> is extension type
     * @return matching extension names
     */
    <E extends Extension> List<String> listAllNames(Class<E> type, ListOptions options, Sort sort);

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
    <E extends Extension> List<String> listTopNames(Class<E> type, ListOptions options, Sort sort, int topN);

    /**
     * Lists a page of extensions matching the supplied options.
     *
     * @param type is the class type of the extension
     * @param options query options
     * @param page page request
     * @param <E> is extension type
     * @return matching extensions
     */
    <E extends Extension> ListResult<E> listBy(Class<E> type, ListOptions options, PageRequest page);

    /**
     * Lists a page of extension names matching the supplied options.
     *
     * @param type is the class type of the extension
     * @param options query options
     * @param page page request
     * @param <E> is extension type
     * @return matching extension names
     */
    <E extends Extension> ListResult<String> listNamesBy(Class<E> type, ListOptions options, PageRequest page);

    /**
     * Counts extensions matching the supplied options.
     *
     * @param type is the class type of the extension
     * @param options query options
     * @param <E> is extension type
     * @return matching extension count
     */
    <E extends Extension> long countBy(Class<E> type, ListOptions options);

    /**
     * Fetches an extension by type and name.
     *
     * @param type is the extension type
     * @param name is the extension name
     * @param <E> is extension type
     * @return the extension when present
     */
    <E extends Extension> Optional<E> fetch(Class<E> type, String name);

    /**
     * Fetches an unstructured extension by group, version and kind.
     *
     * @param gvk extension identity
     * @param name extension name
     * @return the extension when present
     */
    Optional<Unstructured> fetch(GroupVersionKind gvk, String name);
}
