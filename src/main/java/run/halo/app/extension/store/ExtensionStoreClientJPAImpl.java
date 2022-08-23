package run.halo.app.extension.store;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * An implementation of ExtensionStoreClient using JPA.
 *
 * @author johnniang
 */
@Service
public class ExtensionStoreClientJPAImpl implements ExtensionStoreClient {

    private final ExtensionStoreRepository repository;

    public ExtensionStoreClientJPAImpl(ExtensionStoreRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ExtensionStore> listByNamePrefix(String prefix) {
        return repository.findAllByNameStartingWith(prefix).collectList().block();
    }

    @Override
    public Optional<ExtensionStore> fetchByName(String name) {
        return repository.findById(name).blockOptional();
    }

    @Override
    public ExtensionStore create(String name, byte[] data) {
        var store = new ExtensionStore(name, data);
        return repository.save(store).block();
    }

    @Override
    public ExtensionStore update(String name, Long version, byte[] data) {
        var store = new ExtensionStore(name, data, version);
        return repository.save(store).block();
    }

    @Override
    public ExtensionStore delete(String name, Long version) {
        return repository.findById(name)
            .switchIfEmpty(Mono.error(() -> new EntityNotFoundException(
                "Extension store with name " + name + " was not found.")))
            .flatMap(deleting -> {
                deleting.setVersion(version);
                return repository.delete(deleting).thenReturn(deleting);
            })
            .block();
    }

}
