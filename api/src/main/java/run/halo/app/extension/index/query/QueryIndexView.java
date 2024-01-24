package run.halo.app.extension.index.query;

import java.util.List;
import java.util.NavigableSet;
import org.springframework.data.domain.Sort;
import run.halo.app.extension.Metadata;
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
    NavigableSet<String> getIdsForFieldValue(String fieldName, String fieldValue);

    /**
     * Gets all field values for a given field name.
     *
     * @param fieldName the field name
     * @return all field values for the given field name
     * @throws IllegalArgumentException if the field name is not indexed
     */
    NavigableSet<String> getAllValuesForField(String fieldName);

    /**
     * Gets all object ids for a given field name without null cells.
     *
     * @param fieldName the field name
     * @return all indexed object ids for the given field name
     * @throws IllegalArgumentException if the field name is not indexed
     */
    NavigableSet<String> getAllIdsForField(String fieldName);

    /**
     * Gets all object ids in this view.
     *
     * @return all object ids in this view
     */
    NavigableSet<String> getAllIds();

    NavigableSet<String> findIdsForFieldValueEqual(String fieldName1, String fieldName2);

    NavigableSet<String> findIdsForFieldValueGreaterThan(String fieldName1, String fieldName2,
        boolean orEqual);

    NavigableSet<String> findIdsForFieldValueLessThan(String fieldName1, String fieldName2,
        boolean orEqual);

    void removeByIdNotIn(NavigableSet<String> ids);

    List<String> sortBy(Sort sort);
}
