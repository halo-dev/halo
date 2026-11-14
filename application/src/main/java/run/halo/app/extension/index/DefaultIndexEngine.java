package run.halo.app.extension.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.Function;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequest;
import run.halo.app.extension.index.query.QueryVisitor;

/**
 * Default implementation of {@link IndexEngine}.
 *
 * @author johnniang
 * @since 2.22.0
 */
@Component
class DefaultIndexEngine implements IndexEngine, DisposableBean {

    private IndicesManager indicesManager;

    private final ConversionService conversionService;

    public DefaultIndexEngine(ConversionService conversionService) {
        this.conversionService = conversionService;
        this.indicesManager = new DefaultIndicesManager();
    }

    /**
     * Set the indices manager. Only for testing purpose.
     *
     * @param indicesManager the indices manager
     */
    void setIndicesManager(IndicesManager indicesManager) {
        Assert.notNull(indicesManager, "indicesManager must not be null");
        this.indicesManager = indicesManager;
    }

    @Override
    public void destroy() throws Exception {
        this.indicesManager.close();
    }

    @Override
    public <E extends Extension> void insert(Iterable<E> extensions) {
        extensions.forEach(extension -> {
            // get indices manager
            var indices = indicesManager.get((Class<E>) extension.getClass());
            indices.insert(extension);
        });
    }

    @Override
    public <E extends Extension> void update(Iterable<E> extensions) {
        extensions.forEach(extension -> {
            var indices = indicesManager.get((Class<E>) extension.getClass());
            indices.update(extension);
        });
    }

    @Override
    public <E extends Extension> void delete(Iterable<E> extensions) {
        extensions.forEach(extension -> {
            var indices = indicesManager.get((Class<E>) extension.getClass());
            indices.delete(extension);
        });
    }

    @Override
    public <E extends Extension> void deleteByName(Class<E> type, String primaryKey) {
        indicesManager.get(type).deleteByName(primaryKey);
    }

    @Override
    public <E extends Extension> ListResult<String> retrieve(Class<E> type, ListOptions options, PageRequest page) {
        if (options == null) {
            options = ListOptions.builder().build();
        }
        var finalCondition = options.toCondition();
        indicesManager.awaitReady(type);
        var indices = indicesManager.get(type);
        var queryVisitor = new QueryVisitor<>(indices, conversionService);
        queryVisitor.enter(finalCondition);
        var result = queryVisitor.getResult();
        var sort = page.getSort();
        var sortOrders = sort.isUnsorted() ? List.<SortOrder>of() : buildSortOrders(sort, indices);
        return paginate(result, page, sortOrders);
    }

    @Override
    public <E extends Extension> Iterable<String> retrieveAll(Class<E> type, ListOptions options, Sort sort) {
        if (options == null) {
            options = ListOptions.builder().build();
        }
        if (sort == null) {
            sort = Sort.unsorted();
        }
        var finalCondition = options.toCondition();
        indicesManager.awaitReady(type);
        var indices = indicesManager.get(type);
        var queryVisitor = new QueryVisitor<>(indices, conversionService);
        queryVisitor.enter(finalCondition);
        var result = queryVisitor.getResult();
        if (sort.isUnsorted()) {
            // no need to sort the result
            return result.stream()::iterator;
        }
        var sortOrders = buildSortOrders(sort, indices);
        return sortedPrimaryKeys(result, sortOrders)::iterator;
    }

    @Override
    public <E extends Extension> Iterable<String> retrieveTopN(
            Class<E> type, ListOptions options, Sort sort, int topN) {
        Assert.isTrue(topN > 0, "topN must be greater than 0");
        if (options == null) {
            options = ListOptions.builder().build();
        }
        if (sort == null) {
            sort = Sort.unsorted();
        }
        var finalCondition = options.toCondition();
        indicesManager.awaitReady(type);
        var indices = indicesManager.get(type);
        var queryVisitor = new QueryVisitor<>(indices, conversionService);
        queryVisitor.enter(finalCondition);
        var result = queryVisitor.getResult();
        var sortOrders = sort.isUnsorted() ? List.<SortOrder>of() : buildSortOrders(sort, indices);
        return topN(result, sortOrders, topN);
    }

    @Override
    public <E extends Extension> long count(Class<E> type, ListOptions options) {
        if (options == null) {
            options = ListOptions.builder().build();
        }
        var finalCondition = options.toCondition();
        indicesManager.awaitReady(type);
        var indices = indicesManager.get(type);
        var queryVisitor = new QueryVisitor<>(indices, conversionService);
        queryVisitor.enter(finalCondition);
        return queryVisitor.getResult().size();
    }

    @Override
    public IndicesManager getIndicesManager() {
        return this.indicesManager;
    }

    private <E extends Extension> List<SortOrder> buildSortOrders(Sort sort, Indices<E> indices) {
        var sortOrders = new ArrayList<SortOrder>(sort.toList().size());
        for (var order : sort) {
            Index<E, ?> index = indices.getIndex(order.getProperty());
            Function<String, Object> accessor;
            if (index instanceof MultiValueIndex<?, ?> multiValueIndex) {
                accessor = primaryKey -> {
                    var keys = multiValueIndex.getKeys(primaryKey);
                    return CollectionUtils.isEmpty(keys)
                            ? null
                            : keys.iterator().next();
                };
            } else if (index instanceof SingleValueIndex<?, ?> singleValueIndex) {
                accessor = singleValueIndex::getKey;
            } else {
                throw new UnsupportedOperationException("Unsupported index type for sorting: " + index.getClass());
            }
            sortOrders.add(new SortOrder(accessor, order.isDescending()));
        }
        return sortOrders;
    }

