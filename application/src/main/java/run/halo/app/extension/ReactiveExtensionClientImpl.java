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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Predicates;
import org.springframework.stereotype.Component;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import run.halo.app.extension.exception.ExtensionNotFoundException;
import run.halo.app.extension.index.DefaultExtensionIterator;
import run.halo.app.extension.index.ExtensionIterator;
import run.halo.app.extension.index.IndexedQueryEngine;
import run.halo.app.extension.index.IndexerFactory;
import run.halo.app.extension.store.ReactiveExtensionStoreClient;

@Slf4j
@Component
public class ReactiveExtensionClientImpl implements ReactiveExtensionClient {

    private final ReactiveExtensionStoreClient client;

    private final ExtensionConverter converter;

    private final SchemeManager schemeManager;

    private final Watcher.WatcherComposite watchers = new Watcher.WatcherComposite();

    private final ObjectMapper objectMapper;

    private final IndexerFactory indexerFactory;

    private final IndexedQueryEngine indexedQueryEngine;

    private final ConcurrentMap<GroupKind, AtomicBoolean> indexBuildingState =
        new ConcurrentHashMap<>();

    private TransactionalOperator transactionalOperator;

    public ReactiveExtensionClientImpl(ReactiveExtensionStoreClient client,
        ExtensionConverter converter, SchemeManager schemeManager, ObjectMapper objectMapper,
        IndexerFactory indexerFactory, IndexedQueryEngine indexedQueryEngine,
        ReactiveTransactionManager reactiveTransactionManager) {
        this.client = client;
        this.converter = converter;
        this.schemeManager = schemeManager;
        this.objectMapper = objectMapper;
        this.indexerFactory = indexerFactory;
        this.indexedQueryEngine = indexedQueryEngine;
        this.transactionalOperator = TransactionalOperator.create(reactiveTransactionManager);
    }

