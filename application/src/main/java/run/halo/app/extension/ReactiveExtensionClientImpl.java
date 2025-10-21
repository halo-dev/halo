package run.halo.app.extension;

import static org.apache.commons.lang3.RandomStringUtils.secure;
import static org.springframework.util.StringUtils.hasText;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Predicates;
import org.springframework.stereotype.Component;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;
import run.halo.app.extension.exception.ExtensionNotFoundException;
import run.halo.app.extension.index.IndexEngine;
import run.halo.app.extension.index.IndexedQueryEngine;
import run.halo.app.extension.store.ReactiveExtensionStoreClient;

@Slf4j
@Component
public class ReactiveExtensionClientImpl implements ReactiveExtensionClient {

    public static final int GENERATE_NAME_RANDOM_LENGTH = 8;

    private final ReactiveExtensionStoreClient client;

    private final ExtensionConverter converter;

    private final SchemeManager schemeManager;

    private final Watcher.WatcherComposite watchers = new Watcher.WatcherComposite();

    private final ObjectMapper objectMapper;

    private final IndexEngine indexEngine;

    private Scheduler scheduler;

    private TransactionalOperator transactionalOperator;

    public ReactiveExtensionClientImpl(ReactiveExtensionStoreClient client,
        ExtensionConverter converter, SchemeManager schemeManager, ObjectMapper objectMapper,
        IndexEngine indexEngine,
        ReactiveTransactionManager reactiveTransactionManager) {
        this.client = client;
        this.converter = converter;
        this.schemeManager = schemeManager;
        this.objectMapper = objectMapper;
        this.indexEngine = indexEngine;
        this.transactionalOperator = TransactionalOperator.create(reactiveTransactionManager);
        this.scheduler = Schedulers.boundedElastic();
    }

    /**
     * Only for test.
     *
     * @param scheduler the scheduler to set
     */
    void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
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
        var nullSafeSort = Optional.ofNullable(sort)
            .orElseGet(() -> {
                log.warn("The sort parameter is null, it is recommended to use Sort.unsorted() "
                    + "instead and the compatibility support for null will be removed in the "
                    + "subsequent version.");
                return Sort.unsorted();
            });
        var scheme = schemeManager.get(type);

