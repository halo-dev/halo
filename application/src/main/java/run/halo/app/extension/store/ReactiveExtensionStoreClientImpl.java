package run.halo.app.extension.store;

import java.util.Comparator;
import java.util.List;
import java.util.function.ToIntFunction;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
    public Flux<ExtensionStore> listByNamePrefix(String prefix) {
        return repository.findAllByNameStartingWith(prefix);
    }

    @Override
    public Mono<Page<ExtensionStore>> listByNamePrefix(String prefix, Pageable pageable) {
        return this.repository.findAllByNameStartingWith(prefix, pageable)
            .collectList()
            .zipWith(this.repository.countByNameStartingWith(prefix))
            .map(p -> new PageImpl<>(p.getT1(), pageable, p.getT2()));
    }

    @Override
    public Mono<Long> countByNamePrefix(String prefix) {
        return this.repository.countByNameStartingWith(prefix);
    }

    @Override
    public Flux<ExtensionStore> listByNames(List<String> names) {
        ToIntFunction<ExtensionStore> comparator =
            store -> names.indexOf(store.getName());
        return repository.findByNameIn(names)
            .sort(Comparator.comparingInt(comparator));
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