    /**
     * Only for test.
     */
    void setTransactionalOperator(TransactionalOperator transactionalOperator) {
        this.transactionalOperator = transactionalOperator;
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
    public <E extends Extension> Flux<E> listAll(Class<E> type, ListOptions options, Sort sort) {
        return listBy(type, options, PageRequestImpl.ofSize(0).withSort(sort))
            .flatMapIterable(ListResult::getItems);
    }

    @Override
    public <E extends Extension> Mono<ListResult<E>> listBy(Class<E> type, ListOptions options,
        PageRequest page) {
        var scheme = schemeManager.get(type);
        return Mono.fromSupplier(
                () -> indexedQueryEngine.retrieve(scheme.groupVersionKind(), options, page)
            )
            .flatMap(objectKeys -> {
                var storeNames = objectKeys.get()
                    .map(objectKey -> ExtensionStoreUtil.buildStoreName(scheme, objectKey))
                    .toList();
                final long startTimeMs = System.currentTimeMillis();
                return client.listByNames(storeNames)
                    .map(extensionStore -> converter.convertFrom(type, extensionStore))
                    .doFinally(s -> {
                        log.debug("Successfully retrieved by names from db for {} in {}ms",
                            scheme.groupVersionKind(), System.currentTimeMillis() - startTimeMs);
                    })
                    .collectList()
                    .map(result -> new ListResult<>(page.getPageNumber(), page.getPageSize(),
                        objectKeys.getTotal(), result));
            })
            .defaultIfEmpty(ListResult.emptyResult());
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
    public <E extends Extension> Mono<E> create(E extension) {
        checkClientWritable(extension);
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
            .flatMap(extStore -> doCreate(extension, extStore.getName(), extStore.getData())
                .doOnNext(watchers::onAdd)
            )
            .retryWhen(Retry.backoff(3, Duration.ofMillis(100))
                // retry when generateName is set
                .filter(t -> t instanceof DataIntegrityViolationException
                    && hasText(extension.getMetadata().getGenerateName()))
            );
    }

    @Override
    public <E extends Extension> Mono<E> update(E extension) {
        checkClientWritable(extension);
        // Refactor the atomic reference if we have a better solution.
        return getLatest(extension).flatMap(old -> {
            var oldJsonExt = new JsonExtension(objectMapper, old);
            var newJsonExt = new JsonExtension(objectMapper, extension);
            // reset some mandatory fields
            var oldMetadata = oldJsonExt.getMetadata();
            var newMetadata = newJsonExt.getMetadata();
            newMetadata.setCreationTimestamp(oldMetadata.getCreationTimestamp());
            newMetadata.setGenerateName(oldMetadata.getGenerateName());

            // If the extension is an unstructured, the version type may be integer instead of long.
            // reset metadata.version for long type.
            oldMetadata.setVersion(oldMetadata.getVersion());
            newMetadata.setVersion(newMetadata.getVersion());

            if (Objects.equals(oldJsonExt, newJsonExt)) {
                // skip updating if not data changed.
                return Mono.just(extension);
            }

            var onlyStatusChanged =
                isOnlyStatusChanged(oldJsonExt.getInternal(), newJsonExt.getInternal());

            var store = this.converter.convertTo(newJsonExt);
            var updated = doUpdate(extension, store.getName(), store.getVersion(), store.getData());
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
    public <E extends Extension> Mono<E> delete(E extension) {
        checkClientWritable(extension);
        // set deletionTimestamp
        extension.getMetadata().setDeletionTimestamp(Instant.now());
        var extensionStore = converter.convertTo(extension);
        return doUpdate(extension, extensionStore.getName(),
            extensionStore.getVersion(), extensionStore.getData()
        ).doOnNext(watchers::onDelete);
    }

    @Override
    public IndexedQueryEngine indexedQueryEngine() {
        return this.indexedQueryEngine;
    }

    @SuppressWarnings("unchecked")
    <E extends Extension> Mono<E> doCreate(E oldExtension, String name, byte[] data) {
        return Mono.defer(() -> {
            var gvk = oldExtension.groupVersionKind();
            var type = (Class<E>) oldExtension.getClass();
            var indexer = indexerFactory.getIndexer(gvk);
            return client.create(name, data)
                .map(created -> converter.convertFrom(type, created))
                .doOnNext(indexer::indexRecord)
                .as(transactionalOperator::transactional);
        });
    }

    @SuppressWarnings("unchecked")
    <E extends Extension> Mono<E> doUpdate(E oldExtension, String name, Long version, byte[] data) {
        return Mono.defer(() -> {
            var type = (Class<E>) oldExtension.getClass();
            var indexer = indexerFactory.getIndexer(oldExtension.groupVersionKind());
            return client.update(name, version, data)
                .map(updated -> converter.convertFrom(type, updated))
                .doOnNext(indexer::updateRecord)
                .as(transactionalOperator::transactional);
        });
    }

    /**
     * If the extension is being updated, we should the index is not building index for the
     * extension, otherwise the {@link IllegalStateException} will be thrown.
     */
    private <E extends Extension> void checkClientWritable(E extension) {
        var buildingState = indexBuildingState.get(extension.groupVersionKind().groupKind());
        if (buildingState != null && buildingState.get()) {
            throw new IllegalStateException("Index is building for " + extension.groupVersionKind()
                + ", please wait for a moment and try again.");
        }
    }

    void setIndexBuildingStateFor(GroupKind groupKind, boolean building) {
        indexBuildingState.computeIfAbsent(groupKind, k -> new AtomicBoolean(building))
            .set(building);
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

    @Component
    @RequiredArgsConstructor
    class IndexBuildsManager {
        private final SchemeManager schemeManager;
        private final IndexerFactory indexerFactory;
        private final ExtensionConverter converter;
        private final ReactiveExtensionStoreClient client;
        private final SchemeWatcherManager schemeWatcherManager;

        @NonNull
        private ExtensionIterator<Extension> createExtensionIterator(Scheme scheme) {
            var type = scheme.type();
            var prefix = ExtensionStoreUtil.buildStoreNamePrefix(scheme);
            return new DefaultExtensionIterator<>(pageable ->
                client.listByNamePrefix(prefix, pageable)
                    .map(page ->
                        page.map(store -> (Extension) converter.convertFrom(type, store))
                    )
                    .block()
            );
        }

        @EventListener(ContextRefreshedEvent.class)
        public void startBuildingIndex() {
            final long startTimeMs = System.currentTimeMillis();
            log.info("Start building index for all extensions, please wait...");
            schemeManager.schemes()
                .forEach(this::createIndexerFor);

            schemeWatcherManager.register(event -> {
                if (event instanceof SchemeWatcherManager.SchemeRegistered schemeRegistered) {
                    createIndexerFor(schemeRegistered.getNewScheme());
                    return;
                }
                if (event instanceof SchemeWatcherManager.SchemeUnregistered schemeUnregistered) {
                    var scheme = schemeUnregistered.getDeletedScheme();
                    indexerFactory.removeIndexer(scheme);
                }
            });
            log.info("Successfully built index in {}ms, Preparing to lunch application...",
                System.currentTimeMillis() - startTimeMs);
        }

        private void createIndexerFor(Scheme scheme) {
            setIndexBuildingStateFor(scheme.groupVersionKind().groupKind(), true);
            indexerFactory.createIndexerFor(scheme.type(), createExtensionIterator(scheme));
            setIndexBuildingStateFor(scheme.groupVersionKind().groupKind(), false);
        }
    }
}