        return Mono.fromCallable(
                () -> indexEngine.retrieveAll(scheme.type(), options, nullSafeSort)
            )
            .subscribeOn(this.scheduler)
            .flatMapMany(objectKeys -> {
                var storeNames = StreamSupport.stream(objectKeys.spliterator(), false)
                    .map(objectKey -> ExtensionStoreUtil.buildStoreName(scheme, objectKey))
                    .toList();
                if (log.isDebugEnabled()) {
                    if (storeNames.size() > 500) {
                        log.warn("""
                                The number of objects retrieved by listAll is too large ({}) \
                                and it is recommended to use paging query.\
                                """,
                            storeNames.size()
                        );
                    }
                }
                long startTimeMs = System.currentTimeMillis();
                return client.listByNames(storeNames)
                    .map(extensionStore -> converter.convertFrom(type, extensionStore))
                    .doOnComplete(() -> log.debug(
                        "Successfully retrieved all by names from db for {} in {}ms",
                        scheme.groupVersionKind(), System.currentTimeMillis() - startTimeMs)
                    );
            });
    }

    @Override
    public <E extends Extension> Flux<String> listAllNames(
        Class<E> type, ListOptions options, Sort sort
    ) {
        var scheme = schemeManager.get(type);
        return Mono.fromCallable(() -> indexEngine.retrieveAll(scheme.type(), options, sort))
            .subscribeOn(this.scheduler)
            .flatMapMany(Flux::fromIterable);
    }

    @Override
    public <E extends Extension> Flux<String> listTopNames(
        Class<E> type, ListOptions options, Sort sort, int topN
    ) {
        var scheme = schemeManager.get(type);
        return Mono.fromCallable(() -> indexEngine.retrieveTopN(scheme.type(), options, sort, topN))
            .subscribeOn(this.scheduler)
            .flatMapMany(Flux::fromIterable);
    }

    @Override
    public <E extends Extension> Mono<ListResult<E>> listBy(Class<E> type, ListOptions options,
        PageRequest page) {
        var scheme = schemeManager.get(type);
        return Mono.fromCallable(() -> indexEngine.retrieve(scheme.type(), options, page))
            .subscribeOn(this.scheduler)
            .flatMap(listResult -> {
                var storeNames = listResult.get()
                    .map(objectKey -> ExtensionStoreUtil.buildStoreName(scheme, objectKey))
                    .toList();
                final long startTimeMs = System.currentTimeMillis();
                return client.listByNames(storeNames)
                    .map(extensionStore -> converter.convertFrom(type, extensionStore))
                    .doOnComplete(() -> log.debug(
                        "Successfully retrieved by names from db for {} in {}ms",
                        scheme.groupVersionKind(), System.currentTimeMillis() - startTimeMs)
                    )
                    .collectList()
                    .map(items -> new ListResult<>(page.getPageNumber(), page.getPageSize(),
                        listResult.getTotal(), items)
                    );
            })
            .defaultIfEmpty(ListResult.emptyResult());
    }

    @Override
    public <E extends Extension> Mono<ListResult<String>> listNamesBy(Class<E> type,
        ListOptions options, PageRequest pageable) {
        var scheme = schemeManager.get(type);
        return Mono.fromCallable(() -> indexEngine.retrieve(scheme.type(), options, pageable))
            .subscribeOn(this.scheduler);
    }

    @Override
    public <E extends Extension> Mono<Long> countBy(Class<E> type, ListOptions options) {
        var scheme = schemeManager.get(type);
        return Mono.fromCallable(() -> indexEngine.count(scheme.type(), options))
            .subscribeOn(this.scheduler);
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

    private Mono<JsonExtension> fetchJsonExtension(GroupVersionKind gvk, String name) {
        var storeName = ExtensionStoreUtil.buildStoreName(schemeManager.get(gvk), name);
        return client.fetchByName(storeName)
            .map(extensionStore -> converter.convertFrom(JsonExtension.class, extensionStore));
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
    public Mono<JsonExtension> getJsonExtension(GroupVersionKind gvk, String name) {
        return fetchJsonExtension(gvk, name)
            .switchIfEmpty(Mono.error(() -> new ExtensionNotFoundException(gvk, name)));
    }

    @Override
    public <E extends Extension> Mono<E> create(E extension) {
        return Mono.fromCallable(
                () -> {
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
                        metadata.setName(metadata.getGenerateName() + secure()
                            .nextAlphanumeric(GENERATE_NAME_RANDOM_LENGTH)
                            // Prevent data conflicts caused by database case sensitivity
                            .toLowerCase()
                        );
                    }
                    extension.setMetadata(metadata);
                    return converter.convertTo(extension);
                })
            // the method secureStrong() may invoke blocking SecureRandom, so we need to subscribe
            // on boundedElastic thread pool.
            .subscribeOn(this.scheduler)
            .flatMap(extStore -> doCreate(extension, extStore.getName(), extStore.getData())
                .doOnNext(created -> watchers.onAdd(convertToRealExtension(created)))
            )
            .retryWhen(Retry.backoff(3, Duration.ofMillis(100))
                // retry when generateName is set
                .filter(t -> t instanceof DataIntegrityViolationException
                    && hasText(extension.getMetadata().getGenerateName()))
            );
    }

    @Override
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
                updated = updated.doOnNext(ext -> watchers.onUpdate(convertToRealExtension(old),
                    convertToRealExtension(ext))
                );
            }
            return updated;
        });
    }

    private Mono<? extends Extension> getLatest(Extension extension) {
        if (extension instanceof Unstructured) {
            return get(extension.groupVersionKind(), extension.getMetadata().getName());
        }
        if (extension instanceof JsonExtension) {
            return getJsonExtension(
                extension.groupVersionKind(),
                extension.getMetadata().getName()
            );
        }
        return get(extension.getClass(), extension.getMetadata().getName());
    }

    @Override
    public <E extends Extension> Mono<E> delete(E extension) {
        // make sure the version is not null, or it will cause extension insertion.
        Assert.notNull(extension.getMetadata().getVersion(), "Extension version must not be null");
        // set deletionTimestamp
        extension.getMetadata().setDeletionTimestamp(Instant.now());
        var extensionStore = converter.convertTo(extension);
        return doUpdate(extension, extensionStore.getName(),
            extensionStore.getVersion(), extensionStore.getData()
        ).doOnNext(updated -> watchers.onDelete(convertToRealExtension(extension)));
    }

    @Override
    public IndexedQueryEngine indexedQueryEngine() {
        return new IndexedQueryEngine() {
            @Override
            public ListResult<String> retrieve(GroupVersionKind gvk, ListOptions options,
                PageRequest page) {
                var scheme = schemeManager.get(gvk);
                return indexEngine.retrieve(scheme.type(), options, page);
            }

            @Override
            public List<String> retrieveAll(GroupVersionKind gvk, ListOptions options, Sort sort) {
                var scheme = schemeManager.get(gvk);
                return StreamSupport.stream(
                        indexEngine.retrieveAll(scheme.type(), options, sort).spliterator(), false
                    )
                    .toList();
            }
        };
    }

    /**
     * <p>Note of transactional:</p>
     * <p>doSomething does not have a transaction, but methodOne and methodTwo have their own
     * transactions.</p>
     * <p>If methodTwo fails and throws an exception, the transaction in methodTwo will be rolled
     * back, but the transaction in methodOne will not.</p>
     * <pre>
     * public void doSomething() {
     *     // with manual transaction
     *     methodOne();
     *     // with manual transaction
     *     methodTwo();
     * }
     * </pre>
     * <p>If doSomething is annotated with &#64;Transactional, both methodOne and methodTwo will be
     * executed within the same transaction context.</p>
     * <p>If methodTwo fails and throws an exception, the entire transaction will be rolled back,
     * including any changes made by methodOne.</p>
     * <p>This ensures that all operations within the doSomething method either succeed or fail
     * together.</p>
     * <p>This example advises against adding transaction annotations to the outer method that
     * invokes {@link #update(Extension)} or {@link #create(Extension)}, as doing so could
     * undermine the intended transactional integrity of this method.</p>
     *
     * <p>Note another point:</p>
     * After executing the {@code client.create(name, data)} method, an attempt is made to
     * indexRecord. However, indexRecord might fail due to duplicate keys in the unique index,
     * causing the index creation to fail. In such cases, the data created by {@code client
     * .create(name, data)} should be rolled back to maintain consistency between the index and
     * the data.
     * <p><b>Until a better solution is found for this consistency problem, do not remove the
     * manual transaction here.</b></p>
     * <pre>
     * client.create(name, data)
     *  .doOnNext(extension -> indexer.indexRecord(convertToRealExtension(extension)))
     *  .as(transactionalOperator::transactional);
     * </pre>
     */
    @SuppressWarnings("unchecked")
    <E extends Extension> Mono<E> doCreate(E oldExtension, String name, byte[] data) {
        return Mono.defer(() -> {
            var type = (Class<E>) oldExtension.getClass();
            return client.create(name, data)
                .map(created -> converter.convertFrom(type, created))
                .flatMap(extension -> Mono.fromRunnable(
                        () -> this.indexEngine.insert(List.of(convertToRealExtension(extension))))
                    .subscribeOn(this.scheduler)
                    .thenReturn(extension)
                )
                .as(transactionalOperator::transactional);
        });
    }

    /**
     * see also {@link #doCreate(Extension, String, byte[])}.
     */
    @SuppressWarnings("unchecked")
    <E extends Extension> Mono<E> doUpdate(E oldExtension, String name, Long version, byte[] data) {
        return Mono.defer(() -> {
            var type = (Class<E>) oldExtension.getClass();
            return client.update(name, version, data)
                .map(updated -> converter.convertFrom(type, updated))
                .flatMap(extension -> Mono.fromRunnable(
                        () -> this.indexEngine.update(List.of(convertToRealExtension(extension))))
                    .subscribeOn(this.scheduler)
                    .thenReturn(extension)
                )
                .as(transactionalOperator::transactional);
        });
    }

    private Extension convertToRealExtension(Extension extension) {
        var gvk = extension.groupVersionKind();
        var realType = schemeManager.get(gvk).type();
        Extension realExtension = extension;
        if (extension instanceof Unstructured) {
            realExtension = Unstructured.OBJECT_MAPPER.convertValue(extension, realType);
        } else if (extension instanceof JsonExtension jsonExtension) {
            realExtension = jsonExtension.getObjectMapper().convertValue(jsonExtension, realType);
        }
        return realExtension;
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
