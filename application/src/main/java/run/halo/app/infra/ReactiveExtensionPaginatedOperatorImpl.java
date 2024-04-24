package run.halo.app.infra;

import static run.halo.app.extension.index.query.QueryFactory.isNull;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import run.halo.app.core.extension.notification.Subscription;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequest;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.ReactiveExtensionClient;

@Component
@RequiredArgsConstructor
public class ReactiveExtensionPaginatedOperatorImpl implements ReactiveExtensionPaginatedOperator {
    private static final int DEFAULT_PAGE_SIZE = 200;
    private final ReactiveExtensionClient client;

    @Override
    public <E extends Extension> Mono<Void> deleteContinuously(Class<E> type,
        ListOptions listOptions) {
        var pageRequest = createPageRequest();
        return cleanupContinuously(type, listOptions, pageRequest);
    }

    private <E extends Extension> Mono<Void> cleanupContinuously(Class<E> type,
        ListOptions listOptions,
        PageRequest pageRequest) {
        // forever loop first page until no more to delete
        return pageBy(type, listOptions, pageRequest)
            .flatMap(page -> Flux.fromIterable(page.getItems())
                .flatMap(client::delete)
                .then(page.hasNext() ? cleanupContinuously(type, listOptions, pageRequest)
                    : Mono.empty())
            );
    }

    @Override
    public <E extends Extension> Flux<E> deleteInitialBatch(Class<E> type,
        ListOptions listOptions) {
        var pageRequest = createPageRequest();
        var newFieldQuery = listOptions.getFieldSelector()
            .andQuery(isNull("metadata.deletionTimestamp"));
        listOptions.setFieldSelector(newFieldQuery);

        final AtomicLong totalRecords = new AtomicLong(0);
        final AtomicLong consumedRecords = new AtomicLong(0);

        return pageBy(type, listOptions, pageRequest)
            .doOnNext(page -> totalRecords.compareAndSet(0, page.getTotal()))
            // forever loop first page until no more to delete
            .expand(page -> hasMorePages(page, consumedRecords.get(), totalRecords.get())
                ? pageBy(type, listOptions, pageRequest) : Mono.empty())
            .flatMap(page -> Flux.fromIterable(page.getItems()))
            .takeWhile(item -> consumedRecords.incrementAndGet() <= totalRecords.get())
            .flatMap(this::deleteWithRetry);
    }

    @SuppressWarnings("unchecked")
    <E extends Extension> Mono<E> deleteWithRetry(E item) {
        return client.delete(item)
            .onErrorResume(OptimisticLockingFailureException.class,
                e -> attemptToDelete((Class<E>) item.getClass(), item.getMetadata().getName()));
    }

    private <E extends Extension> Mono<E> attemptToDelete(Class<E> type, String name) {
        return Mono.defer(() -> client.fetch(type, name)
                .flatMap(client::delete)
            )
            .retryWhen(Retry.backoff(8, Duration.ofMillis(100))
                .filter(OptimisticLockingFailureException.class::isInstance));
    }

    private <E extends Extension> boolean hasMorePages(ListResult<E> result, long consumedRecords,
        long totalRecords) {
        return result.hasNext() && consumedRecords < totalRecords;
    }

    @Override
    public <E extends Extension> Flux<E> list(Class<E> type, ListOptions listOptions) {
        var pageRequest = createPageRequest();
        return list(type, listOptions, pageRequest);
    }

    /**
     * Paginated list all items to avoid memory overflow.
     * <pre>
     * 1. Retrieve data multiple times until all data is consumed.
     * 2. Fetch next page if current page has more data and consumed records is less than total
     * records.
     * 3. Take while consumed records is less than total records.
     * 4. totalRecords from first page to ensure new inserted data will not be counted in during
     * querying to avoid infinite loop.
     * </pre>
     */
    private <E extends Extension> Flux<E> list(Class<E> type, ListOptions listOptions,
        PageRequest pageRequest) {
        final AtomicLong totalRecords = new AtomicLong(0);
        final AtomicLong consumedRecords = new AtomicLong(0);
        return pageBy(type, listOptions, pageRequest)
            // set total records in first page
            .doOnNext(page -> totalRecords.compareAndSet(0, page.getTotal()))
            .expandDeep(page -> {
                if (hasMorePages(page, consumedRecords.get(), totalRecords.get())) {
                    // fetch next page
                    PageRequest nextPageRequest = pageRequest.next();
                    return pageBy(type, listOptions, nextPageRequest);
                } else {
                    return Mono.empty();
                }
            })
            .flatMap(page -> Flux.fromIterable(page.getItems()))
            .takeWhile(item -> consumedRecords.incrementAndGet() <= totalRecords.get());
    }

    private PageRequest createPageRequest() {
        return PageRequestImpl.of(1, DEFAULT_PAGE_SIZE,
            Sort.by("metadata.creationTimestamp", "metadata.name"));
    }

    private <E extends Extension> Mono<ListResult<E>> pageBy(Class<E> type, ListOptions listOptions,
        PageRequest pageRequest) {
        return client.listBy(type, listOptions, pageRequest);
    }
}
