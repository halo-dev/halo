package run.halo.app.extension;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import run.halo.app.extension.store.ExtensionStoreClient;

/**
 * DefaultExtensionClient is default implementation of ExtensionClient.
 *
 * @author johnniang
 */
@Component
public class DefaultExtensionClient implements ExtensionClient {

    private final ExtensionStoreClient storeClient;
    private final ExtensionConverter converter;

    private final SchemeManager schemeManager;

    private final Watcher.WatcherComposite watchers;

    private final ReactiveExtensionClient reactiveClient;

    public DefaultExtensionClient(ExtensionStoreClient storeClient,
        ExtensionConverter converter,
        SchemeManager schemeManager, ReactiveExtensionClient reactiveClient) {
        this.storeClient = storeClient;
        this.converter = converter;
        this.schemeManager = schemeManager;
        this.reactiveClient = reactiveClient;

        watchers = new Watcher.WatcherComposite();
    }

    @Override
    public <E extends Extension> List<E> list(Class<E> type, Predicate<E> predicate,
        Comparator<E> comparator) {
        var scheme = schemeManager.get(type);
        var storeNamePrefix = ExtensionUtil.buildStoreNamePrefix(scheme);

        var storesStream = storeClient.listByNamePrefix(storeNamePrefix).stream()
            .map(extensionStore -> converter.convertFrom(type, extensionStore));
        if (predicate != null) {
            storesStream = storesStream.filter(predicate);
        }
        if (comparator != null) {
            storesStream = storesStream.sorted(comparator);
        }
        return storesStream.toList();
    }

    @Override
    public <E extends Extension> ListResult<E> list(Class<E> type, Predicate<E> predicate,
        Comparator<E> comparators, int page, int size) {
        var extensions = list(type, predicate, comparators);
        var extensionStream = extensions.stream();
        if (page > 0) {
            extensionStream = extensionStream.skip(((long) (page - 1)) * (long) size);
        }
        if (size > 0) {
            extensionStream = extensionStream.limit(size);
        }
        var content = extensionStream.toList();

        return new ListResult<>(page, size, extensions.size(), content);
    }

    @Override
    public <E extends Extension> Optional<E> fetch(Class<E> type, String name) {
        return fetch(schemeManager.get(type), name, type);
    }

    @Override
    public Optional<Unstructured> fetch(GroupVersionKind gvk, String name) {
        return fetch(schemeManager.get(gvk), name, Unstructured.class);
    }

    private <E extends Extension> Optional<E> fetch(Scheme scheme, String name, Class<E> type) {
        var storeName = ExtensionUtil.buildStoreName(scheme, name);
        return storeClient.fetchByName(storeName)
            .map(extensionStore -> converter.convertFrom(type, extensionStore));
    }

    @Override
    public <E extends Extension> void create(E extension) {
        var metadata = extension.getMetadata();
        metadata.setCreationTimestamp(Instant.now());
        // extension.setMetadata(metadata);
        var extensionStore = converter.convertTo(extension);
        var createdStore = storeClient.create(extensionStore.getName(), extensionStore.getData());
        var createdExt = converter.convertFrom(extension.getClass(), createdStore);
        watchers.onAdd(createdExt);
    }

    @Override
    public <E extends Extension> void update(E extension) {
        var extensionStore = converter.convertTo(extension);
        Assert.notNull(extension.getMetadata().getVersion(),
            "Extension version must not be null when updating");
        var updatedStore = storeClient.update(extensionStore.getName(), extensionStore.getVersion(),
            extensionStore.getData());
        var updatedExt = converter.convertFrom(extension.getClass(), updatedStore);
        watchers.onUpdate(extension, updatedExt);
    }

    @Override
    public <E extends Extension> void delete(E extension) {
        extension.getMetadata().setDeletionTimestamp(Instant.now());
        var extensionStore = converter.convertTo(extension);
        var deleteStore = storeClient.update(extensionStore.getName(), extensionStore.getVersion(),
            extensionStore.getData());
        Extension deleteExt = converter.convertFrom(extension.getClass(), deleteStore);
        watchers.onDelete(deleteExt);
    }

    @Override
    public void watch(Watcher watcher) {
        this.watchers.addWatcher(watcher);
        // TODO Refactor the watch process. At present, we have to ensure the compatibility.
        reactiveClient.watch(watcher);
    }

}
