package run.halo.app.extension.index;

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.extension.Extension;

/**
 * Single value index implementation.
 *
 * @param <E> the type of extension
 * @param <K> the type of index key
 * @author johnniang
 * @since 2.22.0
 */
class SingleValueIndex<E extends Extension, K extends Comparable<K>>
    implements ValueIndexQuery<K>, Index<E, K> {

    private final ConcurrentNavigableMap<K, Set<String>> index;

    private final ConcurrentMap<String, K> invertedIndex;

    private final Set<String> nullKeyValues;

    private final SingleValueIndexSpec<E, K> spec;

    public SingleValueIndex(SingleValueIndexSpec<E, K> spec) {
        this.spec = spec;
        this.index = new ConcurrentSkipListMap<>(Comparator.naturalOrder());
        this.invertedIndex = new ConcurrentHashMap<>();
        this.nullKeyValues = ConcurrentHashMap.newKeySet();
    }

    @Override
    public void close() throws IOException {
        this.index.clear();
        this.invertedIndex.clear();
        this.nullKeyValues.clear();
    }

    @Override
    public Class<K> getKeyType() {
        return spec.getKeyType();
    }

    @Override
    public Set<String> equal(K key) {
        var primaryKeys = index.get(key);
        return CollectionUtils.isEmpty(primaryKeys) ? Set.of() : new HashSet<>(primaryKeys);
    }

    @Override
    public Set<String> notEqual(K key) {
        return index.entrySet().stream()
            .filter(entry -> !Objects.equals(entry.getKey(), key))
            .map(Map.Entry::getValue)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    @Override
    public Set<String> all() {
        return invertedIndex.keySet();
    }

    @Override
    public Set<String> between(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
        Assert.notNull(fromKey, "From key must not be null");
        Assert.notNull(toKey, "To key must not be null");
        Assert.isTrue(fromKey.compareTo(toKey) <= 0,
            "From key must be less than or equal to to key"
        );
        return index.subMap(fromKey, fromInclusive, toKey, toInclusive).values()
            .stream()
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    @Override
    public Set<String> notBetween(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
        Assert.notNull(fromKey, "From key must not be null");
        Assert.notNull(toKey, "To key must not be null");
        Assert.isTrue(fromKey.compareTo(toKey) <= 0,
            "From key must be less than or equal to to key"
        );
        return Stream.concat(
                index.headMap(fromKey, !fromInclusive).values().stream(),
                index.tailMap(toKey, !toInclusive).values().stream()
            )
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    @Override
    public Set<String> in(Collection<K> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return Set.of();
        }
        return keys.stream()
            .distinct()
            .map(index::get)
            .filter(Objects::nonNull)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    @Override
    public Set<String> notIn(Collection<K> keys) {
        if (CollectionUtils.isEmpty(keys)) {
            return Set.of();
        }
        var keySet = keys instanceof Set<K> set ? set : new HashSet<>(keys);
        return index.entrySet().stream()
            .distinct()
            .filter(entry -> !keySet.contains(entry.getKey()))
            .map(Map.Entry::getValue)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    @Override
    public Set<String> lessThan(K key, boolean inclusive) {
        Assert.notNull(key, "Key must not be null");
        return index.headMap(key, inclusive).values().stream()
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    @Override
    public Set<String> greaterThan(K key, boolean inclusive) {
        Assert.notNull(key, "Key must not be null");
        return index.tailMap(key, inclusive).values().stream()
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    @Override
    public Set<String> isNull() {
        Assert.isTrue(spec.isNullable(), "Index " + getName() + " is not nullable");
        return new HashSet<>(nullKeyValues);
    }

    @Override
    public Set<String> isNotNull() {
        Assert.isTrue(spec.isNullable(), "Index is not nullable");
        return index.values().stream()
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    @Override
    public Set<String> stringContains(String keyword) {
        ensureStringKeyType();
        return index.entrySet().stream()
            .filter(entry -> StringUtils.containsIgnoreCase(entry.getKey().toString(), keyword))
            .map(Map.Entry::getValue)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    @Override
    public Set<String> stringNotContains(String keyword) {
        ensureStringKeyType();
        return index.entrySet().stream()
            .filter(entry -> !StringUtils.containsIgnoreCase(entry.getKey().toString(), keyword))
            .map(Map.Entry::getValue)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    @Override
    public Set<String> stringStartsWith(String prefix) {
        ensureStringKeyType();
        var toKey = prefix + Character.MAX_VALUE;
        return index.subMap((K) prefix, true, (K) toKey, true).values().stream()
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    @Override
    public Set<String> stringNotStartsWith(String prefix) {
        ensureStringKeyType();
        var toKey = prefix + Character.MAX_VALUE;
        return Stream.concat(
                index.headMap((K) prefix, false).values().stream(),
                index.tailMap((K) toKey, true).values().stream()
            )
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    @Override
    public Set<String> stringEndsWith(String suffix) {
        ensureStringKeyType();
        return index.entrySet().stream()
            .filter(entry -> StringUtils.endsWithIgnoreCase(entry.getKey().toString(), suffix))
            .map(Map.Entry::getValue)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    @Override
    public Set<String> stringNotEndsWith(String suffix) {
        ensureStringKeyType();
        return index.entrySet().stream()
            .filter(entry -> !StringUtils.endsWithIgnoreCase(entry.getKey().toString(), suffix))
            .map(Map.Entry::getValue)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    @Override
    public String getName() {
        return spec.getName();
    }

    @Override
    public TransactionalOperation prepareInsert(E extension) {
        var primaryKey = extension.getMetadata().getName();
        var key = spec.getValue(extension);
        return new UpsertTransactionalOperation(primaryKey, key);
    }

    @Override
    public TransactionalOperation prepareUpdate(E extension) {
        var primaryKey = extension.getMetadata().getName();
        var key = spec.getValue(extension);
        return new UpsertTransactionalOperation(primaryKey, key);
    }

    @Override
    public TransactionalOperation prepareDelete(String primaryKey) {
        return new DeleteTransactionalOperation(primaryKey);
    }

    /**
     * Get key for the given primary key.
     *
     * @param primaryKey the primary key
     * @return the index key, or null if not found
     */
    @Nullable
    K getKey(String primaryKey) {
        return invertedIndex.get(primaryKey);
    }

    @Override
    public boolean isUnique() {
        return spec.isUnique();
    }

    class UpsertTransactionalOperation implements TransactionalOperation {

        private final String primaryKey;

        @Nullable
        private final K newKey;

        private K previousKey;

        private boolean previousNull;

        private boolean committed;

        UpsertTransactionalOperation(String primaryKey, @Nullable K newKey) {
            this.primaryKey = primaryKey;
            this.newKey = newKey;
        }

        @Override
        public void prepare() {
            // preflight checks
            if (!spec.isNullable() && newKey == null) {
                throw new IllegalArgumentException(
                    "Index %s of %s is not nullable".formatted(getName(), primaryKey)
                );
            }
            previousKey = invertedIndex.get(primaryKey);
            previousNull = nullKeyValues.contains(primaryKey);
            if (isUnique() && newKey != null && !Objects.equals(previousKey, newKey)) {
                var existingPrimaryKeys = index.get(newKey);
                if (!CollectionUtils.isEmpty(existingPrimaryKeys)) {
                    throw new DuplicateKeyException(
                        "Duplicate key '" + newKey + "' for index '" + getName() + "'"
                    );
                }
            }
        }

        @Override
        public void commit() {
            if (committed) {
                return;
            }
            committed = true;
            removeKey(primaryKey, previousKey);
            addKey(primaryKey, newKey);
        }

        @Override
        public void rollback() {
            if (!committed) {
                return;
            }
            removeKey(primaryKey, newKey);
            if (spec.isNullable() || previousKey != null) {
                addKey(primaryKey, previousKey);
            }
            if (previousNull) {
                nullKeyValues.add(primaryKey);
            } else {
                nullKeyValues.remove(primaryKey);
            }
        }

    }

    class DeleteTransactionalOperation implements TransactionalOperation {

        private final String primaryKey;

        private K previousKey;

        private boolean previousNull;

        private boolean committed;

        DeleteTransactionalOperation(String primaryKey) {
            this.primaryKey = primaryKey;
        }

        @Override
        public void prepare() {
            previousKey = invertedIndex.get(primaryKey);
            previousNull = nullKeyValues.contains(primaryKey);
        }

        @Override
        public void commit() {
            if (committed) {
                return;
            }
            committed = true;
            removeKey(primaryKey, previousKey);
        }

        @Override
        public void rollback() {
            if (!committed) {
                return;
            }
            if (spec.isNullable() || previousKey != null) {
                addKey(primaryKey, previousKey);
            }
            if (previousNull) {
                nullKeyValues.add(primaryKey);
            } else {
                nullKeyValues.remove(primaryKey);
            }
        }

    }

    private void removeKey(String primaryKey, K key) {
        nullKeyValues.remove(primaryKey);
        if (key == null) {
            var oldKey = invertedIndex.remove(primaryKey);
            if (oldKey != null) {
                index.computeIfPresent(oldKey, (k, v) -> {
                    v.remove(primaryKey);
                    return v.isEmpty() ? null : v;
                });
            }
            return;
        }
        index.computeIfPresent(key, (k, v) -> {
            v.remove(primaryKey);
            return v.isEmpty() ? null : v;
        });
        invertedIndex.remove(primaryKey, key);
    }

    private void addKey(String primaryKey, K key) {
        if (!spec.isNullable() && key == null) {
            throw new IllegalArgumentException(
                "Index %s of %s is not nullable".formatted(getName(), primaryKey)
            );
        }
        if (key == null) {
            var oldKey = invertedIndex.remove(primaryKey);
            if (oldKey != null) {
                index.computeIfPresent(oldKey, (k, v) -> {
                    v.remove(primaryKey);
                    return v.isEmpty() ? null : v;
                });
            }
            nullKeyValues.add(primaryKey);
            return;
        }
        nullKeyValues.remove(primaryKey);
        index.compute(key, (k, v) -> {
            if (v == null) {
                v = ConcurrentHashMap.newKeySet();
            }
            if (!v.add(primaryKey) && spec.isUnique()) {
                throw new DuplicateKeyException(
                    "Duplicate key '" + key + "' for index '" + getName() + "'"
                );
            }
            return v;
        });
        invertedIndex.put(primaryKey, key);
    }

    private void ensureStringKeyType() {
        Assert.isTrue(
            getKeyType() == String.class || getKeyType() == UnknownKey.class,
            "Key type must be String for this operation"
        );
    }
}
