package run.halo.app.extension.index;

import java.util.Collection;
import java.util.Set;

/**
 * Label index query interface.
 *
 * <p>Implementations must return a new mutable {@link Set} owned by the caller. Callers may modify the returned set, so
 * implementations must never expose a set that is backed by the index itself.
 *
 * @author johnniang
 * @since 2.22.0
 */
public interface LabelIndexQuery {

    /**
     * Checks if the label with the given key exists.
     *
     * @param labelKey the label key
     * @return the set of entity IDs that have the label key
     */
    Set<String> exists(String labelKey);

    /**
     * Checks if the label with the given key does not exist.
     *
     * @param labelKey the label key
     * @param labelValue the label value
     * @return the set of entity IDs that do not have the label key
     */
    Set<String> equal(String labelKey, String labelValue);

    /**
     * Checks if the label with the given key does not equal the specified value.
     *
     * @param labelKey the label key
     * @param labelValue the label value
     * @return the set of entity IDs that do not have the label key equal to the specified value
     */
    Set<String> notEqual(String labelKey, String labelValue);

    /**
     * Checks if the label with the given key has a value in the specified collection.
     *
     * @param labelKey the label key
     * @param labelValues the collection of label values
     * @return the set of entity IDs that have the label key with values in the specified collection
     */
    Set<String> in(String labelKey, Collection<String> labelValues);

    /**
     * Checks if the label with the given key has a value not in the specified collection.
     *
     * @param labelKey the label key
     * @param labelValues the collection of label values
     * @return the set of entity IDs that have the label key with values not in the specified collection.
     */
    Set<String> notIn(String labelKey, Collection<String> labelValues);
}
