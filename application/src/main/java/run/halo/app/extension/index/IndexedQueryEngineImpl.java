package run.halo.app.extension.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequest;
import run.halo.app.extension.index.query.Query;
import run.halo.app.extension.index.query.QueryIndexViewImpl;
import run.halo.app.extension.router.selector.FieldSelector;
import run.halo.app.extension.router.selector.LabelSelector;
import run.halo.app.extension.router.selector.SelectorMatcher;

/**
 * A default implementation of {@link IndexedQueryEngine}.
 *
 * @author guqing
 * @since 2.12.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IndexedQueryEngineImpl implements IndexedQueryEngine {

    private final IndexerFactory indexerFactory;

    private static Map<String, IndexEntry> fieldPathIndexEntryMap(Indexer indexer) {
        // O(n) time complexity
        Map<String, IndexEntry> indexEntryMap = new HashMap<>();
        var iterator = indexer.readyIndexesIterator();
        while (iterator.hasNext()) {
            var indexEntry = iterator.next();
            var descriptor = indexEntry.getIndexDescriptor();
            var indexedFieldPath = descriptor.getSpec().getName();
            indexEntryMap.put(indexedFieldPath, indexEntry);
        }
        return indexEntryMap;
    }

    static IndexEntry getIndexEntry(String fieldPath, Map<String, IndexEntry> fieldPathEntryMap) {
        if (!fieldPathEntryMap.containsKey(fieldPath)) {
            throwNotIndexedException(fieldPath);
        }
        return fieldPathEntryMap.get(fieldPath);
    }

    static <T> List<T> paginate(List<T> list, int page, int size) {
        if (list == null) {
            return new ArrayList<>();
        }

        if (size == 0) {
            return new ArrayList<>(list);
        }

        int fromIndex = (page - 1) * size;
        if (fromIndex >= list.size() || fromIndex < 0) {
            return new ArrayList<>();
        }

        int toIndex = Math.min(fromIndex + size, list.size());
        return new ArrayList<>(list.subList(fromIndex, toIndex));
    }

    @Override
    public ListResult<String> retrieve(GroupVersionKind type, ListOptions options,
        PageRequest page) {
        var indexer = indexerFactory.getIndexer(type);
        var allMatchedResult = doRetrieve(indexer, options, page.getSort());
        var list = paginate(allMatchedResult, page.getPageNumber(), page.getPageSize());
        return new ListResult<>(page.getPageNumber(), page.getPageSize(),
            allMatchedResult.size(), list);
    }

    @Override
    public List<String> retrieveAll(GroupVersionKind type, ListOptions options) {
        var indexer = indexerFactory.getIndexer(type);
        return doRetrieve(indexer, options, Sort.unsorted());
    }

    static <T> List<T> intersection(List<T> list1, List<T> list2) {
        Set<T> set = new LinkedHashSet<>(list1);
        List<T> intersection = new ArrayList<>();
        for (T item : list2) {
            if (set.contains(item) && !intersection.contains(item)) {
                intersection.add(item);
            }
        }
        return intersection;
    }

    static void throwNotIndexedException(String fieldPath) {
        throw new IllegalArgumentException(
            "No index found for fieldPath: " + fieldPath
                + ", make sure you have created an index for this field.");
    }

    List<String> retrieveForLabelMatchers(List<SelectorMatcher> labelMatchers,
        Map<String, IndexEntry> fieldPathEntryMap, List<String> allMetadataNames) {
        var indexEntry = getIndexEntry(LabelIndexSpecUtils.LABEL_PATH, fieldPathEntryMap);
        // O(m) time complexity, m is the number of labelMatchers
        var labelKeysToQuery = labelMatchers.stream()
            .sorted(Comparator.comparing(SelectorMatcher::getKey))
            .map(SelectorMatcher::getKey)
            .collect(Collectors.toSet());

        Map<String, Map<String, String>> objectNameLabelsMap = new HashMap<>();
        indexEntry.acquireReadLock();
        try {
            indexEntry.entries().forEach(entry -> {
                // key is labelKey=labelValue, value is objectName
                var labelPair = LabelIndexSpecUtils.labelKeyValuePair(entry.getKey());
                if (!labelKeysToQuery.contains(labelPair.getFirst())) {
                    return;
                }
                objectNameLabelsMap.computeIfAbsent(entry.getValue(), k -> new HashMap<>())
                    .put(labelPair.getFirst(), labelPair.getSecond());
            });
        } finally {
            indexEntry.releaseReadLock();
        }
        // O(p * m) time complexity, p is the number of allMetadataNames
        return allMetadataNames.stream()
            .filter(objectName -> {
                var labels = objectNameLabelsMap.getOrDefault(objectName, Map.of());
                // object match all labels will be returned
                return labelMatchers.stream()
                    .allMatch(matcher -> matcher.test(labels.get(matcher.getKey())));
            })
            .toList();
    }

    List<String> doRetrieve(Indexer indexer, ListOptions options, Sort sort) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("build index entry map");
        var fieldPathEntryMap = fieldPathIndexEntryMap(indexer);
        stopWatch.stop();
        var primaryEntry = getIndexEntry(PrimaryKeySpecUtils.PRIMARY_INDEX_NAME, fieldPathEntryMap);
        // O(n) time complexity
        stopWatch.start("retrieve all metadata names");
        var allMetadataNames = new ArrayList<String>();
        primaryEntry.acquireReadLock();
        try {
            allMetadataNames.addAll(primaryEntry.indexedKeys());
        } finally {
            primaryEntry.releaseReadLock();
        }
        stopWatch.stop();

        stopWatch.start("retrieve matched metadata names");
        var hasLabelSelector = hasLabelSelector(options.getLabelSelector());
        final List<String> matchedByLabels = hasLabelSelector
            ? retrieveForLabelMatchers(options.getLabelSelector().getMatchers(), fieldPathEntryMap,
            allMetadataNames)
            : allMetadataNames;
        stopWatch.stop();

        stopWatch.start("retrieve matched metadata names by fields");
        final var hasFieldSelector = hasFieldSelector(options.getFieldSelector());
        var matchedByFields = hasFieldSelector
            ? retrieveForFieldSelector(options.getFieldSelector().query(), fieldPathEntryMap)
            : allMetadataNames;
        stopWatch.stop();

        stopWatch.start("merge result");
        List<String> foundObjectKeys;
        if (!hasLabelSelector && !hasFieldSelector) {
            foundObjectKeys = allMetadataNames;
        } else if (!hasLabelSelector) {
            foundObjectKeys = matchedByFields;
        } else {
            foundObjectKeys = intersection(matchedByFields, matchedByLabels);
        }
        stopWatch.stop();

        stopWatch.start("sort result");
        ResultSorter resultSorter = new ResultSorter(fieldPathEntryMap, foundObjectKeys);
        var result = resultSorter.sortBy(sort);
        stopWatch.stop();
        if (log.isTraceEnabled()) {
            log.trace("Retrieve result from indexer, {}", stopWatch.prettyPrint());
        }
        return result;
    }

    boolean hasLabelSelector(LabelSelector labelSelector) {
        return labelSelector != null && !CollectionUtils.isEmpty(labelSelector.getMatchers());
    }

    boolean hasFieldSelector(FieldSelector fieldSelector) {
        return fieldSelector != null && fieldSelector.query() != null;
    }

    List<String> retrieveForFieldSelector(Query query, Map<String, IndexEntry> fieldPathEntryMap) {
        Map<String, Collection<Map.Entry<String, String>>> indexView = new HashMap<>();
        for (Map.Entry<String, IndexEntry> entry : fieldPathEntryMap.entrySet()) {
            indexView.put(entry.getKey(), entry.getValue().immutableEntries());
        }
        // TODO optimize build indexView time
        var queryIndexView = new QueryIndexViewImpl(indexView);
        var resultSet = query.matches(queryIndexView);
        return new ArrayList<>(resultSet);
    }

    /**
     * Sort the given list by the given {@link Sort}.
     */
    static class ResultSorter {
        private final Map<String, IndexEntry> fieldPathEntryMap;
        private final List<String> list;

        public ResultSorter(Map<String, IndexEntry> fieldPathEntryMap, List<String> list) {
            this.fieldPathEntryMap = fieldPathEntryMap;
            this.list = list;
        }

        public List<String> sortBy(@NonNull Sort sort) {
            if (sort.isUnsorted()) {
                return list;
            }
            var sortedLists = new ArrayList<List<String>>();
            for (Sort.Order order : sort) {
                var indexEntry = fieldPathEntryMap.get(order.getProperty());
                if (indexEntry == null) {
                    throwNotIndexedException(order.getProperty());
                }
                var set = new HashSet<>(list);
                var objectNames = new ArrayList<String>();
                indexEntry.acquireReadLock();
                try {
                    for (var entry : indexEntry.entries()) {
                        var objectName = entry.getValue();
                        if (set.contains(objectName)) {
                            objectNames.add(objectName);
                        }
                    }
                } finally {
                    indexEntry.releaseReadLock();
                }
                var indexOrder = indexEntry.getIndexDescriptor().getSpec().getOrder();
                var asc = IndexSpec.OrderType.ASC.equals(indexOrder);
                if (asc != order.isAscending()) {
                    Collections.reverse(objectNames);
                }
                sortedLists.add(objectNames);
            }
            return mergeSortedLists(sortedLists);
        }

        /**
         * <p>Merge the given sorted lists into one sorted list.</p>
         * <p>The time complexity is O(n * log(m)), n is the number of all elements in the
         * sortedLists, m is the number of sortedLists.</p>
         */
        private List<String> mergeSortedLists(List<List<String>> sortedLists) {
            List<String> result = new ArrayList<>();
            // Use a priority queue to store the current element of each list and its index in
            // the list
            PriorityQueue<Pair> minHeap = new PriorityQueue<>(
                Comparator.comparing(pair -> pair.value));

            // Initialize the priority queue and add the first element of each list to the queue
            for (int i = 0; i < sortedLists.size(); i++) {
                if (!sortedLists.get(i).isEmpty()) {
                    minHeap.add(new Pair(i, 0, sortedLists.get(i).get(0)));
                }
            }

            while (!minHeap.isEmpty()) {
                Pair current = minHeap.poll();
                result.add(current.value());

                // Add the next element of this list to the priority queue
                if (current.indexInList() + 1 < sortedLists.get(current.listIndex()).size()) {
                    var list = sortedLists.get(current.listIndex());
                    minHeap.add(new Pair(current.listIndex(),
                        current.indexInList() + 1,
                        list.get(current.indexInList() + 1))
                    );
                }
            }
            return result;
        }

        /**
         * <p>A pair of element and its position in the original list.</p>
         * <pre>
         * listIndex: column index.
         * indexInList: element index in the list.
         * value: element value.
         * </pre>
         */
        private record Pair(int listIndex, int indexInList, String value) {
        }
    }
}
