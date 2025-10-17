package run.halo.app.extension.index;

import java.util.Collection;
import java.util.Set;

public interface ValueIndexQuery<K extends Comparable<K>> {

    Class<K> getKeyType();

    Set<String> equal(K key);

    Set<String> notEqual(K key);

    Set<String> all();

    Set<String> between(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive);

    Set<String> notBetween(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive);

    Set<String> in(Collection<K> keys);

    Set<String> notIn(Collection<K> keys);

    Set<String> lessThan(K key, boolean inclusive);

    Set<String> greaterThan(K key, boolean inclusive);

    Set<String> isNull();

    Set<String> isNotNull();

    Set<String> stringContains(String keyword);

    Set<String> stringNotContains(String keyword);

    Set<String> stringStartsWith(String prefix);

    Set<String> stringNotStartsWith(String prefix);

    Set<String> stringEndsWith(String suffix);

    Set<String> stringNotEndsWith(String suffix);

}
