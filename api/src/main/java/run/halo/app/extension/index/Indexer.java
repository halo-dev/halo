package run.halo.app.extension.index;

import java.util.Iterator;
import java.util.function.Function;
import org.springframework.lang.NonNull;
import run.halo.app.extension.Extension;

/**
 * <p>The {@link Indexer} is owned by the {@link Extension} and is responsible for the lookup and
 * lifetimes of the indexes in a {@link Extension} collection. Every {@link Extension} has
 * exactly one instance of this class.</p>
 * <p>Callers are expected to have acquired the necessary locks while accessing this interface.</p>
 * To inspect the contents of this {@link Indexer}, callers may obtain an iterator from
 * getIndexIterator().
 *
 * @author guqing
 * @since 2.12.0
 */
public interface Indexer {

    /**
     * <p>Index the specified {@link Extension} by {@link IndexDescriptor}s.</p>
     * <p>First, the {@link Indexer} will index the {@link Extension} by the
     * {@link IndexDescriptor}s and record the index entries to {@code IndexerTransaction} and
     * commit the transaction, if any error occurs, the transaction will be rollback to keep the
     * {@link Indexer} consistent.</p>
     *
     * @param extension the {@link Extension} to be indexed
     * @param <E> the type of the {@link Extension}
     */
    <E extends Extension> void indexRecord(E extension);

    /**
     * <p>Update indexes for the specified {@link Extension} by {@link IndexDescriptor}s.</p>
     * <p>First, the {@link Indexer} will remove the index entries of the {@link Extension} by
     * the old {@link IndexDescriptor}s and reindex the {@link Extension} to generate change logs
     * to {@code IndexerTransaction} and commit the transaction, if any error occurs, the
     * transaction will be rollback to keep the {@link Indexer} consistent.</p>
     *
     * @param extension the {@link Extension} to be updated
     * @param <E> the type of the {@link Extension}
     */
    <E extends Extension> void updateRecord(E extension);

    /**
     * <p>Remove indexes (index entries) for the specified {@link Extension} already indexed by
     * {@link IndexDescriptor}s.</p>
     *
     * @param extensionName the {@link Extension} to be removed
     */
    void unIndexRecord(String extensionName);

    /**
     * <p>Find index by name.</p>
     * <p>The index name uniquely identifies an index.</p>
     *
     * @param name index name
     * @return index descriptor if found, null otherwise
     */
    IndexDescriptor findIndexByName(String name);

    /**
     * <p>Create an index entry for the specified {@link IndexDescriptor}.</p>
     *
     * @param descriptor the {@link IndexDescriptor} to be recorded
     * @return the {@link IndexEntry} created
     */
    IndexEntry createIndexEntry(IndexDescriptor descriptor);

    /**
     * <p>Remove all index entries that match the given {@link IndexDescriptor}.</p>
     *
     * @param matchFn the {@link IndexDescriptor} to be matched
     */
    void removeIndexRecords(Function<IndexDescriptor, Boolean> matchFn);

    /**
     * <p>Get the {@link IndexEntry} by index name if found and ready.</p>
     *
     * @param name an index name
     * @return the {@link IndexEntry} if found
     * @throws IllegalArgumentException if the index name is not found or the index is not ready
     */
    @NonNull
    IndexEntry getIndexEntry(String name);

    /**
     * <p>Gets an iterator over all the ready {@link IndexEntry}s, in no particular order.</p>
     *
     * @return an iterator over all the ready {@link IndexEntry}s
     * @see IndexDescriptor#isReady()
     */
    Iterator<IndexEntry> readyIndexesIterator();

    /**
     * <p>Gets an iterator over all the {@link IndexEntry}s, in no particular order.</p>
     *
     * @return an iterator over all the {@link IndexEntry}s
     * @see IndexDescriptor#isReady()
     */
    Iterator<IndexEntry> allIndexesIterator();

    void acquireReadLock();

    void releaseReadLock();
}
