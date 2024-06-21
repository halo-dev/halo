package run.halo.app.extension.index.query;

import java.util.List;
import java.util.NavigableSet;
import org.springframework.data.domain.Sort;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.index.IndexEntry;
import run.halo.app.extension.index.IndexSpec;

/**
 * <p>A view of an index entries that can be queried.</p>
 * <p>Explanation of naming:</p>
 * <ul>
 *     <li>fieldName: a field of an index, usually {@link IndexSpec#getName()}</li>
 *     <li>fieldValue: a value of a field, e.g. a value of a field "name" could be "foo"</li>
 *     <li>id: the id of an object pointing to object position, see {@link Metadata#getName()}</li>
 * </ul>
 *
 * @author guqing
 * @since 2.12.0
 */
public interface QueryIndexView {

    /**
     * Gets all object ids for a given field name and field value.
     *
     * @param fieldName the field name
     * @param fieldValue the field value
     * @return all indexed object ids associated with the given field name and field value
     * @throws IllegalArgumentException if the field name is not indexed
     */
    NavigableSet<String> findIds(String fieldName, String fieldValue);

    /**
     * Gets all object ids for a given field name without null cells.
     *
     * @param fieldName the field name
     * @return all indexed object ids for the given field name
     * @throws IllegalArgumentException if the field name is not indexed
     */
    NavigableSet<String> getIdsForField(String fieldName);

    /**
     * Gets all object ids in this view.
     *
     * @return all object ids in this view
     */
    NavigableSet<String> getAllIds();

    /**
     * <p>Finds and returns a set of unique identifiers (metadata.name) for entries that have
     * matching values across two fields and where the values are equal.</p>
     * For example:
     * <pre>
     * metadata.name | field1 | field2
     * ------------- | ------ | ------
     * foo           | 1      | 1
     * bar           | 2      | 3
     * baz           | 3      | 3
     * </pre>
     * <code>findMatchingIdsWithEqualValues("field1", "field2")</code> would return ["foo","baz"]
     *
     * @see #findMatchingIdsWithGreaterValues(String, String, boolean)
     * @see #findMatchingIdsWithSmallerValues(String, String, boolean)
     */
    NavigableSet<String> findMatchingIdsWithEqualValues(String fieldName1, String fieldName2);

    /**
     * <p>Finds and returns a set of unique identifiers (metadata.name) for entries that have
     * matching values across two fields, but where the value associated with fieldName1 is
     * greater than the value associated with fieldName2.</p>
     * For example:
     * <pre>
     *     metadata.name | field1 | field2
     *     ------------- | ------ | ------
     *     foo           | 1      | 1
     *     bar           | 2      | 3
     *     baz           | 3      | 3
     *     qux           | 4      | 2
     * </pre>
     * <p><code>findMatchingIdsWithGreaterValues("field1", "field2")</code>would return ["qux"]</p>
     * <p><code>findMatchingIdsWithGreaterValues("field2", "field1")</code>would return ["bar"]</p>
     * <p><code>findMatchingIdsWithGreaterValues("field1", "field2", true)</code>would return
     * ["foo","baz","qux"]</p>
     *
     * @param fieldName1 The field name whose values are compared as the larger values.
     * @param fieldName2 The field name whose values are compared as the smaller values.
     * @param orEqual whether to include equal values
     * @return A result set of ids where the entries in fieldName1 have greater values than those
     * in fieldName2 for entries that have the same id across both fields
     */
    NavigableSet<String> findMatchingIdsWithGreaterValues(String fieldName1, String fieldName2,
        boolean orEqual);

    NavigableSet<String> findIdsGreaterThan(String fieldName, String fieldValue, boolean orEqual);

    /**
     * <p>Finds and returns a set of unique identifiers (metadata.name) for entries that have
     * matching values across two fields, but where the value associated with fieldName1 is
     * less than the value associated with fieldName2.</p>
     * For example:
     * <pre>
     *     metadata.name | field1 | field2
     *     ------------- | ------ | ------
     *     foo           | 1      | 1
     *     bar           | 2      | 3
     *     baz           | 3      | 3
     *     qux           | 4      | 2
     * </pre>
     * <p><code>findMatchingIdsWithSmallerValues("field1", "field2")</code> would return ["bar"]</p>
     * <p><code>findMatchingIdsWithSmallerValues("field2", "field1")</code> would return ["qux"]</p>
     * <p><code>findMatchingIdsWithSmallerValues("field1", "field2", true)</code> would return
     * ["foo","bar","baz"]</p>
     *
     * @param fieldName1 The field name whose values are compared as the smaller values.
     * @param fieldName2 The field name whose values are compared as the larger values.
     * @param orEqual whether to include equal values
     * @return A result set of ids where the entries in fieldName1 have smaller values than those
     * in fieldName2 for entries that have the same id across both fields
     */
    NavigableSet<String> findMatchingIdsWithSmallerValues(String fieldName1, String fieldName2,
        boolean orEqual);

    NavigableSet<String> findIdsLessThan(String fieldName, String fieldValue, boolean orEqual);

    NavigableSet<String> between(String fieldName, String lowerValue, boolean lowerInclusive,
        String upperValue, boolean upperInclusive);

    List<String> sortBy(NavigableSet<String> resultSet, Sort sort);

    IndexEntry getIndexEntry(String fieldName);

    /**
     * Acquire a read lock on the indexer.
     * if you need to operate on more than one {@code IndexEntry} at the same time, you need to
     * lock first.
     *
     * @see #getIndexEntry(String)
     */
    void acquireReadLock();

    void releaseReadLock();
}
