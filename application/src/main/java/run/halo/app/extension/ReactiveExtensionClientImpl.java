package run.halo.app.extension;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.springframework.util.StringUtils.hasText;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
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

    private final ObjectMapper objectMapper;

    public ReactiveExtensionClientImpl(ReactiveExtensionStoreClient client,
        ExtensionConverter converter, SchemeManager schemeManager, ObjectMapper objectMapper) {
        this.client = client;
        this.converter = converter;
        this.schemeManager = schemeManager;
        this.objectMapper = objectMapper;
        this.watchers = new Watcher.WatcherComposite();
    }

    @Override
    public <E extends Extension> Flux<E> list(Class<E> type, Predicate<E> predicate,
        Comparator<E> comparator) {
        var scheme = schemeManager.get(type);
        var prefix = ExtensionStoreUtil.buildStoreNamePrefix(scheme);

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
        var storeName = ExtensionStoreUtil.buildStoreName(schemeManager.get(type), name);
        return client.fetchByName(storeName)
            .map(extensionStore -> converter.convertFrom(type, extensionStore));
    }

    @Override
    public Mono<Unstructured> fetch(GroupVersionKind gvk, String name) {
        var storeName = ExtensionStoreUtil.buildStoreName(schemeManager.get(gvk), name);
        return client.fetchByName(storeName)
            .map(extensionStore -> converter.convertFrom(Unstructured.class, extensionStore));
    }

    @Override
    public <E extends Extension> Mono<E> get(Class<E> type, String name) {
        return fetch(type, name)
            .switchIfEmpty(Mono.error(() -> {
                var gvk = GroupVersionKind.fromExtension(type);
                return new ExtensionNotFoundException(gvk, name);
            }));
    }

    private Mono<Unstructured> get(GroupVersionKind gvk, String name) {
        return fetch(gvk, name)
            .switchIfEmpty(Mono.error(() -> new ExtensionNotFoundException(gvk, name)));
    }

    @Override
    @SuppressWarnings("unchecked")
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
    @SuppressWarnings("unchecked")
    public <E extends Extension> Mono<E> update(E extension) {
        // Refactor the atomic reference if we have a better solution.
        return getLatest(extension).flatMap(old -> {
            var oldJsonExt = new JsonExtension(objectMapper, old);
            var newJsonExt = new JsonExtension(objectMapper, extension);
            // reset some mandatory fields
            var oldMetadata = oldJsonExt.getMetadata();
            var newMetadata = newJsonExt.getMetadata();
            newMetadata.setCreationTimestamp(oldMetadata.getCreationTimestamp());
            newMetadata.setGenerateName(oldMetadata.getGenerateName());

            if (Objects.equals(oldJsonExt, newJsonExt)) {
                // skip updating if not data changed.
                return Mono.just(extension);
            }

            var onlyStatusChanged =
                isOnlyStatusChanged(oldJsonExt.getInternal(), newJsonExt.getInternal());

            var store = this.converter.convertTo(newJsonExt);
            var updated = client.update(store.getName(), store.getVersion(), store.getData())
                .map(ext -> converter.convertFrom((Class<E>) extension.getClass(), ext));
            if (!onlyStatusChanged) {
                updated = updated.doOnNext(ext -> watchers.onUpdate(old, ext));
            }
            return updated;
        });
    }

    private Mono<? extends Extension> getLatest(Extension extension) {
        if (extension instanceof Unstructured unstructured) {
            return get(unstructured.groupVersionKind(), unstructured.getMetadata().getName());
        }
        return get(extension.getClass(), extension.getMetadata().getName());
    }

    @Override
    @SuppressWarnings("unchecked")
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

    private static boolean isOnlyStatusChanged(ObjectNode oldNode, ObjectNode newNode) {
        if (Objects.equals(oldNode, newNode)) {
            return false;
        }
        // WARNING!!!
        // Do not edit the ObjectNode
        var oldFields = new HashSet<String>();
        var newFields = new HashSet<String>();
        oldNode.fieldNames().forEachRemaining(oldFields::add);
        newNode.fieldNames().forEachRemaining(newFields::add);
        oldFields.remove("status");
        newFields.remove("status");
        if (!Objects.equals(oldFields, newFields)) {
            return false;
        }
        for (var field : oldFields) {
            if (!Objects.equals(oldNode.get(field), newNode.get(field))) {
                return false;
            }
        }
        return true;
    }
}
