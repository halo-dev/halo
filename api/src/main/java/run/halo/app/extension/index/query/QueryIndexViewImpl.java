package run.halo.app.extension.index.query;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A default implementation for {@link QueryIndexView}.
 *
 * @author guqing
 * @since 2.12.0
 */
public class QueryIndexViewImpl implements QueryIndexView {
    private final Lock lock = new ReentrantLock();
    private final Map<String, SetMultimap<String, String>> orderedMatches;

    /**
     * Creates a new {@link QueryIndexViewImpl} for the given {@link Map} of index entries.
     *
     * @param indexEntries index entries from indexer to create the view for.
     */
    public QueryIndexViewImpl(Map<String, Collection<Map.Entry<String, String>>> indexEntries) {
        this.orderedMatches = new HashMap<>();
        for (var entry : indexEntries.entrySet()) {
            // do not use stream collect here as it is slower
            this.orderedMatches.put(entry.getKey(), createSetMultiMap(entry.getValue()));
        }
    }

    @Override
    public NavigableSet<String> getIdsForFieldValue(String fieldName, String fieldValue) {
        lock.lock();
        try {
            checkFieldNameIndexed(fieldName);
            SetMultimap<String, String> fieldMap = orderedMatches.get(fieldName);
            return fieldMap != null ? new TreeSet<>(fieldMap.get(fieldValue)) : emptySet();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public NavigableSet<String> getAllValuesForField(String fieldName) {
        lock.lock();
        try {
            checkFieldNameIndexed(fieldName);
            SetMultimap<String, String> fieldMap = orderedMatches.get(fieldName);
            return fieldMap != null ? new TreeSet<>(fieldMap.keySet()) : emptySet();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public NavigableSet<String> getAllIdsForField(String fieldName) {
        lock.lock();
        try {
            checkFieldNameIndexed(fieldName);
            SetMultimap<String, String> fieldMap = orderedMatches.get(fieldName);
            return fieldMap != null ? new TreeSet<>(fieldMap.values()) : emptySet();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public NavigableSet<String> findIdsForFieldValueEqual(String fieldName1, String fieldName2) {
        lock.lock();
        try {
            checkFieldNameIndexed(fieldName1);
            checkFieldNameIndexed(fieldName2);
            var index1 = orderedMatches.get(fieldName1);
            var index2 = orderedMatches.get(fieldName2);

            var idFieldValuesForIndex2Map =
                Multimaps.invertFrom(index2, MultimapBuilder.treeKeys().treeSetValues().build());
            var result = Sets.<String>newTreeSet();
            for (Map.Entry<String, String> entryForIndex1 : index1.entries()) {
                var fieldValues = idFieldValuesForIndex2Map.get(entryForIndex1.getValue());
                for (String item : fieldValues) {
                    if (entryForIndex1.getKey().equals(item)) {
                        result.add(entryForIndex1.getValue());
                    }
                }
            }
            return result;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public NavigableSet<String> findIdsForFieldValueGreaterThan(String fieldName1,
        String fieldName2, boolean orEqual) {
        lock.lock();
        try {
            checkFieldNameIndexed(fieldName1);
            checkFieldNameIndexed(fieldName2);

            var index1 = orderedMatches.get(fieldName1);
            var index2 = orderedMatches.get(fieldName2);

            var idFieldValuesForIndex2Map =
                Multimaps.invertFrom(index2, MultimapBuilder.treeKeys().treeSetValues().build());

            var result = Sets.<String>newTreeSet();
            for (Map.Entry<String, String> entryForIndex1 : index1.entries()) {
                var fieldValues = idFieldValuesForIndex2Map.get(entryForIndex1.getValue());
                for (String item : fieldValues) {
                    int compare = entryForIndex1.getKey().compareTo(item);
                    if (orEqual ? compare >= 0 : compare > 0) {
                        result.add(entryForIndex1.getValue());
                    }
                }
            }
            return result;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public NavigableSet<String> findIdsForFieldValueLessThan(String fieldName1, String fieldName2,
        boolean orEqual) {
        lock.lock();
        try {
            checkFieldNameIndexed(fieldName1);
            checkFieldNameIndexed(fieldName2);
            SetMultimap<String, String> index1 = orderedMatches.get(fieldName1);
            SetMultimap<String, String> index2 = orderedMatches.get(fieldName2);

            var idFieldValuesForIndex2Map =
                Multimaps.invertFrom(index2, MultimapBuilder.treeKeys().treeSetValues().build());

            var result = Sets.<String>newTreeSet();
            for (Map.Entry<String, String> entryForIndex1 : index1.entries()) {
                var fieldValues = idFieldValuesForIndex2Map.get(entryForIndex1.getValue());
                for (String item : fieldValues) {
                    int compare = entryForIndex1.getKey().compareTo(item);
                    if (orEqual ? compare <= 0 : compare < 0) {
                        result.add(entryForIndex1.getValue());
                    }
                }
            }
            return result;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void removeAllFieldValuesByIdNotIn(NavigableSet<String> ids) {
        lock.lock();
        try {
            for (var fieldNameValuesEntry : orderedMatches.entrySet()) {
                SetMultimap<String, String> indicates = fieldNameValuesEntry.getValue();
                indicates.entries().removeIf(entry -> !ids.contains(entry.getValue()));
            }
        } finally {
            lock.unlock();
        }
    }

    private void checkFieldNameIndexed(String fieldName) {
        if (!orderedMatches.containsKey(fieldName)) {
            throw new IllegalArgumentException("Field name " + fieldName
                + " is not indexed, please ensure it added to the index spec before querying");
        }
    }

    private TreeSet<String> emptySet() {
        return new TreeSet<>();
    }

    private SetMultimap<String, String> createSetMultiMap(
        Collection<Map.Entry<String, String>> entries) {

        SetMultimap<String, String> multiMap = MultimapBuilder.hashKeys()
            .hashSetValues()
            .build();
        for (Map.Entry<String, String> entry : entries) {
            multiMap.put(entry.getKey(), entry.getValue());
        }
        return multiMap;
    }
}
