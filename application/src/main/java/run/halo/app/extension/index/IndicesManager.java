package run.halo.app.extension.index;

import java.io.Closeable;
import java.util.List;
import run.halo.app.extension.Extension;

public interface IndicesManager extends Closeable {

    /**
     * Add a new {@link Indices} for the given extension type and index specifications.
     *
     * @param type the type of the extension
     * @param specs the list of index specifications
     * @param <E> the type of the extension
     */
    <E extends Extension> void add(Class<E> type, List<ValueIndexSpec<E, ?>> specs);

    /**
     * Get the {@link Indices} for the given extension type.
     *
     * @param type the type of the extension
     * @param <E> the type of the extension
     * @return the indices for the given extension type
     * @throws IllegalArgumentException if the indices for the given extension type does not exist
     */
    <E extends Extension> Indices<E> get(Class<E> type);

    /**
     * Awaits until the indices for the given type are fully built or restored, with a timeout.
     * Indices are ready immediately after {@link #add(Class, List)}; this method blocks only
     * between {@link #markBuilding(Class)} and {@link #markReady(Class)}.
     *
     * @param type the extension type
     * @throws IllegalArgumentException if no indices are registered for the type
     * @throws IllegalStateException if the indices are not ready within the timeout
     * @since 2.26.0
     */
    void awaitReady(Class<? extends Extension> type);

    /**
     * Marks the indices for the given type as building, so that queries block until
     * {@link #markReady(Class)} is called. Call this before (re)building or restoring indices.
     *
     * @param type the extension type
     * @since 2.26.0
     */
    void markBuilding(Class<? extends Extension> type);

    /**
     * Marks the indices for the given type as ready. Idempotent.
     *
     * @param type the extension type
     * @since 2.26.0
     */
    void markReady(Class<? extends Extension> type);

    /**
     * Remove the {@link Indices} for the given extension type and release resources.
     *
     * @param type type of the extension
     * @param <E> type of the extension
     */
    <E extends Extension> void remove(Class<E> type);
}
