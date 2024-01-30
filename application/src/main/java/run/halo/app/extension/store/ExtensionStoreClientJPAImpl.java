package run.halo.app.extension.store;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * An implementation of ExtensionStoreClient using JPA.
 *
 * @author johnniang
 */
@Service
public class ExtensionStoreClientJPAImpl implements ExtensionStoreClient {

    private final ReactiveExtensionStoreClient storeClient;

    public ExtensionStoreClientJPAImpl(ReactiveExtensionStoreClient storeClient) {
        this.storeClient = storeClient;
    }

    @Override
    public List<ExtensionStore> listByNamePrefix(String prefix) {
        return storeClient.listByNamePrefix(prefix).collectList().block();
    }

    @Override
    public Page<ExtensionStore> listByNamePrefix(String prefix, Pageable pageable) {
        return storeClient.listByNamePrefix(prefix, pageable).block();
    }

    @Override
    public List<ExtensionStore> listByNames(List<String> names) {
        return storeClient.listByNames(names).collectList().block();
    }

    @Override
    public Optional<ExtensionStore> fetchByName(String name) {
        return storeClient.fetchByName(name).blockOptional();
    }

    @Override
    public ExtensionStore create(String name, byte[] data) {
        return storeClient.create(name, data).block();
    }

    @Override
    public ExtensionStore update(String name, Long version, byte[] data) {
        return storeClient.update(name, version, data).block();
    }

    @Override
    public ExtensionStore delete(String name, Long version) {
        return storeClient.delete(name, version).block();
    }
}
