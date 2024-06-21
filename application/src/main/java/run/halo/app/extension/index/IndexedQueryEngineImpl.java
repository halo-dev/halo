package run.halo.app.extension.index;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
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
import run.halo.app.extension.index.query.QueryIndexView;
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

    @Override
    public ListResult<String> retrieve(GroupVersionKind type, ListOptions options,
        PageRequest page) {
        var allMatchedResult = doRetrieve(type, options, page.getSort());
        var list = ListResult.subList(allMatchedResult, page.getPageNumber(), page.getPageSize());
        return new ListResult<>(page.getPageNumber(), page.getPageSize(),
            allMatchedResult.size(), list);
    }

    @Override
    public List<String> retrieveAll(GroupVersionKind type, ListOptions options, Sort sort) {
        return doRetrieve(type, options, sort);
    }

    NavigableSet<String> retrieveForLabelMatchers(Indexer indexer,
        List<SelectorMatcher> labelMatchers) {
        var objectLabelMap = ObjectLabelMap.buildFrom(indexer, labelMatchers);
        // O(k×m) time complexity, k is the number of keys, m is the number of labelMatchers
        return objectLabelMap.objectIdLabelsMap()
            .entrySet()
            .stream()
            .filter(entry -> {
                var labels = entry.getValue();
                // object match all labels will be returned
                return labelMatchers.stream()
                    .allMatch(matcher -> matcher.test(labels.get(matcher.getKey())));
            })
            .map(Map.Entry::getKey)
            .collect(Collectors.toCollection(TreeSet::new));
    }

    NavigableSet<String> evaluateSelectorsForIndex(Indexer indexer, QueryIndexView indexView,
        ListOptions options) {
        final var hasLabelSelector = hasLabelSelector(options.getLabelSelector());
        final var hasFieldSelector = hasFieldSelector(options.getFieldSelector());

        if (!hasLabelSelector && !hasFieldSelector) {
            return QueryFactory.all().matches(indexView);
        }

        // only label selector
        if (hasLabelSelector && !hasFieldSelector) {
            return retrieveForLabelMatchers(indexer, options.getLabelSelector().getMatchers());
        }

        // only field selector
        if (!hasLabelSelector) {
            var fieldSelector = options.getFieldSelector();
            return fieldSelector.query().matches(indexView);
        }

        // both label and field selector
        var fieldSelector = options.getFieldSelector();
        var forField = fieldSelector.query().matches(indexView);
        var forLabel =
            retrieveForLabelMatchers(indexer, options.getLabelSelector().getMatchers());

        // determine the optimal retainAll direction based on the size of the collection
        var resultSet = (forField.size() <= forLabel.size()) ? forField : forLabel;
        resultSet.retainAll((resultSet == forField) ? forLabel : forField);
        return resultSet;
    }

    List<String> doRetrieve(GroupVersionKind type, ListOptions options, Sort sort) {
        var indexer = indexerFactory.getIndexer(type);

        StopWatch stopWatch = new StopWatch(type.toString());

        stopWatch.start("Check index status to ensure all indexes are ready");
        var fieldNamesUsedInQuery = getFieldNamesUsedInListOptions(options, sort);
        checkIndexForNames(indexer, fieldNamesUsedInQuery);
        stopWatch.stop();

        var indexView = new QueryIndexViewImpl(indexer);

        stopWatch.start("Evaluate selectors for index");
        var resultSet = evaluateSelectorsForIndex(indexer, indexView, options);
        stopWatch.stop();

        stopWatch.start("Sort result set by sort order");
        var result = indexView.sortBy(resultSet, sort);
        stopWatch.stop();

        if (log.isTraceEnabled()) {
            log.trace("Retrieve result from indexer by query [{}],\n {}", options,
                stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
        }
        return result;
    }

    void checkIndexForNames(Indexer indexer, Set<String> indexNames) {
        indexer.acquireReadLock();
        try {
            for (String indexName : indexNames) {
                // get index entry will throw exception if index not found
                indexer.getIndexEntry(indexName);
            }
        } finally {
            indexer.releaseReadLock();
        }
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

    record ObjectLabelMap(Map<String, Map<String, String>> objectIdLabelsMap) {

        public static ObjectLabelMap buildFrom(Indexer indexer,
            List<SelectorMatcher> labelMatchers) {
            indexer.acquireReadLock();
            try {
                final var objectNameLabelsMap = new HashMap<String, Map<String, String>>();
                final var labelIndexEntry = indexer.getIndexEntry(LabelIndexSpecUtils.LABEL_PATH);
                // O(m) time complexity, m is the number of labelMatchers
                final var labelKeysToQuery = labelMatchers.stream()
                    .sorted(Comparator.comparing(SelectorMatcher::getKey))
                    .map(SelectorMatcher::getKey)
                    .collect(Collectors.toSet());

                labelIndexEntry.entries().forEach(entry -> {
                    // key is labelKey=labelValue, value is objectName
                    var labelPair = LabelIndexSpecUtils.labelKeyValuePair(entry.getKey());
                    if (!labelKeysToQuery.contains(labelPair.getFirst())) {
                        return;
                    }
                    objectNameLabelsMap.computeIfAbsent(entry.getValue(), k -> new HashMap<>())
                        .put(labelPair.getFirst(), labelPair.getSecond());
                });

                var nameIndexOperator = new IndexEntryOperatorImpl(
                    indexer.getIndexEntry(PrimaryKeySpecUtils.PRIMARY_INDEX_NAME)
                );
                var allIndexedObjectNames = nameIndexOperator.getValues();

                // remove all object names that exist labels,O(n) time complexity
                allIndexedObjectNames.removeAll(objectNameLabelsMap.keySet());
                // add absent object names to object labels map
                for (String name : allIndexedObjectNames) {
                    objectNameLabelsMap.put(name, new HashMap<>());
                }
                return new ObjectLabelMap(objectNameLabelsMap);
            } finally {
                indexer.releaseReadLock();
            }
        }
    }
}
