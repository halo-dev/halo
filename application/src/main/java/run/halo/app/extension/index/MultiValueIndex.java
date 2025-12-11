package run.halo.app.extension.index;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.extension.Extension;

/**
 * Multi-value index implementation.
 *
 * @param <E> the extension type
 * @param <K> the key type
 * @author johnniang
 * @since 2.22.0
 */
@Slf4j
class MultiValueIndex<E extends Extension, K extends Comparable<K>>
    implements ValueIndexQuery<K>, Index<E, K> {

    private final ConcurrentNavigableMap<K, Set<String>> index;

    private final ConcurrentMap<String, Set<K>> invertedIndex;

    private final Set<String> nullKeyValues;

    private final MultiValueIndexSpec<E, K> spec;

    public MultiValueIndex(MultiValueIndexSpec<E, K> spec) {
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
    public String getName() {
        return spec.getName();
    }

    @Override
    public Class<K> getKeyType() {
        return spec.getKeyType();
    }

    @Override
    public boolean isUnique() {
        return spec.isUnique();
    }

    @Override
    public TransactionalOperation prepareInsert(E extension) {
        var keys = spec.getValues(extension);
        return new UpsertTransactionalOperation(extension.getMetadata().getName(), keys);
    }

    @Override
    public TransactionalOperation prepareUpdate(E extension) {
        // find old state
        var newKeys = spec.getValues(extension);
        var primaryKey = extension.getMetadata().getName();
        return new UpsertTransactionalOperation(primaryKey, newKeys);
    }

    @Override
    public TransactionalOperation prepareDelete(String primaryKey) {
        return new DeleteTransactionalOperation(primaryKey);
    }

    /**
     * Get the keys associated with the given primary key.
     *
     * @param primaryKey the primary key
     * @return the associated keys
     */
    Set<K> getKeys(String primaryKey) {
        return Collections.unmodifiableSet(invertedIndex.get(primaryKey));
    }

    @Override
    public Set<String> between(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
        Assert.isTrue(fromKey.compareTo(toKey) < 0, "fromKey must be less than toKey");
        return index.subMap(fromKey, fromInclusive, toKey, toInclusive)
            .values()
            .stream()
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    @Override
    public Set<String> notBetween(K fromKey, boolean fromInclusive, K toKey, boolean toInclusive) {
        // check if fromKey is less than toKey
        Assert.isTrue(fromKey.compareTo(toKey) < 0, "fromKey must be less than toKey");
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
            return all();
        }
        var keySet = keys instanceof Set<K> ks ? ks : new HashSet<>(keys);
        return index.entrySet().stream()
            .filter(entry -> !keySet.contains(entry.getKey()))
            .map(Map.Entry::getValue)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    @Override
    public Set<String> lessThan(K key, boolean inclusive) {
        return index.headMap(key, inclusive)
            .values()
            .stream()
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    @Override
    public Set<String> greaterThan(K key, boolean inclusive) {
        return index.tailMap(key, inclusive)
            .values()
            .stream()
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    @Override
    public Set<String> isNull() {
        return Collections.unmodifiableSet(nullKeyValues);
    }

    @Override
    public Set<String> isNotNull() {
        return index.values()
            .stream()
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    @Override
    public Set<String> stringContains(String keyword) {
        ensureStringKeyType();
        return index.entrySet()
            .stream()
            .filter(entry -> StringUtils.containsIgnoreCase(entry.getKey().toString(), keyword))
            .map(Map.Entry::getValue)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    @Override
    public Set<String> stringNotContains(String keyword) {
        ensureStringKeyType();
        return index.entrySet()
            .stream()
            .filter(entry -> !StringUtils.containsIgnoreCase(entry.getKey().toString(), keyword))
            .map(Map.Entry::getValue)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    @Override
    public Set<String> stringStartsWith(String prefix) {
        ensureStringKeyType();
        var toKey = prefix + Character.MAX_VALUE;
        return index.subMap((K) prefix, true, (K) toKey, false)
            .values()
            .stream()
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
        return index.entrySet()
            .stream()
            .filter(entry -> StringUtils.endsWithIgnoreCase(entry.getKey().toString(), suffix))
            .map(Map.Entry::getValue)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    @Override
    public Set<String> stringNotEndsWith(String suffix) {
        ensureStringKeyType();
        return index.entrySet()
            .stream()
            .filter(entry -> !StringUtils.endsWithIgnoreCase(entry.getKey().toString(), suffix))
            .map(Map.Entry::getValue)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
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
    public Set<String> equal(K key) {
        return index.getOrDefault(key, Set.of());
    }

    @Override
    public Set<String> all() {
        return Stream.concat(index.values().stream(), Stream.of(nullKeyValues))
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }

    class UpsertTransactionalOperation implements TransactionalOperation {

        @NonNull
        private final String primaryKey;

        @Nullable
        private final Set<K> newKeys;

        private boolean committed;

        private Set<K> previousKeys;

        private boolean previousNullKey;

        UpsertTransactionalOperation(
            @NonNull String primaryKey, @Nullable Set<K> newKeys
        ) {
            this.primaryKey = primaryKey;
            this.newKeys = newKeys;
        }

        @Override
        public void prepare() {
            this.previousKeys = invertedIndex.get(primaryKey);
            this.previousNullKey = nullKeyValues.contains(primaryKey);
        }

        @Override
        public void commit() {
            if (committed) {
                return;
            }
            committed = true;
            if (Objects.equals(previousKeys, newKeys)) {
                return;
            }
            invertedIndex.put(primaryKey, newKeys);
            // remove previous keys
            if (!CollectionUtils.isEmpty(previousKeys)) {
                previousKeys.forEach(key -> index.computeIfPresent(key, (k, v) -> {
                    v.remove(primaryKey);
                    return v.isEmpty() ? null : v;
                }));
            }
            // add new keys
            if (!CollectionUtils.isEmpty(newKeys)) {
                for (K key : newKeys) {
                    index.compute(key, (k, v) -> {
                        if (v == null) {
                            v = ConcurrentHashMap.newKeySet();
                        }
                        if (spec.isUnique() && !v.isEmpty()) {
                            throw new DuplicateKeyException(
                                String.format("Duplicate key '%s' for extension '%s'", k,
                                    primaryKey)
                            );
                        }
                        v.add(primaryKey);
                        return v;
                    });
                }
                nullKeyValues.remove(primaryKey);
            } else {
                nullKeyValues.add(primaryKey);
            }
        }

        @Override
        public void rollback() {
            if (Objects.equals(this.previousKeys, newKeys) || !committed) {
                return;
            }
            // remove possibly added new keys
            if (!CollectionUtils.isEmpty(newKeys)) {
                newKeys.forEach(key -> index.computeIfPresent(key, (k, v) -> {
                    v.remove(primaryKey);
                    return v.isEmpty() ? null : v;
                }));
            }
            // add previous keys
            if (this.previousKeys == null) {
                // remove from inverted index
                invertedIndex.remove(primaryKey);
            } else {
                this.previousKeys.forEach(key -> index.compute(key, (k, v) -> {
                    if (v == null) {
                        v = ConcurrentHashMap.newKeySet();
                    }
                    // No need to check duplicate here, as it was already present before.
                    v.add(primaryKey);
                    return v;
                }));
                invertedIndex.put(primaryKey, this.previousKeys);
            }
            if (previousNullKey) {
                nullKeyValues.add(primaryKey);
            } else {
                nullKeyValues.remove(primaryKey);
            }
        }

    }

    class DeleteTransactionalOperation implements TransactionalOperation {

        @NonNull
        private final String primaryKey;

        private boolean committed;

        private Set<K> previousKeys;

        private boolean previousNullKey;

        DeleteTransactionalOperation(@NonNull String primaryKey) {
            this.primaryKey = primaryKey;
        }

        @Override
        public void prepare() {
            this.previousKeys = invertedIndex.get(primaryKey);
            this.previousNullKey = nullKeyValues.contains(primaryKey);
        }

        @Override
        public void commit() {
            if (committed) {
                return;
            }
            committed = true;
            invertedIndex.remove(primaryKey);
            if (this.previousKeys != null) {
                this.previousKeys.forEach(key -> index.computeIfPresent(key, (k, v) -> {
                    v.remove(primaryKey);
                    return v.isEmpty() ? null : v;
                }));
            }
            nullKeyValues.remove(primaryKey);
        }

        @Override
        public void rollback() {
            if (this.previousKeys == null || !committed) {
                return;
            }
            if (previousNullKey) {
                nullKeyValues.add(primaryKey);
            } else {
                nullKeyValues.remove(primaryKey);
            }
            // add previous keys
            this.previousKeys.forEach(key -> index.compute(key, (k, v) -> {
                if (v == null) {
                    v = ConcurrentHashMap.newKeySet();
                }
                v.add(primaryKey);
                return v;
            }));
            invertedIndex.put(primaryKey, this.previousKeys);
        }
    }

    private void ensureStringKeyType() {
        Assert.isTrue(
            getKeyType() == String.class || getKeyType() == UnknownKey.class,
            "Key type must be String for this operation"
        );
    }
}
