package run.halo.app.extension.index.query;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiPredicate;
import org.springframework.data.domain.Sort;
import run.halo.app.extension.index.IndexEntry;
import run.halo.app.extension.index.IndexEntryOperator;
import run.halo.app.extension.index.IndexEntryOperatorImpl;
import run.halo.app.extension.index.Indexer;
import run.halo.app.extension.index.KeyComparator;

/**
 * A default implementation for {@link run.halo.app.extension.index.query.QueryIndexView}.
 *
 * @author guqing
 * @since 2.17.0
 */
public class QueryIndexViewImpl implements QueryIndexView {

    public static final String PRIMARY_INDEX_NAME = "metadata.name";

    private final Indexer indexer;

    /**
     * Construct a new {@link QueryIndexViewImpl} with the given {@link Indexer}.
     *
     * @throws IllegalArgumentException if the primary index does not exist
     */
    public QueryIndexViewImpl(Indexer indexer) {
        // check if primary index exists
        indexer.getIndexEntry(PRIMARY_INDEX_NAME);
        this.indexer = indexer;
    }

    @Override
    public NavigableSet<String> findIds(String fieldName, String fieldValue) {
        var operator = getEntryOperator(fieldName);
        return operator.find(fieldValue);
    }

    @Override
    public NavigableSet<String> getIdsForField(String fieldName) {
        var operator = getEntryOperator(fieldName);
        return new TreeSet<>(operator.getValues());
    }

    @Override
    public NavigableSet<String> getAllIds() {
        return new TreeSet<>(allIds());
    }

    @Override
    public NavigableSet<String> findMatchingIdsWithEqualValues(String fieldName1,
        String fieldName2) {
        indexer.acquireReadLock();
        try {
            return findIdsWithKeyComparator(fieldName1, fieldName2, (k1, k2) -> {
                var compare = KeyComparator.INSTANCE.compare(k1, k2);
                return compare == 0;
            });
        } finally {
            indexer.releaseReadLock();
        }
    }

    @Override
    public NavigableSet<String> findMatchingIdsWithGreaterValues(String fieldName1,
        String fieldName2, boolean orEqual) {
        indexer.acquireReadLock();
        try {
            return findIdsWithKeyComparator(fieldName1, fieldName2, (k1, k2) -> {
                var compare = KeyComparator.INSTANCE.compare(k1, k2);
                return orEqual ? compare <= 0 : compare < 0;
            });
        } finally {
            indexer.releaseReadLock();
        }
    }

    @Override
    public NavigableSet<String> findIdsGreaterThan(String fieldName, String fieldValue,
        boolean orEqual) {
        var operator = getEntryOperator(fieldName);
        return operator.greaterThan(fieldValue, orEqual);
    }

    @Override
    public NavigableSet<String> findMatchingIdsWithSmallerValues(String fieldName1,
        String fieldName2, boolean orEqual) {
        indexer.acquireReadLock();
        try {
            return findIdsWithKeyComparator(fieldName1, fieldName2, (k1, k2) -> {
                var compare = KeyComparator.INSTANCE.compare(k1, k2);
                return orEqual ? compare >= 0 : compare > 0;
            });
        } finally {
            indexer.releaseReadLock();
        }
    }

    @Override
    public NavigableSet<String> findIdsLessThan(String fieldName, String fieldValue,
        boolean orEqual) {
        var operator = getEntryOperator(fieldName);
        return operator.lessThan(fieldValue, orEqual);
    }

    @Override
    public NavigableSet<String> between(String fieldName, String lowerValue, boolean lowerInclusive,
        String upperValue, boolean upperInclusive) {
        var operator = getEntryOperator(fieldName);
        return operator.range(lowerValue, upperValue, lowerInclusive, upperInclusive);
    }

    @Override
    public List<String> sortBy(NavigableSet<String> ids, Sort sort) {
        if (sort.isUnsorted()) {
            return new ArrayList<>(ids);
        }
        indexer.acquireReadLock();
        try {
            var combinedComparator = sort.stream()
                .map(this::comparatorFrom)
                .reduce(Comparator::thenComparing)
                .orElseThrow();
            return ids.stream()
                .sorted(combinedComparator)
                .toList();
        } finally {
            indexer.releaseReadLock();
        }
    }

    Comparator<String> comparatorFrom(Sort.Order order) {
        var indexEntry = getIndexEntry(order.getProperty());
        var idPositionMap = indexEntry.getIdPositionMap();
        var isDesc = order.isDescending();
        // This sort algorithm works leveraging on that the idPositionMap is a map of id -> position
        // if the id is not in the map, it means that it is not indexed, and it will be placed at
        // the end
        return (a, b) -> {
            var indexOfA = idPositionMap.get(a);
            var indexOfB = idPositionMap.get(b);

            var isAIndexed = indexOfA != null;
            var isBIndexed = indexOfB != null;

            if (!isAIndexed && !isBIndexed) {
                return 0;
            }
            // un-indexed item are always at the end
            if (!isAIndexed) {
                return isDesc ? -1 : 1;
            }
            if (!isBIndexed) {
                return isDesc ? 1 : -1;
            }
            return isDesc ? Integer.compare(indexOfB, indexOfA)
                : Integer.compare(indexOfA, indexOfB);
        };
    }

    @Override
    public IndexEntry getIndexEntry(String fieldName) {
        return indexer.getIndexEntry(fieldName);
    }

    @Override
    public void acquireReadLock() {
        indexer.acquireReadLock();
    }

    @Override
    public void releaseReadLock() {
        indexer.releaseReadLock();
    }

    private IndexEntryOperator getEntryOperator(String fieldName) {
        var indexEntry = getIndexEntry(fieldName);
        return createIndexEntryOperator(indexEntry);
    }

    private IndexEntryOperator createIndexEntryOperator(IndexEntry entry) {
        return new IndexEntryOperatorImpl(entry);
    }

    private Set<String> allIds() {
        var indexEntry = getIndexEntry(PRIMARY_INDEX_NAME);
        return createIndexEntryOperator(indexEntry).getValues();
    }

    /**
     * Must lock the indexer before calling this method.
     */
    private NavigableSet<String> findIdsWithKeyComparator(String fieldName1, String fieldName2,
        BiPredicate<String, String> keyComparator) {
        // get entries from indexer for fieldName1
        var entriesA = getIndexEntry(fieldName1).entries();

        Map<String, List<String>> keyMap = new HashMap<>();
        for (Map.Entry<String, String> entry : entriesA) {
            keyMap.computeIfAbsent(entry.getValue(), v -> new ArrayList<>()).add(entry.getKey());
        }

        NavigableSet<String> result = new TreeSet<>();

        // get entries from indexer for fieldName2
        var entriesB = getIndexEntry(fieldName2).entries();
        for (Map.Entry<String, String> entry : entriesB) {
            List<String> matchedKeys = keyMap.get(entry.getValue());
            if (matchedKeys != null) {
                for (String key : matchedKeys) {
                    if (keyComparator.test(entry.getKey(), key)) {
                        result.add(entry.getValue());
                        // found one match, no need to continue
                        break;
                    }
                }
            }
        }
        return result;
    }
}
