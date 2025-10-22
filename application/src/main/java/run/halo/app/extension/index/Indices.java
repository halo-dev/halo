package run.halo.app.extension.index;

import java.io.Closeable;
import org.springframework.lang.NonNull;
import run.halo.app.extension.Extension;

public interface Indices<E extends Extension> extends Closeable {

    void insert(E extension);

    void update(E extension);

    void delete(E extension);

    /**
     * Get index by name.
     *
     * @param indexName index name
     * @param <K> the key type
     * @return the index
     * @throws IllegalArgumentException if the index with the given name does not exist
     */
    @NonNull
    <K extends Comparable<K>> Index<E, K> getIndex(String indexName);

}
