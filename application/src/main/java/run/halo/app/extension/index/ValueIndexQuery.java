package run.halo.app.extension.index;

import java.util.Collection;
import java.util.Set;

/**
 * Value index query interface.
 *
 * <p>Implementations must return a new mutable {@link Set} owned by the caller. Callers may modify the returned set, so
 * implementations must never expose a set that is backed by the index itself.
 *
 * @param <K> the type of the key
 */
public interface ValueIndexQuery<K extends Comparable<K>> {

    /**
     * Gets the type of the key.
     *
     * @return the class of the key type
     */
    Class<K> getKeyType();

    /**
     * Checks for equality with the given key.
     *
     * @param key the key to compare
     * @return the set of entity IDs that match the equality condition
     */
    Set<String> equal(K key);

    /**
     * Checks for inequality with the given key.
     *
     * @param key the key to compare
     * @return the set of entity IDs that match the inequality condition
     */
    Set<String> notEqual(K key);

    /**
     * Removes the candidates that match the given key from the candidates instead of building the complement over the
     * whole index, and returns the remaining candidates.
     *
     * @param candidates the candidates to refine; must be mutable and owned by the caller
     * @param key the key to compare
     * @return the same candidate set
     */
    Set<String> notEqual(Set<String> candidates, K key);

    /**
     * Gets all entity IDs in the index.
     *
     * @return the set of all entity IDs
     */
    Set<String> all();

    /**
     * Gets entity IDs between the specified range.
     *
     * @param fromKey the starting key
     * @param fromInclusive whether the starting key is inclusive
     * @param toKey the ending key
     * @param toInclusive whether the ending key is inclusive
     * @return the set of entity IDs within the specified range
     */
    Set<String> between(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive);

    /**
     * Gets entity IDs not between the specified range.
     *
     * @param fromKey the starting key
     * @param fromInclusive whether the starting key is inclusive
     * @param toKey the ending key
     * @param toInclusive whether the ending key is inclusive
     * @return the set of entity IDs outside the specified range
     */
    Set<String> notBetween(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive);

    /**
     * Refines the candidates with the negated range check instead of building the complement over the whole index.
     *
     * @param candidates the candidates to refine; must be mutable and owned by the caller
     * @return the same candidate set
     */
    Set<String> notBetween(Set<String> candidates, K fromKey, boolean fromInclusive, K toKey, boolean toInclusive);

    /**
     * Gets entity IDs with keys in the specified collection.
     *
     * @param keys the collection of keys
     * @return the set of entity IDs with keys in the collection
     */
    Set<String> in(Collection<K> keys);

    /**
     * Gets entity IDs with keys not in the specified collection.
     *
     * @param keys the collection of keys
     * @return the set of entity IDs with keys not in the collection
     */
    Set<String> notIn(Collection<K> keys);

    /**
     * Refines the candidates with the negated membership check instead of building the complement over the whole index.
     *
     * @param candidates the candidates to refine; must be mutable and owned by the caller
     * @return the same candidate set
     */
    Set<String> notIn(Set<String> candidates, Collection<K> keys);

    /**
     * Gets entity IDs with keys less than the specified key.
     *
     * @param key the key to compare
     * @param inclusive whether the comparison is inclusive
     * @return the set of entity IDs with keys less than the specified key
     */
    Set<String> lessThan(K key, boolean inclusive);

    /**
     * Gets entity IDs with keys greater than the specified key.
     *
     * @param key the key to compare
     * @param inclusive whether the comparison is inclusive
     * @return the set of entity IDs with keys greater than the specified key
     */
    Set<String> greaterThan(K key, boolean inclusive);

    /**
     * Gets entity IDs with null keys.
     *
     * @return the set of entity IDs with null keys
     * @throws IllegalArgumentException if the key type is not nullable
     */
    Set<String> isNull();

    /**
     * Gets entity IDs with non-null keys.
     *
     * @return the set of entity IDs with non-null keys
     */
    Set<String> isNotNull();

