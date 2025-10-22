package run.halo.app.extension.index;

import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequest;

/**
 * Index engine for managing extension indices.
 *
 * @author johnniang
 * @since 2.22.0
 */
public interface IndexEngine {

    /**
     * Insert extensions into the index.
     *
     * @param extensions the extensions to insert
     * @param <E> the type of the extension
     */
    <E extends Extension> void insert(@NonNull Iterable<E> extensions);

    /**
     * Update extension in the index.
     *
     * @param extension the extension to update
     * @param <E> the type of the extension
     */
    <E extends Extension> void update(@NonNull Iterable<E> extension);

    /**
     * Delete extensions from the index.
     *
     * @param extensions the extensions to delete
     * @param <E> the type of the extension
     */
    <E extends Extension> void delete(@NonNull Iterable<E> extensions);

    /**
     * Retrieve extension names from the index.
     *
     * @param type the type of the extension
     * @param options the list options
     * @param page the page request
     * @param <E> the type of the extension
     * @return the list result of extension names
     */
    <E extends Extension> ListResult<String> retrieve(
        Class<E> type, @Nullable ListOptions options, @NonNull PageRequest page
    );

    /**
     * Retrieve all extension names from the index.
     *
     * @param type the type of the extension
     * @param options the list options
     * @param sort the sort options
     * @param <E> the type of the extension
     * @return the iterable of extension names
     */
    <E extends Extension> Iterable<String> retrieveAll(
        Class<E> type, @Nullable ListOptions options, @Nullable Sort sort
    );

    /**
     * Retrieve top N extension names from the index.
     *
     * @param type the type of the extension
     * @param options the list options
     * @param sort the sort options
     * @param topN the number of top extensions to retrieve
     * @param <E> the type of the extension
     * @return the iterable of extension names
     */
    <E extends Extension> Iterable<String> retrieveTopN(
        Class<E> type, @Nullable ListOptions options, @Nullable Sort sort, int topN
    );

    /**
     * Count the number of extensions in the index.
     *
     * @param type the type of the extension
     * @param options the list options
     * @param <E> the type of the extension
     * @return the count of extensions
     */
    <E extends Extension> long count(Class<E> type, ListOptions options);

    /**
     * Get the indices' manager.
     *
     * @return the indices manager
     */
    @NonNull
    IndicesManager getIndicesManager();

}
