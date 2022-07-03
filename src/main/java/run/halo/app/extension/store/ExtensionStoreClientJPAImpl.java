package run.halo.app.extension.store;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return repository.findAllByNameStartingWith(prefix);
    }

    @Override
    public Optional<ExtensionStore> fetchByName(String name) {
        return repository.findById(name);
    }

    @Override
    public ExtensionStore create(String name, byte[] data) {
        var store = new ExtensionStore(name, data);
        return repository.save(store);
    }

    @Override
    public ExtensionStore update(String name, Long version, byte[] data) {
        var store = new ExtensionStore(name, data, version);
        return repository.save(store);
    }

    @Override
    @Transactional
    public ExtensionStore delete(String name, Long version) {
        var extensionStore =
            repository.findById(name).orElseThrow(EntityNotFoundException::new);
        extensionStore.setVersion(version);
        repository.delete(extensionStore);
        return extensionStore;
    }

}