    /**
     * Removes the candidates without a key from the candidates instead of collecting every non-null key in the index.
     *
     * @param candidates the candidates to refine; must be mutable and owned by the caller
     * @return the same candidate set
     */
    Set<String> isNotNull(Set<String> candidates);

    /**
     * Gets entity IDs where the string representation of the key contains the specified keyword.
     *
     * @param keyword the keyword to search for
     * @return the set of entity IDs that contain the keyword
     * @throws IllegalArgumentException if the key type is not String
     */
    Set<String> stringContains(String keyword);

    /**
     * Refines the candidates with the "contains" check instead of scanning the whole index.
     *
     * @param candidates the candidates to refine; must be mutable and owned by the caller
     * @param keyword the keyword to search for
     * @return the same candidate set
     */
    Set<String> stringContains(Set<String> candidates, String keyword);

    /**
     * Gets entity IDs where the string representation of the key does not contain the specified keyword.
     *
     * @param keyword the keyword to search for
     * @return the set of entity IDs that do not contain the keyword
     * @throws IllegalArgumentException if the key type is not String
     */
    Set<String> stringNotContains(String keyword);

    /**
     * Refines the candidates with the negated "contains" check instead of scanning the whole index.
     *
     * @param candidates the candidates to refine; must be mutable and owned by the caller
     * @param keyword the keyword to search for
     * @return the same candidate set
     */
    Set<String> stringNotContains(Set<String> candidates, String keyword);

    /**
     * Gets entity IDs where the string representation of the key starts with the specified prefix.
     *
     * @param prefix the prefix to search for
     * @return the set of entity IDs that start with the prefix
     * @throws IllegalArgumentException if the key type is not String
     */
    Set<String> stringStartsWith(String prefix);

    /**
     * Refines the candidates with the "starts with" check instead of collecting the matching range in the index.
     *
     * @param candidates the candidates to refine; must be mutable and owned by the caller
     * @param prefix the prefix to search for
     * @return the same candidate set
     */
    Set<String> stringStartsWith(Set<String> candidates, String prefix);

    /**
     * Gets entity IDs where the string representation of the key does not start with the specified prefix.
     *
     * @param prefix the prefix to search for
     * @return the set of entity IDs that do not start with the prefix
     * @throws IllegalArgumentException if the key type is not String
     */
    Set<String> stringNotStartsWith(String prefix);

    /**
     * Refines the candidates with the negated "starts with" check instead of collecting the complement in the index.
     *
     * @param candidates the candidates to refine; must be mutable and owned by the caller
     * @param prefix the prefix to search for
     * @return the same candidate set
     */
    Set<String> stringNotStartsWith(Set<String> candidates, String prefix);

    /**
     * Gets entity IDs where the string representation of the key ends with the specified suffix.
     *
     * @param suffix the suffix to search for
     * @return the set of entity IDs that end with the suffix
     * @throws IllegalArgumentException if the key type is not String
     */
    Set<String> stringEndsWith(String suffix);

    /**
     * Refines the candidates with the "ends with" check instead of scanning the whole index.
     *
     * @param candidates the candidates to refine; must be mutable and owned by the caller
     * @param suffix the suffix to search for
     * @return the same candidate set
     */
    Set<String> stringEndsWith(Set<String> candidates, String suffix);

    /**
     * Gets entity IDs where the string representation of the key does not end with the specified suffix.
     *
     * @param suffix the suffix to search for
     * @return the set of entity IDs that do not end with the suffix
     * @throws IllegalArgumentException if the key type is not String
     */
    Set<String> stringNotEndsWith(String suffix);

    /**
     * Refines the candidates with the negated "ends with" check instead of scanning the whole index.
     *
     * @param candidates the candidates to refine; must be mutable and owned by the caller
     * @param suffix the suffix to search for
     * @return the same candidate set
     */
    Set<String> stringNotEndsWith(Set<String> candidates, String suffix);
}
