package run.halo.app.extension.index;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
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
    public <E extends Extension> void insert(@NonNull Iterable<E> extensions) {
        extensions.forEach(extension -> {
            // get indices manager
            var indices = indicesManager.get((Class<E>) extension.getClass());
            indices.insert(extension);
        });
    }

    @Override
    public <E extends Extension> void update(@NonNull Iterable<E> extensions) {
        extensions.forEach(extension -> {
            var indices = indicesManager.get((Class<E>) extension.getClass());
            indices.update(extension);
        });
    }

    @Override
    public <E extends Extension> void delete(@NonNull Iterable<E> extensions) {
        extensions.forEach(extension -> {
            var indices = indicesManager.get((Class<E>) extension.getClass());
            indices.delete(extension);
        });
    }

    @Override
    public <E extends Extension> ListResult<String> retrieve(
        Class<E> type, ListOptions options, PageRequest page) {
        if (options == null) {
            options = ListOptions.builder().build();
        }
        var finalCondition = options.toCondition();
        var indices = indicesManager.get(type);
        var queryVisitor = new QueryVisitor<>(indices, conversionService);
        queryVisitor.enter(finalCondition);
        var result = queryVisitor.getResult();
        var total = result.size();
        // create comparator
        var sort = page.getSort();
        var comparator = buildComparator(sort, indices);

        int offset = (page.getPageNumber() - 1) * page.getPageSize();
        int limit = page.getPageSize();

        if (limit <= 0) {
            // return all results for backward compatibility
            var finalResult = result.stream().sorted(comparator).toList();
            return new ListResult<>(
                page.getPageNumber(), page.getPageSize(), total, finalResult
            );
        }
        if (offset > total) {
            return new ListResult<>(
                page.getPageNumber(), page.getPageSize(), total, new LinkedList<>()
            );
        }
        var n = offset + limit;
        if (n > 1000) {
            var finalResult = result.stream().sorted(comparator)
                .skip(offset)
                .limit(limit)
                .toList();
            return new ListResult<>(
                page.getPageNumber(), page.getPageSize(), total, finalResult
            );
        }
        var pq = new PriorityQueue<>(n, comparator.reversed());
        result.forEach(primaryKey -> {
            pq.offer(primaryKey);
            if (pq.size() > n) {
                pq.poll();
            }
        });
        var finalResult = new LinkedList<String>();
        while (!pq.isEmpty()) {
            finalResult.addFirst(pq.poll());
            if (finalResult.size() >= limit) {
                // no need to compare further
                break;
            }
        }
        pq.clear();
        return new ListResult<>(
            page.getPageNumber(), page.getPageSize(), total, finalResult
        );
    }


    @Override
    public <E extends Extension> Iterable<String> retrieveAll(
        Class<E> type, ListOptions options, Sort sort) {
        if (options == null) {
            options = ListOptions.builder().build();
        }
        if (sort == null) {
            sort = Sort.unsorted();
        }
        var finalCondition = options.toCondition();
        var indices = indicesManager.get(type);
        var queryVisitor = new QueryVisitor<>(indices, conversionService);
        queryVisitor.enter(finalCondition);
        var result = queryVisitor.getResult();
        if (sort.isUnsorted()) {
            // no need to sort the result
            return result.stream()::iterator;
        }
        // create comparator
        var comparator = buildComparator(sort, indices);
        return result.stream().sorted(comparator)::iterator;
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
        var indices = indicesManager.get(type);
        var queryVisitor = new QueryVisitor<>(indices, conversionService);
        queryVisitor.enter(finalCondition);
        var result = queryVisitor.getResult();
        // create comparator
        var comparator = buildComparator(sort, indices);
        // make sure using reversed comparator to get top N
        var pq = new PriorityQueue<>(topN + 1, comparator.reversed());
        result.forEach(primaryKey -> {
            pq.offer(primaryKey);
            if (pq.size() > topN) {
                pq.poll();
            }
        });
        var finalResult = new LinkedList<String>();
        while (!pq.isEmpty()) {
            finalResult.addFirst(pq.poll());
        }
        return finalResult;
    }

    @Override
    public <E extends Extension> long count(Class<E> type, ListOptions options) {
        if (options == null) {
            options = ListOptions.builder().build();
        }
        var finalCondition = options.toCondition();
        var indices = indicesManager.get(type);
        var queryVisitor = new QueryVisitor<>(indices, conversionService);
        queryVisitor.enter(finalCondition);
        return queryVisitor.getResult().size();
    }

    @Override
    @NonNull
    public IndicesManager getIndicesManager() {
        return this.indicesManager;
    }

    private <E extends Extension> Comparator<String> buildComparator(
        Sort sort, Indices<E> indices
    ) {
        return sort.stream()
            .map(order -> buildComparator(order, indices))
            .reduce(Comparator::thenComparing)
            .orElseGet(Comparator::naturalOrder);
    }

    private <K extends Comparable<K>, E extends Extension> Comparator<String> buildComparator(
        Sort.Order order, Indices<E> indices
    ) {
        var index = indices.<K>getIndex(order.getProperty());
        Comparator<String> comparator;
        if (index instanceof MultiValueIndex<E, K> multiValueIndex) {
            comparator = (left, right) -> {
                var leftKeys = multiValueIndex.getKeys(left);
                var rightKeys = multiValueIndex.getKeys(right);
                // null first by default
                if (CollectionUtils.isEmpty(leftKeys)) {
                    return CollectionUtils.isEmpty(rightKeys) ? 0 : -1;
                }
                if (CollectionUtils.isEmpty(rightKeys)) {
                    return 1;
                }
                // compare the first key
                K leftKey = leftKeys.iterator().next();
                K rightKey = rightKeys.iterator().next();
                return Comparator.<K>naturalOrder().compare(leftKey, rightKey);
            };
        } else if (index instanceof SingleValueIndex<E, K> singleValueIndex) {
            comparator = (left, right) -> {
                K leftKey = singleValueIndex.getKey(left);
                K rightKey = singleValueIndex.getKey(right);
                // null first by default
                if (leftKey == null) {
                    return rightKey == null ? 0 : -1;
                }
                if (rightKey == null) {
                    return 1;
                }
                return leftKey.compareTo(rightKey);
            };
        } else {
            throw new UnsupportedOperationException(
                "Unsupported index type for sorting: " + index.getClass()
            );
        }
        if (order.isDescending()) {
            comparator = comparator.reversed();
        }
        return comparator;
    }
}
