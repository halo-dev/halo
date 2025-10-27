package run.halo.app.extension.store;

import java.util.Comparator;
import java.util.List;
import java.util.function.ToIntFunction;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.infra.exception.DuplicateNameException;

@Component
public class ReactiveExtensionStoreClientImpl implements ReactiveExtensionStoreClient {

    private final ExtensionStoreRepository repository;

    public ReactiveExtensionStoreClientImpl(ExtensionStoreRepository repository) {
        this.repository = repository;
    }

    @Override
    public Flux<Extensions> listByNamePrefix(String prefix) {
        return repository.findAllByNameStartingWith(prefix);
    }

    @Override
    public Mono<Page<Extensions>> listByNamePrefix(String prefix, Pageable pageable) {
        return this.repository.findAllByNameStartingWith(prefix, pageable)
            .collectList()
            .zipWith(this.repository.countByNameStartingWith(prefix))
            .map(p -> new PageImpl<>(p.getT1(), pageable, p.getT2()));
    }

    @Override
    public Flux<Extensions> listBy(String prefix, String nameCursor, int limit) {
        var page = PageRequest.ofSize(limit).withSort(Sort.Direction.ASC, "name");
        if (StringUtils.isBlank(nameCursor)) {
            return this.repository.findAllByNameStartingWith(prefix, page);
        }
        var cursor = StringUtils.prependIfMissing(nameCursor, prefix);
        return this.repository.findAllByNameStartingWithAndNameGreaterThan(
            prefix, cursor, page
        );
    }

    @Override
    public Mono<Long> countByNamePrefix(String prefix) {
        return this.repository.countByNameStartingWith(prefix);
    }

    @Override
    public Flux<Extensions> listByNames(List<String> names) {
        ToIntFunction<Extensions> comparator =
            store -> names.indexOf(store.getName());
        return repository.findByNameIn(names)
            .sort(Comparator.comparingInt(comparator));
    }

    @Override
    public Mono<Extensions> fetchByName(String name) {
        return repository.findById(name);
    }

    @Override
    public Mono<Extensions> create(String name, byte[] data) {
        return repository.save(new Extensions(name, data))
            .onErrorMap(DuplicateKeyException.class,
                t -> new DuplicateNameException("Duplicate name detected.", t));
    }

    @Override
    public Mono<Extensions> update(String name, Long version, byte[] data) {
        return repository.save(new Extensions(name, data, version));
    }

    @Override
    public Mono<Extensions> delete(String name, Long version) {
        return repository.findById(name)
            .flatMap(extensionStore -> {
                // reset the version
                extensionStore.setVersion(version);
                return repository.delete(extensionStore).thenReturn(extensionStore);
            });
    }
}
