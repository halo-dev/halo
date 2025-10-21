package run.halo.app.extension.index;

import java.io.Closeable;
import java.util.List;
import org.springframework.lang.NonNull;
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
    @NonNull
    <E extends Extension> Indices<E> get(Class<E> type);

    /**
     * Remove the {@link Indices} for the given extension type and release resources.
     *
     * @param type type of the extension
     * @param <E> type of the extension
     */
    <E extends Extension> void remove(Class<E> type);

}
