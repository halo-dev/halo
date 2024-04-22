package run.halo.app.infra;

import java.util.concurrent.atomic.AtomicLong;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequest;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.ReactiveExtensionClient;

/**
 * <p>Note that: This class can not be used for <code>deletion</code> operation, because deletion
 * operation will change the total records.</p>
 */
public class ReactiveExtensionPaginatedIterator<E extends Extension> {
    private static final int DEFAULT_PAGE_SIZE = 200;
    private final PageRequest pageRequest;
    private final Class<E> type;
    private final ListOptions listOptions;
    private final ReactiveExtensionClient client;

    public ReactiveExtensionPaginatedIterator(ReactiveExtensionClient client, Class<E> type,
        ListOptions listOptions) {
        this(client, type, listOptions, DEFAULT_PAGE_SIZE);
    }

    /**
     * Page request with default sort by creationTimestamp and name asc to ensure data consistency.
     */
    public ReactiveExtensionPaginatedIterator(ReactiveExtensionClient client, Class<E> type,
        ListOptions listOptions, int pageSize) {
        this.client = client;
        this.type = type;
        this.listOptions = listOptions;
        pageRequest = PageRequestImpl.of(1, pageSize,
            Sort.by("metadata.creationTimestamp", "metadata.name"));
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
    public Flux<E> list() {
        final AtomicLong totalRecords = new AtomicLong(0);
        final AtomicLong consumedRecords = new AtomicLong(0);
        return Flux.defer(() -> pageBy(pageRequest))
            .doOnNext(page -> {
                // set total records in first page
                if (page.isFirst()) {
                    totalRecords.set(page.getTotal());
                }
            })
            .expandDeep(page -> {
                if (page.hasNext() && consumedRecords.get() < totalRecords.get()) {
                    // fetch next page
                    PageRequest nextPageRequest = pageRequest.next();
                    return pageBy(nextPageRequest);
                } else {
                    return Mono.empty();
                }
            })
            .flatMap(page -> Flux.fromIterable(page.getItems()))
            .takeWhile(item -> {
                long currentCount = consumedRecords.incrementAndGet();
                return currentCount <= totalRecords.get();
            });
    }

    private Mono<ListResult<E>> pageBy(PageRequest pageRequest) {
        return client.listBy(type, listOptions, pageRequest);
    }
}
