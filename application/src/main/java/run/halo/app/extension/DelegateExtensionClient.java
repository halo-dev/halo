package run.halo.app.extension;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import run.halo.app.extension.index.IndexedQueryEngine;

/**
 * DelegateExtensionClient fully delegates ReactiveExtensionClient.
 *
 * @author johnniang
 */
@Component
public class DelegateExtensionClient implements ExtensionClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private final ReactiveExtensionClient client;

    public DelegateExtensionClient(ReactiveExtensionClient client) {
        this.client = client;
    }

    @Override
    public <E extends Extension> List<E> list(Class<E> type, Predicate<E> predicate,
        Comparator<E> comparator) {
        return client.list(type, predicate, comparator).collectList().block(TIMEOUT);
    }

    @Override
    public <E extends Extension> ListResult<E> list(Class<E> type, Predicate<E> predicate,
        Comparator<E> comparator, int page, int size) {
        return client.list(type, predicate, comparator, page, size).block(TIMEOUT);
    }

    @Override
    public <E extends Extension> List<E> listAll(Class<E> type, ListOptions options, Sort sort) {
        return client.listAll(type, options, sort).collectList().block(TIMEOUT);
    }

    @Override
    public <E extends Extension> List<String> listAllNames(Class<E> type, ListOptions options,
        Sort sort) {
        return client.listAllNames(type, options, sort).collectList().block(TIMEOUT);
    }

    @Override
    public <E extends Extension> List<String> listTopNames(Class<E> type, ListOptions options,
        Sort sort, int topN) {
        return client.listTopNames(type, options, sort, topN).collectList().block(TIMEOUT);
    }

    @Override
    public <E extends Extension> ListResult<E> listBy(Class<E> type, ListOptions options,
        PageRequest page) {
        return client.listBy(type, options, page).block(TIMEOUT);
    }

    @Override
    public <E extends Extension> ListResult<String> listNamesBy(Class<E> type, ListOptions options,
        PageRequest page) {
        return client.listNamesBy(type, options, page).block(TIMEOUT);
    }

    @Override
    public <E extends Extension> long countBy(Class<E> type, ListOptions options) {
        return client.countBy(type, options).blockOptional(TIMEOUT).orElse(0L);
    }

    @Override
    public <E extends Extension> Optional<E> fetch(Class<E> type, String name) {
        return client.fetch(type, name).blockOptional(TIMEOUT);
    }

    @Override
    public Optional<Unstructured> fetch(GroupVersionKind gvk, String name) {
        return client.fetch(gvk, name).blockOptional(TIMEOUT);
    }

    @Override
    public <E extends Extension> void create(E extension) {
        client.create(extension).block(TIMEOUT);
    }

    @Override
    public <E extends Extension> void update(E extension) {
        client.update(extension).block(TIMEOUT);
    }

    @Override
    public <E extends Extension> void delete(E extension) {
        client.delete(extension).block(TIMEOUT);
    }

    @Override
    public IndexedQueryEngine indexedQueryEngine() {
        return client.indexedQueryEngine();
    }

    @Override
    public void watch(Watcher watcher) {
        client.watch(watcher);
    }
}
