package run.halo.app.extension;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.springframework.util.StringUtils.hasText;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.util.Predicates;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
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
        var scheme = schemeManager.get(type);
        var prefix = ExtensionUtil.buildStoreNamePrefix(scheme);

        return client.listByNamePrefix(prefix)
            .map(extensionStore -> converter.convertFrom(type, extensionStore))
            .filter(predicate == null ? Predicates.isTrue() : predicate)
            .sort(comparator == null ? Comparator.naturalOrder() : comparator);
    }

    @Override
    public <E extends Extension> Mono<ListResult<E>> list(Class<E> type, Predicate<E> predicate,
        Comparator<E> comparator, int page, int size) {
        var extensions = list(type, predicate, comparator);
        var totalMono = extensions.count();
        if (page > 0) {
            extensions = extensions.skip(((long) (page - 1)) * (long) size);
        }
        if (size > 0) {
            extensions = extensions.take(size);
        }
        return extensions.collectList().zipWith(totalMono)
            .map(tuple -> {
                List<E> content = tuple.getT1();
                Long total = tuple.getT2();
                return new ListResult<>(page, size, total, content);
            });
    }

    @Override
    public <E extends Extension> Mono<E> fetch(Class<E> type, String name) {
        var storeName = ExtensionUtil.buildStoreName(schemeManager.get(type), name);
        return client.fetchByName(storeName)
            .map(extensionStore -> converter.convertFrom(type, extensionStore));
    }

    @Override
    public Mono<Unstructured> fetch(GroupVersionKind gvk, String name) {
        var storeName = ExtensionUtil.buildStoreName(schemeManager.get(gvk), name);
        return client.fetchByName(storeName)
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
                var metadata = extension.getMetadata();
                // those fields should be managed by halo.
                metadata.setCreationTimestamp(Instant.now());
                metadata.setDeletionTimestamp(null);
                metadata.setVersion(null);

                if (!hasText(metadata.getName())) {
                    if (!hasText(metadata.getGenerateName())) {
                        throw new IllegalArgumentException(
                            "The metadata.generateName must not be blank when metadata.name is "
                                + "blank");
                    }
                    // generate name with random text
                    metadata.setName(metadata.getGenerateName() + randomAlphabetic(5));
                }
                extension.setMetadata(metadata);
            })
            .map(converter::convertTo)
            .flatMap(extStore -> client.create(extStore.getName(), extStore.getData())
                .map(created -> converter.convertFrom((Class<E>) extension.getClass(), created))
                .doOnNext(watchers::onAdd))
            .retryWhen(Retry.backoff(3, Duration.ofMillis(100))
                // retry when generateName is set
                .filter(t -> t instanceof DataIntegrityViolationException
                    && hasText(extension.getMetadata().getGenerateName())));
    }

    @Override
    public <E extends Extension> Mono<E> update(E extension) {
        // overwrite some fields
        Mono<? extends Extension> mono;
        if (extension instanceof Unstructured unstructured) {
            mono = get(unstructured.groupVersionKind(), extension.getMetadata().getName());
        } else {
            mono = get(extension.getClass(), extension.getMetadata().getName());
        }
        return mono
            .map(old -> {
                // reset some fields
                var oldMetadata = old.getMetadata();
                var newMetadata = extension.getMetadata();
                newMetadata.setCreationTimestamp(oldMetadata.getCreationTimestamp());
                newMetadata.setDeletionTimestamp(oldMetadata.getDeletionTimestamp());
                extension.setMetadata(newMetadata);
                return converter.convertTo(extension);
            })
            .flatMap(extensionStore -> client.update(extensionStore.getName(),
                extensionStore.getVersion(),
                extensionStore.getData()))
            .map(updated -> converter.convertFrom((Class<E>) extension.getClass(), updated))
            .doOnNext(updated -> watchers.onUpdate(extension, updated));
    }

    @Override
    public <E extends Extension> Mono<E> delete(E extension) {
        // set deletionTimestamp
        extension.getMetadata().setDeletionTimestamp(Instant.now());
        var extensionStore = converter.convertTo(extension);
        return client.update(extensionStore.getName(), extensionStore.getVersion(),
                extensionStore.getData())
            .map(deleted -> converter.convertFrom((Class<E>) extension.getClass(), deleted))
            .doOnNext(watchers::onDelete);
    }

    @Override
    public void watch(Watcher watcher) {
        this.watchers.addWatcher(watcher);
    }

}
