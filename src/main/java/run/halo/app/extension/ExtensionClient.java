package run.halo.app.extension;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import org.springframework.data.domain.Page;

/**
 * ExtensionClient is an interface which contains some operations on Extension instead of
 * ExtensionStore.
 *
 * @author johnniang
 */
public interface ExtensionClient {

    /**
     * Lists Extensions by Extension type, filter and sorter.
     *
     * @param type is the class type of Extension.
     * @param predicate filters the result.
     * @param comparator sorts the result.
     * @param <E> is Extension type.
     * @return all filtered and sorted Extensions.
     */
    <E extends Extension> List<E> list(Class<E> type, Predicate<E> predicate,
        Comparator<E> comparator);

    /**
     * Lists Extensions by Extension type, filter, sorter and page info.
     *
     * @param type is the class type of Extension.
     * @param predicate filters the result.
     * @param comparator sorts the result.
     * @param page is page number which starts from 0.
     * @param size is page size.
     * @param <E> is Extension type.
     * @return a page of Extensions.
     */
    <E extends Extension> Page<E> page(Class<E> type, Predicate<E> predicate,
        Comparator<E> comparator, int page, int size);

    /**
     * Fetches Extension by its type and name.
     *
     * @param type is Extension type.
     * @param name is Extension name.
     * @param <E> is Extension type.
     * @return an optional Extension.
     */
    <E extends Extension> Optional<E> fetch(Class<E> type, String name);


    /**
     * Creates an Extension.
     *
     * @param extension is fresh Extension to be created. Please make sure the Extension name does
     * not exist.
     * @param <E> is Extension type.
     */
    <E extends Extension> void create(E extension);

    /**
     * Updates an Extension.
     *
     * @param extension is an Extension to be updated. Please make sure the resource version is
     * latest.
     * @param <E> is Extension type.
     */
    <E extends Extension> void update(E extension);

    /**
     * Deletes an Extension.
     *
     * @param extension is an Extension to be deleted. Please make sure the resource version is
     * latest.
     * @param <E> is Extension type.
     */
    <E extends Extension> void delete(E extension);

}
