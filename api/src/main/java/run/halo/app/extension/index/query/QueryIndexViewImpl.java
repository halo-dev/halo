package run.halo.app.extension.index.query;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import run.halo.app.extension.index.KeyComparator;

/**
 * A default implementation for {@link run.halo.app.extension.index.query.QueryIndexView}.
 *
 * @author guqing
 * @since 2.12.0
 */
public class QueryIndexViewImpl implements QueryIndexView {
    private final Lock lock = new ReentrantLock();
    private final Set<String> fieldNames;
    private final Table<String, String, NavigableSet<String>> orderedMatches;

    /**
     * Creates a new {@link QueryIndexViewImpl} for the given {@link Map} of index entries.
     *
     * @param indexEntries index entries from indexer to create the view for.
     */
    public QueryIndexViewImpl(Map<String, Collection<Map.Entry<String, String>>> indexEntries) {
        this.fieldNames = new HashSet<>();
        this.orderedMatches = HashBasedTable.create();
        for (var entry : indexEntries.entrySet()) {
            String fieldName = entry.getKey();
            this.fieldNames.add(fieldName);
            for (var fieldEntry : entry.getValue()) {
                var id = fieldEntry.getValue();
                var fieldValue = fieldEntry.getKey();
                var columnValue = this.orderedMatches.get(id, fieldName);
                if (columnValue == null) {
                    columnValue = Sets.newTreeSet();
                    this.orderedMatches.put(id, fieldName, columnValue);
                }
                columnValue.add(fieldValue);
            }
        }
    }

