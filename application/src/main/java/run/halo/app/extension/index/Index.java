package run.halo.app.extension.index;

import java.io.Closeable;
import run.halo.app.extension.Extension;

/**
 * Index for Extensions.
 *
 * @param <E> the type of the extension.
 * @param <K> the type of the index key.
 * @author johnniang
 * @since 2.22.0
 */
public interface Index<E extends Extension, K extends Comparable<K>> extends Closeable {

    /**
     * Get the name of the index.
     *
     * @return the name of the index.
     */
    String getName();

    /**
     * Get the type of the index key.
     *
     * @return the type of the index key.
     */
    Class<K> getKeyType();

    /**
     * Whether the index is unique.
     *
     * @return true if the index is unique, false otherwise.
     */
    default boolean isUnique() {
        return false;
    }

    /**
     * Prepare insert operation.
     *
     * @param extension the extension to insert.
     * @return the transactional operation.
     */
    TransactionalOperation prepareInsert(E extension);

    /**
     * Prepare update operation.
     *
     * @param newExtension the new extension.
     * @return the transactional operation.
     */
    TransactionalOperation prepareUpdate(E newExtension);

    /**
     * Prepare delete operation.
     *
     * @param primaryKey the primary key of the extension to delete.
     * @return the transactional operation.
     */
    TransactionalOperation prepareDelete(String primaryKey);

    /**
     * Dumps the content of this index into a serializable snapshot.
     *
     * @return the snapshot of this index
     * @since 2.26.0
     */
    IndexSnapshot dump();

    /**
     * Restores the content of this index from a snapshot. The index must be empty.
     *
     * @param snapshot the snapshot to restore from
     * @since 2.26.0
     */
    void restore(IndexSnapshot snapshot);

    /**
     * Gets the structural fingerprint of this index, used to match snapshots against specs.
     *
     * @return the fingerprint
     * @since 2.26.0
     */
    String getFingerprint();
}
