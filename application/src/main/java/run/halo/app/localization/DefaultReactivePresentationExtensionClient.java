package run.halo.app.localization;

import java.util.Comparator;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.Extension;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequest;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.ReactivePresentationExtensionClient;
import run.halo.app.extension.Unstructured;

/** Source reads and queries complete before the shared presentation pipeline runs. */
@Component
@RequiredArgsConstructor
public class DefaultReactivePresentationExtensionClient implements ReactivePresentationExtensionClient {
    private static final int BATCH_SIZE = 100;

    private final ReactiveExtensionClient delegate;
    private final ResourcePresentationService presentation;

    @Override
    public <E extends Extension> Mono<E> fetch(Class<E> type, String name) {
        return present(delegate.fetch(type, name));
    }

    @Override
    public Mono<Unstructured> fetch(GroupVersionKind gvk, String name) {
        return present(delegate.fetch(gvk, name));
    }

    @Override
    public <E extends Extension> Mono<E> get(Class<E> type, String name) {
        return present(delegate.get(type, name));
    }

    @Override
    public <E extends Extension> Flux<E> list(Class<E> type, Predicate<E> predicate, Comparator<E> comparator) {
        return present(delegate.list(type, predicate, comparator));
    }

    /** @deprecated use {@link #listAll(Class, ListOptions, Sort)} instead */
    @Override
    @Deprecated(since = "2.22.0")
    public <E extends Extension> Mono<ListResult<E>> list(
            Class<E> type, Predicate<E> predicate, Comparator<E> comparator, int page, int size) {
        return delegate.list(type, predicate, comparator, page, size)
                .flatMap(result -> present(Flux.fromIterable(result.getItems()))
                        .collectList()
                        .map(items -> new ListResult<>(result.getPage(), result.getSize(), result.getTotal(), items)));
    }

    @Override
    public <E extends Extension> Flux<E> listAll(Class<E> type, ListOptions options, Sort sort) {
        return present(delegate.listAll(type, options, sort));
    }

    @Override
    public <E extends Extension> Mono<ListResult<E>> listBy(Class<E> type, ListOptions options, PageRequest page) {
        return delegate.listBy(type, options, page)
                .flatMap(result -> present(Flux.fromIterable(result.getItems()))
                        .collectList()
                        .map(items -> new ListResult<>(result.getPage(), result.getSize(), result.getTotal(), items)));
    }

    @Override
    public <E extends Extension> Mono<ListResult<String>> listNamesBy(
            Class<E> type, ListOptions options, PageRequest page) {
        return delegate.listNamesBy(type, options, page);
    }

    @Override
    public <E extends Extension> Mono<Long> countBy(Class<E> type, ListOptions options) {
        return delegate.countBy(type, options);
    }

    @Override
    public <E extends Extension> Flux<String> listAllNames(Class<E> type, ListOptions options, Sort sort) {
        return delegate.listAllNames(type, options, sort);
    }

    @Override
    public <E extends Extension> Flux<String> listTopNames(Class<E> type, ListOptions options, Sort sort, int topN) {
        return delegate.listTopNames(type, options, sort, topN);
    }

    private <E extends Extension> Mono<E> present(Mono<E> source) {
        return present(source.flux()).singleOrEmpty();
    }

    private <E extends Extension> Flux<E> present(Flux<E> sources) {
        return Flux.deferContextual(view -> {
            var context = view.<LocalizationContext>getOrEmpty(LocalizationContext.class);
            if (context.isEmpty()) {
                return sources;
            }
            var scope = context.get().scope();
            if (scope != LocalizationScope.PUBLIC_PAGE && scope != LocalizationScope.PUBLIC_API) {
                return sources;
            }
            return sources.buffer(BATCH_SIZE)
                    .concatMap(presentation::presentAll)
                    .flatMapIterable(items -> items);
        });
    }
}
