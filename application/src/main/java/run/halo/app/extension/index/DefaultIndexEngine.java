package run.halo.app.extension.index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
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
        // Re-create the stream for every iteration, so the iterable can be subscribed more than once.
        return () -> orderedPrimaryKeys(result, sortOrders).iterator();
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
            var index = indices.getIndex(order.getProperty());
            if (index instanceof MultiValueIndex<?, ?> multiValueIndex) {
                sortOrders.add(new SortOrder(multiValueIndex::getSortKey, order.isDescending(), multiValueIndex));
            } else if (index instanceof SingleValueIndex<?, ?> singleValueIndex) {
                sortOrders.add(new SortOrder(singleValueIndex::getKey, order.isDescending(), singleValueIndex));
            } else {
                throw new UnsupportedOperationException("Unsupported index type for sorting: " + index.getClass());
            }
        }
        return sortOrders;
    }

    /** Builds a comparator for the sort orders, ending with the primary key so that ordering is stable. */
    private static Comparator<String> buildComparator(List<SortOrder> sortOrders) {
        Comparator<String> comparator = null;
        for (var sortOrder : sortOrders) {
            var accessor = sortOrder.accessor();
            Comparator<String> orderComparator = (left, right) -> {
                var leftKey = accessor.apply(left);
                var rightKey = accessor.apply(right);
                return sortOrder.descending() ? compareSortKeys(rightKey, leftKey) : compareSortKeys(leftKey, rightKey);
            };
            comparator = comparator == null ? orderComparator : comparator.thenComparing(orderComparator);
        }
        if (comparator == null) {
            return Comparator.naturalOrder();
        }
        return comparator.thenComparing(Comparator.naturalOrder());
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

    /**
     * Orders the primary keys through the index of the first sort order, and applies the remaining sort orders only
     * within the groups that share the same first key.
     */
    private static Stream<String> orderedPrimaryKeys(Collection<String> primaryKeys, List<SortOrder> sortOrders) {
        var candidates = primaryKeys instanceof Set<String> set ? set : new HashSet<>(primaryKeys);
        var primaryOrder = sortOrders.getFirst();
        var tieBreaker = buildComparator(sortOrders.subList(1, sortOrders.size()));
        return primaryOrder.index().sortedStream(candidates, primaryOrder.descending(), tieBreaker);
    }

    private static List<String> sortedPrimaryKeys(Collection<String> primaryKeys, List<SortOrder> sortOrders) {
        if (sortOrders.isEmpty()) {
            var sortedResult = new ArrayList<>(primaryKeys);
            sortedResult.sort(Comparator.naturalOrder());
            return sortedResult;
        }
        return orderedPrimaryKeys(primaryKeys, sortOrders).toList();
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
        limit = Math.min(limit, total - offset);
        List<String> finalResult;
        if (sortOrders.isEmpty()) {
            var sortedResult = new ArrayList<>(result);
            sortedResult.sort(Comparator.naturalOrder());
            finalResult = new ArrayList<>(sortedResult.subList(offset, offset + limit));
        } else {
            finalResult = orderedPrimaryKeys(result, sortOrders)
                    .skip(offset)
                    .limit(limit)
                    .toList();
        }
        return new ListResult<>(page.getPageNumber(), page.getPageSize(), total, finalResult);
    }

    private static List<String> topN(Collection<String> result, List<SortOrder> sortOrders, int topN) {
        if (sortOrders.isEmpty()) {
            var sortedResult = new ArrayList<>(result);
            sortedResult.sort(Comparator.naturalOrder());
            return sortedResult.subList(0, Math.min(topN, sortedResult.size()));
        }
        return orderedPrimaryKeys(result, sortOrders).limit(topN).toList();
    }

    private record SortOrder(Function<String, Object> accessor, boolean descending, SortableIndex index) {}
}
