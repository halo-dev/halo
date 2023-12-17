package run.halo.app.extension;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import org.springframework.stereotype.Component;

/**
 * DelegateExtensionClient fully delegates ReactiveExtensionClient.
 *
 * @author johnniang
 */
@Component
public class DelegateExtensionClient implements ExtensionClient {

    private final ReactiveExtensionClient client;

    public DelegateExtensionClient(ReactiveExtensionClient client) {
        this.client = client;
    }

    @Override
    public <E extends Extension> List<E> list(Class<E> type, Predicate<E> predicate,
        Comparator<E> comparator) {
        return client.list(type, predicate, comparator).collectList().block();
    }

    @Override
    public <E extends Extension> ListResult<E> list(Class<E> type, Predicate<E> predicate,
        Comparator<E> comparator, int page, int size) {
        return client.list(type, predicate, comparator, page, size).block();
    }

    @Override
    public <E extends Extension> Optional<E> fetch(Class<E> type, String name) {
        return client.fetch(type, name).blockOptional();
    }

    @Override
    public Optional<Unstructured> fetch(GroupVersionKind gvk, String name) {
        return client.fetch(gvk, name).blockOptional();
    }

    @Override
    public <E extends Extension> void create(E extension) {
        client.create(extension).block();
    }

    @Override
    public <E extends Extension> void update(E extension) {
        client.update(extension).block();
    }

    @Override
    public <E extends Extension> void delete(E extension) {
        client.delete(extension).block();
    }

    @Override
    public void watch(Watcher watcher) {
        client.watch(watcher);
    }
}
