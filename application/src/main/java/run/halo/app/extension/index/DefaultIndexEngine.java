package run.halo.app.extension.index;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.function.Function;
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
import run.halo.app.extension.index.query.Condition;
import run.halo.app.extension.index.query.QueryVisitor;
import run.halo.app.extension.router.selector.SelectorMatcher;

@Component
class DefaultIndexEngine implements IndexEngine, DisposableBean {

    private final IndicesManager indicesManager;

    private final ConversionService conversionService;

    public DefaultIndexEngine(ConversionService conversionService) {
        this.conversionService = conversionService;
        this.indicesManager = new DefaultIndicesManager();
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
    public <E extends Extension> ListResult<String> retrieve(
        Class<E> type, ListOptions options, PageRequest page) {
        if (options == null) {
            options = ListOptions.builder().build();
        }
        var finalCondition = buildCondition(options);
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
        var finalCondition = buildCondition(options);
        var indices = indicesManager.get(type);
        var queryVisitor = new QueryVisitor<>(indices, conversionService);
        queryVisitor.enter(finalCondition);
        var result = queryVisitor.getResult();
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
        var finalCondition = buildCondition(options);
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
        var finalCondition = buildCondition(options);
        var indices = indicesManager.get(type);
        var queryVisitor = new QueryVisitor<>(indices, conversionService);
        queryVisitor.enter(finalCondition);
        return queryVisitor.getResult().size();
    }

    @Override
    public IndicesManager getIndicesManager() {
        return this.indicesManager;
    }

    private Condition buildCondition(@NonNull ListOptions options) {
        var condition = Condition.empty();
        var fieldSelector = options.getFieldSelector();
        if (fieldSelector != null) {
            var query = fieldSelector.query();
            if (!(query instanceof Condition fieldCondition)) {
                throw new IllegalArgumentException("Only support condition query");
            }
            condition = condition.and(fieldCondition);
        }
        var labelSelector = options.getLabelSelector();
        if (labelSelector != null) {
            var labelCondition = labelSelector.getMatchers().stream()
                .map(SelectorMatcher::toCondition)
                .map(Function.<Condition>identity())
                .reduce(Condition::and)
                .orElse(Condition.empty());
            condition = condition.and(labelCondition);
        }
        return condition;
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
        var comparator = (Comparator<String>) (left, right) -> {
            var leftKeys = index.getKeys(left);
            var rightKeys = index.getKeys(right);
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
        if (order.isDescending()) {
            comparator = comparator.reversed();
        }
        return comparator;
    }
}