    @Override
    public NavigableSet<String> getIdsForFieldValue(String fieldName, String fieldValue) {
        lock.lock();
        try {
            checkFieldNameIndexed(fieldName);
            var result = new TreeSet<String>();
            for (var cell : orderedMatches.cellSet()) {
                if (cell.getColumnKey().equals(fieldName) && cell.getValue().contains(fieldValue)) {
                    result.add(cell.getRowKey());
                }
            }
            return result;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public NavigableSet<String> getAllValuesForField(String fieldName) {
        lock.lock();
        try {
            checkFieldNameIndexed(fieldName);
            var result = Sets.<String>newTreeSet();
            for (var cell : orderedMatches.cellSet()) {
                if (cell.getColumnKey().equals(fieldName)) {
                    result.addAll(cell.getValue());
                }
            }
            return result;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public NavigableSet<String> getAllIdsForField(String fieldName) {
        lock.lock();
        try {
            checkFieldNameIndexed(fieldName);
            NavigableSet<String> ids = new TreeSet<>();
            // iterate over the table and collect all IDs associated with the given field name
            for (var cell : orderedMatches.cellSet()) {
                if (cell.getColumnKey().equals(fieldName)) {
                    ids.add(cell.getRowKey());
                }
            }
            return ids;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public NavigableSet<String> getAllIds() {
        lock.lock();
        try {
            return new TreeSet<>(orderedMatches.rowKeySet());
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

            NavigableSet<String> result = new TreeSet<>();

            // obtain all values for both fields and their corresponding IDs
            var field1ValuesToIds = getColumnValuesToIdsMap(fieldName1);
            var field2ValuesToIds = getColumnValuesToIdsMap(fieldName2);

            // iterate over each value of the first field
            for (Map.Entry<String, NavigableSet<String>> entry : field1ValuesToIds.entrySet()) {
                String fieldValue = entry.getKey();
                NavigableSet<String> idsForFieldValue = entry.getValue();

                // if the second field contains the same value, add all matching IDs
                if (field2ValuesToIds.containsKey(fieldValue)) {
                    NavigableSet<String> matchingIds = field2ValuesToIds.get(fieldValue);
                    for (String id : idsForFieldValue) {
                        if (matchingIds.contains(id)) {
                            result.add(id);
                        }
                    }
                }
            }
            return result;
        } finally {
            lock.unlock();
        }
    }

    private Map<String, NavigableSet<String>> getColumnValuesToIdsMap(String fieldName) {
        var valuesToIdsMap = new HashMap<String, NavigableSet<String>>();
        for (var cell : orderedMatches.cellSet()) {
            if (cell.getColumnKey().equals(fieldName)) {
                var celValues = cell.getValue();
                if (CollectionUtils.isEmpty(celValues)) {
                    continue;
                }
                if (celValues.size() != 1) {
                    throw new IllegalArgumentException(
                        "Unsupported multi cell values to join with other field for: " + fieldName
                            + " with values: " + celValues);
                }
                String fieldValue = cell.getValue().first();
                if (!valuesToIdsMap.containsKey(fieldValue)) {
                    valuesToIdsMap.put(fieldValue, new TreeSet<>());
                }
                valuesToIdsMap.get(fieldValue).add(cell.getRowKey());
            }
        }
        return valuesToIdsMap;
    }

    @Override
    public NavigableSet<String> findIdsForFieldValueGreaterThan(String fieldName1,
        String fieldName2, boolean orEqual) {
        lock.lock();
        try {
            checkFieldNameIndexed(fieldName1);
            checkFieldNameIndexed(fieldName2);

            NavigableSet<String> result = new TreeSet<>();

            // obtain all values for both fields and their corresponding IDs
            var field1ValuesToIds = getColumnValuesToIdsMap(fieldName1);
            var field2ValuesToIds = getColumnValuesToIdsMap(fieldName2);

            // iterate over each value of the first field
            for (var entryField1 : field1ValuesToIds.entrySet()) {
                String fieldValue1 = entryField1.getKey();

                // iterate over each value of the second field
                for (var entryField2 : field2ValuesToIds.entrySet()) {
                    String fieldValue2 = entryField2.getKey();

                    int comparison = fieldValue1.compareTo(fieldValue2);
                    if (orEqual ? comparison >= 0 : comparison > 0) {
                        // if the second field contains the same value, add all matching IDs
                        for (String id : entryField1.getValue()) {
                            if (field2ValuesToIds.get(fieldValue2).contains(id)) {
                                result.add(id);
                            }
                        }
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

            NavigableSet<String> result = new TreeSet<>();

            // obtain all values for both fields and their corresponding IDs
            var field1ValuesToIds = getColumnValuesToIdsMap(fieldName1);
            var field2ValuesToIds = getColumnValuesToIdsMap(fieldName2);

            // iterate over each value of the first field
            for (var entryField1 : field1ValuesToIds.entrySet()) {
                String fieldValue1 = entryField1.getKey();

                // iterate over each value of the second field
                for (var entryField2 : field2ValuesToIds.entrySet()) {
                    String fieldValue2 = entryField2.getKey();

                    int comparison = fieldValue1.compareTo(fieldValue2);
                    if (orEqual ? comparison <= 0 : comparison < 0) {
                        // if the second field contains the same value, add all matching IDs
                        for (String id : entryField1.getValue()) {
                            if (field2ValuesToIds.get(fieldValue2).contains(id)) {
                                result.add(id);
                            }
                        }
                    }
                }
            }
            return result;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void removeByIdNotIn(NavigableSet<String> ids) {
        lock.lock();
        try {
            Set<String> idsToRemove = new HashSet<>();
            // check each row key if it is not in the given ids set
            for (String rowKey : orderedMatches.rowKeySet()) {
                if (!ids.contains(rowKey)) {
                    idsToRemove.add(rowKey);
                }
            }

            // remove all rows that are not in the given ids set
            for (String idToRemove : idsToRemove) {
                orderedMatches.row(idToRemove).clear();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<String> sortBy(Sort sort) {
        lock.lock();
        try {
            for (Sort.Order order : sort) {
                String fieldName = order.getProperty();
                checkFieldNameIndexed(fieldName);
            }

            // obtain all row keys (IDs)
            Set<String> allRowKeys = orderedMatches.rowKeySet();

            // convert row keys to list for sorting
            List<String> sortedRowKeys = new ArrayList<>(allRowKeys);
            if (sort.isUnsorted()) {
                return sortedRowKeys;
            }

            // sort row keys according to sort criteria in a Sort object
            sortedRowKeys.sort((id1, id2) -> {
                for (Sort.Order order : sort) {
                    String fieldName = order.getProperty();

                    // compare the values of the two rows on the field
                    int comparison = compareRowValue(id1, id2, fieldName, order.isAscending());
                    if (comparison != 0) {
                        return comparison;
                    }
                }
                // if all sort criteria are equal, return 0
                return 0;
            });

            return sortedRowKeys;
        } finally {
            lock.unlock();
        }
    }

    private int compareRowValue(String id1, String id2, String fieldName, boolean isAscending) {
        var value1 = getSingleFieldValueForSort(id1, fieldName);
        var value2 = getSingleFieldValueForSort(id2, fieldName);
        // nulls are less than everything whatever the sort order is
        // do not simply the following code for null check,it's different from KeyComparator
        if (value1 == null && value2 == null) {
            return 0;
        } else if (value1 == null) {
            return 1;
        } else if (value2 == null) {
            return -1;
        }
        return isAscending ? KeyComparator.INSTANCE.compare(value1, value2)
            : KeyComparator.INSTANCE.compare(value2, value1);
    }

    @Nullable
    String getSingleFieldValueForSort(String rowKey, String fieldName) {
        return Optional.ofNullable(orderedMatches.get(rowKey, fieldName))
            .filter(values -> !CollectionUtils.isEmpty(values))
            .map(values -> {
                if (values.size() != 1) {
                    throw new IllegalArgumentException(
                        "Unsupported multi field values to sort for: " + fieldName
                            + " with values: " + values);
                }
                return values.first();
            })
            .orElse(null);
    }

    private void checkFieldNameIndexed(String fieldName) {
        if (!fieldNames.contains(fieldName)) {
            throw new IllegalArgumentException("Field name " + fieldName
                + " is not indexed, please ensure it added to the index spec before querying");
        }
    }
}
