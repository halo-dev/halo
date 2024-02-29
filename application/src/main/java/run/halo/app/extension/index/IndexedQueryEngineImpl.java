package run.halo.app.extension.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequest;
import run.halo.app.extension.index.query.QueryFactory;
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

    @Override
    public ListResult<String> retrieve(GroupVersionKind type, ListOptions options,
        PageRequest page) {
        var indexer = indexerFactory.getIndexer(type);
        var allMatchedResult = doRetrieve(indexer, options, page.getSort());
        var list = ListResult.subList(allMatchedResult, page.getPageNumber(), page.getPageSize());
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
        var primaryEntry = getIndexEntry(PrimaryKeySpecUtils.PRIMARY_INDEX_NAME, fieldPathEntryMap);
        stopWatch.stop();

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

        stopWatch.start("build index view");
        var fieldNamesUsedInQuery = getFieldNamesUsedInListOptions(options, sort);
        var indexViewMap = new HashMap<String, Collection<Map.Entry<String, String>>>();
        for (Map.Entry<String, IndexEntry> entry : fieldPathEntryMap.entrySet()) {
            if (!fieldNamesUsedInQuery.contains(entry.getKey())) {
                continue;
            }
            indexViewMap.put(entry.getKey(), entry.getValue().immutableEntries());
        }
        // TODO optimize build indexView time
        var indexView = new QueryIndexViewImpl(indexViewMap);
        stopWatch.stop();

        stopWatch.start("retrieve matched metadata names");
        if (hasLabelSelector(options.getLabelSelector())) {
            var matchedByLabels = retrieveForLabelMatchers(options.getLabelSelector().getMatchers(),
                fieldPathEntryMap, allMetadataNames);
            if (allMetadataNames.size() != matchedByLabels.size()) {
                indexView.removeByIdNotIn(new TreeSet<>(matchedByLabels));
            }
        }
        stopWatch.stop();

        stopWatch.start("retrieve matched metadata names by fields");
        final var hasFieldSelector = hasFieldSelector(options.getFieldSelector());
        if (hasFieldSelector) {
            var fieldSelector = options.getFieldSelector();
            var query = fieldSelector.query();
            var resultSet = query.matches(indexView);
            indexView.removeByIdNotIn(resultSet);
        }
        stopWatch.stop();

        stopWatch.start("sort result");
        var result = indexView.sortBy(sort);
        stopWatch.stop();

        if (log.isTraceEnabled()) {
            log.trace("Retrieve result from indexer, {}", stopWatch.prettyPrint());
        }
        return result;
    }

    @NonNull
    private Set<String> getFieldNamesUsedInListOptions(ListOptions options, Sort sort) {
        var fieldNamesUsedInQuery = new HashSet<String>();
        fieldNamesUsedInQuery.add(PrimaryKeySpecUtils.PRIMARY_INDEX_NAME);
        for (Sort.Order order : sort) {
            fieldNamesUsedInQuery.add(order.getProperty());
        }
        var hasFieldSelector = hasFieldSelector(options.getFieldSelector());
        if (hasFieldSelector) {
            var fieldQuery = options.getFieldSelector().query();
            var fieldNames = QueryFactory.getFieldNamesUsedInQuery(fieldQuery);
            fieldNamesUsedInQuery.addAll(fieldNames);
        }
        return fieldNamesUsedInQuery;
    }

    boolean hasLabelSelector(LabelSelector labelSelector) {
        return labelSelector != null && !CollectionUtils.isEmpty(labelSelector.getMatchers());
    }

    boolean hasFieldSelector(FieldSelector fieldSelector) {
        return fieldSelector != null;
    }
}