    private static Comparator<Object[]> buildComparator(List<SortOrder> sortOrders) {
        Comparator<Object[]> comparator = null;
        for (var i = 0; i < sortOrders.size(); i++) {
            var index = i;
            var sortOrder = sortOrders.get(i);
            Comparator<Object[]> orderComparator = (left, right) -> {
                // index 0 is the primary key, sort keys start at index 1
                var leftKey = left[index + 1];
                var rightKey = right[index + 1];
                return sortOrder.descending() ? compareSortKeys(rightKey, leftKey) : compareSortKeys(leftKey, rightKey);
            };
            comparator = comparator == null ? orderComparator : comparator.thenComparing(orderComparator);
        }
        if (comparator == null) {
            return Comparator.comparing(DefaultIndexEngine::primaryKey);
        }
        // Use the primary key as a tie-breaker so that paging over equal sort keys is stable.
        return comparator.thenComparing(Comparator.comparing(DefaultIndexEngine::primaryKey));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static int compareSortKeys(Object left, Object right) {
        // null first by default
        if (left == null) {
            return right == null ? 0 : -1;
        }
        if (right == null) {
            return 1;
        }
        return ((Comparable) left).compareTo(right);
    }

    private static List<Object[]> createSortEntries(Collection<String> primaryKeys, List<SortOrder> sortOrders) {
        var entries = new ArrayList<Object[]>(primaryKeys.size());
        for (var primaryKey : primaryKeys) {
            var sortEntry = newSortEntry(sortOrders);
            fillSortEntry(sortEntry, primaryKey, sortOrders);
            entries.add(sortEntry);
        }
        return entries;
    }

    private static Object[] newSortEntry(List<SortOrder> sortOrders) {
        return new Object[sortOrders.size() + 1];
    }

    private static void fillSortEntry(Object[] sortEntry, String primaryKey, List<SortOrder> sortOrders) {
        sortEntry[0] = primaryKey;
        for (var i = 0; i < sortOrders.size(); i++) {
            sortEntry[i + 1] = sortOrders.get(i).accessor().apply(primaryKey);
        }
    }

    private static String primaryKey(Object[] sortEntry) {
        return (String) sortEntry[0];
    }

    private static List<String> sortedPrimaryKeys(Collection<String> primaryKeys, List<SortOrder> sortOrders) {
        if (sortOrders.isEmpty()) {
            var sortedResult = new ArrayList<>(primaryKeys);
            sortedResult.sort(Comparator.naturalOrder());
            return sortedResult;
        }
        var entries = createSortEntries(primaryKeys, sortOrders);
        entries.sort(buildComparator(sortOrders));
        var sortedResult = new ArrayList<String>(entries.size());
        for (var entry : entries) {
            sortedResult.add(primaryKey(entry));
        }
        return sortedResult;
    }

    private ListResult<String> paginate(Collection<String> result, PageRequest page, List<SortOrder> sortOrders) {
        var total = result.size();
        int offset = (page.getPageNumber() - 1) * page.getPageSize();
        int limit = page.getPageSize();
        if (limit <= 0) {
            // return all results for backward compatibility
            var finalResult = sortedPrimaryKeys(result, sortOrders);
            return new ListResult<>(page.getPageNumber(), page.getPageSize(), total, finalResult);
        }
        if (offset >= total) {
            return new ListResult<>(page.getPageNumber(), page.getPageSize(), total, new LinkedList<>());
        }
        if (offset + limit > total) {
            limit = total - offset;
        }
        var n = offset + limit;
        if (n > 1000) {
            var sortedResult = sortedPrimaryKeys(result, sortOrders);
            var finalResult = new ArrayList<String>(limit);
            for (var i = offset; i < offset + limit; i++) {
                finalResult.add(sortedResult.get(i));
            }
            return new ListResult<>(page.getPageNumber(), page.getPageSize(), total, finalResult);
        }
        var pq = topNHeap(result, sortOrders, n);
        var finalResult = new LinkedList<String>();
        while (!pq.isEmpty()) {
            finalResult.addFirst(primaryKey(pq.poll()));
            if (finalResult.size() >= limit) {
                // no need to compare further
                break;
            }
        }
        pq.clear();
        return new ListResult<>(page.getPageNumber(), page.getPageSize(), total, finalResult);
    }

    private static List<String> topN(Collection<String> result, List<SortOrder> sortOrders, int topN) {
        var pq = topNHeap(result, sortOrders, topN);
        var finalResult = new LinkedList<String>();
        while (!pq.isEmpty()) {
            finalResult.addFirst(primaryKey(pq.poll()));
        }
        return finalResult;
    }

    private static PriorityQueue<Object[]> topNHeap(Collection<String> result, List<SortOrder> sortOrders, int topN) {
        var comparator = buildComparator(sortOrders);
        var pq = new PriorityQueue<Object[]>(topN + 1, comparator.reversed());
        // Reuse one buffer for rejected candidates, so only candidates entering the heap allocate.
        var candidate = newSortEntry(sortOrders);
        for (var primaryKey : result) {
            fillSortEntry(candidate, primaryKey, sortOrders);
            if (pq.size() < topN) {
                pq.offer(candidate);
                candidate = newSortEntry(sortOrders);
            } else if (comparator.compare(candidate, pq.peek()) < 0) {
                pq.poll();
                pq.offer(candidate);
                candidate = newSortEntry(sortOrders);
            }
        }
        return pq;
    }

    private record SortOrder(Function<String, Object> accessor, boolean descending) {}
}
