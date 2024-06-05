package run.halo.app.extension.index;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.util.Assert;

public class IndexEntryOperatorImpl implements IndexEntryOperator {
    private final IndexEntry indexEntry;

    public IndexEntryOperatorImpl(IndexEntry indexEntry) {
        this.indexEntry = indexEntry;
    }

    private static NavigableSet<String> createNavigableSet() {
        return new TreeSet<>(KeyComparator.INSTANCE);
    }

    @Override
    public NavigableSet<String> lessThan(String key, boolean orEqual) {
        Assert.notNull(key, "Key must not be null.");
        indexEntry.acquireReadLock();
        try {
            var navigableIndexedKeys = indexEntry.indexedKeys();
            var headSetKeys = navigableIndexedKeys.headSet(key, orEqual);
            return findIn(headSetKeys);
        } finally {
            indexEntry.releaseReadLock();
        }
    }

    @Override
    public NavigableSet<String> greaterThan(String key, boolean orEqual) {
        Assert.notNull(key, "Key must not be null.");
        indexEntry.acquireReadLock();
        try {
            var navigableIndexedKeys = indexEntry.indexedKeys();
            var tailSetKeys = navigableIndexedKeys.tailSet(key, orEqual);
            return findIn(tailSetKeys);
        } finally {
            indexEntry.releaseReadLock();
        }
    }

    @Override
    public NavigableSet<String> range(String start, String end, boolean startInclusive,
        boolean endInclusive) {
        Assert.notNull(start, "The start must not be null.");
        Assert.notNull(end, "The end must not be null.");
        indexEntry.acquireReadLock();
        try {
            var navigableIndexedKeys = indexEntry.indexedKeys();
            var tailSetKeys = navigableIndexedKeys.subSet(start, startInclusive, end, endInclusive);
            return findIn(tailSetKeys);
        } finally {
            indexEntry.releaseReadLock();
        }
    }

    @Override
    public NavigableSet<String> find(String key) {
        Assert.notNull(key, "The key must not be null.");
        indexEntry.acquireReadLock();
        try {
            var resultSet = createNavigableSet();
            var result = indexEntry.getObjectNamesBy(key);
            if (result != null) {
                resultSet.addAll(result);
            }
            return resultSet;
        } finally {
            indexEntry.releaseReadLock();
        }
    }

    @Override
    public NavigableSet<String> findIn(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return createNavigableSet();
        }
        indexEntry.acquireReadLock();
        try {
            var keysToSearch = new HashSet<>(keys);
            var resultSet = createNavigableSet();
            for (var entry : indexEntry.entries()) {
                if (keysToSearch.contains(entry.getKey())) {
                    resultSet.add(entry.getValue());
                }
            }
            return resultSet;
        } finally {
            indexEntry.releaseReadLock();
        }
    }

    @Override
    public Set<String> getValues() {
        indexEntry.acquireReadLock();
        try {
            Set<String> uniqueValues = new HashSet<>();
            for (Map.Entry<String, String> entry : indexEntry.entries()) {
                uniqueValues.add(entry.getValue());
            }
            return uniqueValues;
        } finally {
            indexEntry.releaseReadLock();
        }
    }
}
