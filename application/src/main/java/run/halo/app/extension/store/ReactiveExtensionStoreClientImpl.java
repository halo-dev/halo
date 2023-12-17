package run.halo.app.extension.store;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional
    public Mono<ExtensionStore> delete(String name, Long version) {
        return repository.findById(name)
            .flatMap(extensionStore -> {
                // reset the version
                extensionStore.setVersion(version);
                return repository.delete(extensionStore).thenReturn(extensionStore);
            });
    }
}
