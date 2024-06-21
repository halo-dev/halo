package run.halo.app.extension.index;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import run.halo.app.extension.Metadata;

/**
 * <p>{@link IndexEntry} used to store the mapping between index key and
 * {@link Metadata#getName()}.</p>
 * <p>For example, if we have a {@link Metadata} with name {@code foo} and labels {@code bar=1}
 * and {@code baz=2}, then the index entry will be:</p>
 * <pre>
 *     bar=1 -> foo
 *     baz=2 -> foo
 *     </pre>
 * <p>And if we have another {@link Metadata} with name {@code bar} and labels {@code bar=1}
 * and {@code baz=3}, then the index entry will be:</p>
 * <pre>
 *     bar=1 -> foo, bar
 *     baz=2 -> foo
 *     baz=3 -> bar
 * </pre>
 * <p>{@link #getIndexDescriptor()} describes the owner of this index entry.</p>
 * <p>Index entries is ordered by key, and the order is determined by
 * {@link IndexSpec#getOrder()}.</p>
 * <p>Do not modify the returned result for all methods of this class.</p>
 * <p>This class is thread-safe.</p>
 *
 * @author guqing
 * @since 2.12.0
 */
public interface IndexEntry {

    /**
     * Acquires the read lock for reading such as {@link #getObjectNamesBy(String)},
     * {@link #entries()}, {@link #indexedKeys()}, because the returned result set of these
     * methods is not immutable.
     */
    void acquireReadLock();

    /**
     * Releases the read lock.
     */
    void releaseReadLock();

    /**
     * <p>Adds a new entry to this index entry.</p>
     * <p>For example, if we have a {@link Metadata} with name {@code foo} and labels {@code bar=1}
     * and {@code baz=2} and index order is {@link IndexSpec.OrderType#ASC}, then the index entry
     * will be:</p>
     * <pre>
     *     bar=1 -> foo
     *     baz=2 -> foo
     * </pre>
     *
     * @param indexKeys index keys
     * @param objectKey object key (usually is {@link Metadata#getName()}).
     */
    void addEntry(List<String> indexKeys, String objectKey);

    /**
     * Removes the entry with the given {@code indexedKey} and {@code objectKey}.
     *
     * @param indexedKey indexed key
     * @param objectKey object key (usually is {@link Metadata#getName()}).
     */
    void removeEntry(String indexedKey, String objectKey);

    /**
     * Removes all entries with the given {@code objectKey}.
     *
     * @param objectKey object key(usually is {@link Metadata#getName()}).
     */
    void remove(String objectKey);

    /**
     * Returns the {@link IndexDescriptor} of this entry.
     *
     * @return the {@link IndexDescriptor} of this entry.
     */
    IndexDescriptor getIndexDescriptor();

    /**
     * Returns the indexed keys of this entry in order.
     *
     * @return distinct indexed keys of this entry.
     */
    NavigableSet<String> indexedKeys();

    /**
     * <p>Returns the entries of this entry in order.</p>
     * <p>Note That: Any modification to the returned result will affect the original data
     * directly.</p>
     *
     * @return entries of this entry.
     */
    Collection<Map.Entry<String, String>> entries();

    /**
     * <p>Returns the position of the object name in the indexed attribute value mapping for
     * sorting.</p>
     * For example:
     * <pre>
     * metadata.name | field1
     * ------------- | ------
     * foo           | 1
     * bar           | 2
     * baz           | 2
     * </pre>
     * "field1" is the indexed attribute, and the position of the object name in the indexed
     * attribute
     * value mapping for sorting is:
     * <pre>
     * foo -> 0
     * bar -> 1
     * baz -> 1
     * </pre>
     * "bar" and "baz" have the same value, so they have the same position.
     */
    Map<String, Integer> getIdPositionMap();

    /**
     * Returns the object names of this entry in order.
     *
     * @return object names of this entry.
     */
    List<String> getObjectNamesBy(String indexKey);

    void clear();
}
