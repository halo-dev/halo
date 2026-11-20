package run.halo.app.localization;

import java.util.List;
import java.util.Objects;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequest;

/**
 * Read-only decorator for a domain-owned database adapter. Public callers replace their read dependency with this
 * client; authorization remains in the source adapter and precedes all display processors. Console and write paths
 * continue to use their original repository.
 *
 * <pre>{@code
 * var products = presentationService.client(productSource);
 * return products.fetch(productId);
 * }</pre>
 *
 * @param <T> detached display DTO
 * @since 2.28.0
 */
public final class ReactivePresentationResourceClient<T> {
    private final LocalizableResourceSource<T> source;
    private final ResourcePresentationService presentation;

    public ReactivePresentationResourceClient(
            LocalizableResourceSource<T> source, ResourcePresentationService presentation) {
        this.source = Objects.requireNonNull(source);
        this.presentation = Objects.requireNonNull(presentation);
    }

    /** Returns an authorized presentation snapshot; absence retains the source adapter's empty-result semantics. */
    public Mono<T> fetch(String id) {
        return Mono.defer(() -> source.fetch(id))
                .filter(source.descriptor()::isEligible)
                .flatMap(value -> presentation.presentAll(List.of(value), source.descriptor()))
                .map(List::getFirst);
    }

    /** Preserves source page metadata and item order; the source adapter must filter eligibility before pagination. */
    public Mono<ListResult<T>> list(PageRequest page) {
        return Mono.defer(() -> source.list(page))
                .flatMap(result -> presentation
                        .presentAll(result.getItems(), source.descriptor())
                        .map(items -> new ListResult<>(result.getPage(), result.getSize(), result.getTotal(), items)));
    }
}
