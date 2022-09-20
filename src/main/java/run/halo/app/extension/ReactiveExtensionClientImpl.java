package run.halo.app.extension;

import static run.halo.app.extension.ExtensionUtil.buildStoreName;

import java.time.Instant;
import java.util.Comparator;
import java.util.function.Predicate;
import org.springframework.data.util.Predicates;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.exception.ExtensionNotFoundException;
import run.halo.app.extension.store.ReactiveExtensionStoreClient;

@Component
public class ReactiveExtensionClientImpl implements ReactiveExtensionClient {

    private final ReactiveExtensionStoreClient client;

    private final ExtensionConverter converter;

    private final SchemeManager schemeManager;

    private final Watcher.WatcherComposite watchers;

    public ReactiveExtensionClientImpl(ReactiveExtensionStoreClient client,
                                       ExtensionConverter converter, SchemeManager schemeManager) {
        this.client = client;
        this.converter = converter;
        this.schemeManager = schemeManager;
        this.watchers = new Watcher.WatcherComposite();
    }

    @Override
    public <E extends Extension> Flux<E> list(Class<E> type, Predicate<E> predicate,
                                              Comparator<E> comparator) {
        return Mono.fromCallable(
                () -> {
                    var scheme = schemeManager.get(type);
                    return ExtensionUtil.buildStoreNamePrefix(scheme);
                })
            .flatMapMany(client::listByNamePrefix)
            .map(extensionStore -> converter.convertFrom(type, extensionStore))
            .filter(predicate == null ? Predicates.isTrue() : predicate)
            .sort(comparator == null ? Comparator.naturalOrder() : comparator);
    }

    @Override
    public <E extends Extension> Mono<ListResult<E>> list(Class<E> type, Predicate<E> predicate,
                                                          Comparator<E> comparator, int page,
                                                          int size) {
        return list(type, predicate, comparator)
            .transform(extensions -> {
                var countMono = extensions.count();
                var partialExtensions = extensions;
                if (page > 0) {
                    partialExtensions = partialExtensions.skip(((long) (page - 1)) * (long) size);
                }
                if (size > 0) {
                    partialExtensions = partialExtensions.take(size);
                }
                return partialExtensions.collectList()
                    .flatMap(content ->
                        countMono.map(count -> new ListResult<>(page, size, count, content)));
            })
            .next();
    }

    @Override
    public <E extends Extension> Mono<E> fetch(Class<E> type, String name) {
        return Mono.fromCallable(() -> buildStoreName(schemeManager.get(type), name))
            .flatMap(client::fetchByName)
            .map(extensionStore -> converter.convertFrom(type, extensionStore));
    }

    @Override
    public Mono<Unstructured> fetch(GroupVersionKind gvk, String name) {
        return Mono.fromCallable(() -> buildStoreName(schemeManager.get(gvk), name))
            .flatMap(client::fetchByName)
            .map(extensionStore -> converter.convertFrom(Unstructured.class, extensionStore));
    }

    @Override
    public <E extends Extension> Mono<E> get(Class<E> type, String name) {
        return fetch(type, name)
            .switchIfEmpty(Mono.error(() -> new ExtensionNotFoundException(
                "Extension " + type.getName() + " with name " + name + " not found")));
    }

    private Mono<Unstructured> get(GroupVersionKind gvk, String name) {
        return fetch(gvk, name)
            .switchIfEmpty(Mono.error(() -> new ExtensionNotFoundException(
                "Extension " + gvk + " with name " + name + " not found")));
    }

    @Override
    public <E extends Extension> Mono<E> create(E extension) {
        return Mono.just(extension)
            .doOnNext(ext -> {
                var metadata = ext.getMetadata();
                // those fields should be managed by halo.
                metadata.setCreationTimestamp(Instant.now());
                metadata.setDeletionTimestamp(null);
                metadata.setVersion(null);
            })
            .map(converter::convertTo)
            .flatMap(store -> client.create(store.getName(), store.getData()))
            .map(created -> converter.convertFrom((Class<E>) extension.getClass(), created))
            .doOnNext(watchers::onAdd);
    }

    @Override
    public <E extends Extension> Mono<E> update(E extension) {
        return Mono.just(extension)
            .flatMap(ext -> {
                if (ext instanceof Unstructured unstructured) {
                    return get(unstructured.groupVersionKind(), ext.getMetadata().getName());
                }
                return get(ext.getClass(), ext.getMetadata().getName());
            })
            .map(old -> {
                // reset some fields
                var oldMetadata = old.getMetadata();
                var newMetadata = extension.getMetadata();
                newMetadata.setCreationTimestamp(oldMetadata.getCreationTimestamp());
                newMetadata.setDeletionTimestamp(oldMetadata.getDeletionTimestamp());
                extension.setMetadata(newMetadata);
                return extension;
            })
            .map(converter::convertTo)
            .flatMap(extensionStore -> client.update(extensionStore.getName(),
                extensionStore.getVersion(),
                extensionStore.getData()))
            .map(updated -> converter.convertFrom((Class<E>) extension.getClass(), updated))
            .doOnNext(updated -> watchers.onUpdate(extension, updated));
    }

    @Override
    public <E extends Extension> Mono<E> delete(E extension) {
        return Mono.just(extension)
            .doOnNext(ext -> {
                // set deletionTimestamp
                ext.getMetadata().setDeletionTimestamp(Instant.now());
            })
            .map(converter::convertTo)
            .flatMap(store -> client.update(store.getName(), store.getVersion(), store.getData()))
            .map(deleted -> converter.convertFrom((Class<E>) extension.getClass(), deleted))
            .doOnNext(watchers::onDelete);
    }

    @Override
    public void watch(Watcher watcher) {
        this.watchers.addWatcher(watcher);
    }

}
