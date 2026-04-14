package run.halo.app.extension.store;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.support.ReactivePageableExecutionUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.infra.exception.DuplicateNameException;

@Component
@RequiredArgsConstructor
public class ReactiveExtensionStoreClientImpl implements ReactiveExtensionStoreClient {

    private static final int DEFAULT_FETCH_SIZE = 100;

    private final ExtensionStoreRepository repository;

    private final R2dbcEntityOperations entityOperations;

    private int fetchSize = DEFAULT_FETCH_SIZE;

    void setFetchSize(int fetchSize) {
        Assert.isTrue(fetchSize >= 0, "fetchSize must be greater than or equal to 0");
        this.fetchSize = fetchSize;
    }

    @Override
    public Flux<ExtensionStore> listByNamePrefix(String prefix) {
        Assert.hasText(prefix, "Prefix must not be blank");

        prefix = Strings.CS.appendIfMissing(prefix, "/");
        return entityOperations.select(ExtensionStore.class)
            .withFetchSize(fetchSize)
            .matching(Query.query(
                        Criteria.where("name").like(prefix + "%")
                    )
                    .sort(Sort.by(Sort.Direction.ASC, "name"))
            )
            .all();
    }

    @Override
    public Mono<Page<ExtensionStore>> listByNamePrefix(String prefix, Pageable pageable) {
        Assert.hasText(prefix, "Prefix must not be blank");

        var q = Query.query(
            Criteria.where("name").like(prefix + "%")
        ).sort(Sort.by(Sort.Direction.ASC, "name"));
        var getItems = entityOperations.select(ExtensionStore.class)
            .matching(q.with(pageable))
            .all()
            .collectList();
        var getCount = entityOperations.select(ExtensionStore.class)
            .matching(q)
            .count();
        return getItems.flatMap(
            items -> ReactivePageableExecutionUtils.getPage(items, pageable, getCount)
        );
    }

    @Override
    public Flux<ExtensionStore> listBy(String prefix, @Nullable String nameCursor, int limit) {
        Assert.hasText(prefix, "Prefix must not be blank");
        Assert.isTrue(limit > 0, "Limit must be greater than 0");

        prefix = Strings.CS.appendIfMissing(prefix, "/");
        var criteria = Criteria.where("name").like(prefix + "%");
        if (StringUtils.isNotBlank(nameCursor)) {
            nameCursor = Strings.CS.prependIfMissing(nameCursor, prefix);
            criteria = criteria.and(Criteria.where("name").greaterThan(nameCursor));
        }
        var q = Query.query(criteria).sort(Sort.by(Sort.Direction.ASC, "name"));
        return entityOperations.select(ExtensionStore.class)
            .matching(q.limit(limit))
            .all();
    }

    @Override
    public Mono<Long> countByNamePrefix(String prefix) {
        Assert.hasText(prefix, "Prefix must not be blank");

        var q = Query.query(
                Criteria.where("name").like(prefix + "%")
            )
            .sort(Sort.by(Sort.Direction.ASC, "name"));
        return entityOperations.select(ExtensionStore.class)
            .matching(q)
            .count();
    }

    @Override
    public Flux<ExtensionStore> listByNames(List<String> names) {
        if (CollectionUtils.isEmpty(names)) {
            return Flux.empty();
        }
        // Keep the order of names efficiently
        var orderMap = IntStream.range(0, names.size())
            .boxed()
            .collect(Collectors.toMap(names::get, Function.identity(), (a, b) -> a));
        return repository.findByNameIn(names)
            .sort(Comparator.comparingInt(es -> orderMap.get(es.getName())));
    }

    @Override
    public Mono<ExtensionStore> fetchByName(String name) {
        return repository.findById(name);
    }

    @Override
    public Mono<ExtensionStore> create(String name, byte[] data) {
        return repository.save(new ExtensionStore(name, data))
            .onErrorMap(DuplicateKeyException.class,
                t -> new DuplicateNameException("Duplicate name detected.", t));
    }

    @Override
    public Mono<ExtensionStore> update(String name, Long version, byte[] data) {
        return repository.save(new ExtensionStore(name, data, version));
    }

    @Override
    public Mono<ExtensionStore> delete(String name, Long version) {
        return repository.findById(name)
            .flatMap(extensionStore -> {
                // reset the version
                extensionStore.setVersion(version);
                return repository.delete(extensionStore).thenReturn(extensionStore);
            });
    }
}
