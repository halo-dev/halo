package run.halo.app.extension.index;

import java.io.Closeable;
import java.util.Map;
import java.util.Set;
import run.halo.app.extension.Extension;

public interface Indices<E extends Extension> extends Closeable {

    void insert(E extension);

    void update(E extension);

    void delete(E extension);

    /**
     * Deletes all index entries for the given primary key.
     *
     * @param primaryKey the primary key
     * @since 2.26.0
     */
    void deleteByName(String primaryKey);

    /**
     * Updates only the named indices with the given extension (upsert semantics).
     *
     * @param extension the extension
     * @param indexNames the names of the indices to update
     * @since 2.26.0
     */
    void updateIndices(E extension, Set<String> indexNames);

    /**
     * Dumps all indices and the name-to-version manifest into a snapshot. The manifest is captured BEFORE the indices
     * so that a torn dump always errs on the safe side (manifest version &lt;= index content); see the design doc
     * section 6.3.
     *
     * @return the snapshot
     * @since 2.26.0
     */
    IndicesSnapshot dump();

    /**
     * Restores indices (matched by name) and the manifest from a snapshot.
     *
     * @param snapshot the snapshot; indices not present here are left untouched (empty)
     * @since 2.26.0
     */
    void restore(IndicesSnapshot snapshot);

    /**
     * Gets the current fingerprint of each index, keyed by index name.
     *
     * @return map of index name to fingerprint
     * @since 2.26.0
     */
    Map<String, String> currentFingerprints();

    /**
     * Get index by name.
     *
     * @param indexName index name
     * @param <K> the key type
     * @return the index
     * @throws IllegalArgumentException if the index with the given name does not exist
     */
    <K extends Comparable<K>> Index<E, K> getIndex(String indexName);
}
