package run.halo.app.extension.store;

import java.time.Duration;
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

    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private final ReactiveExtensionStoreClient storeClient;

    public ExtensionStoreClientJPAImpl(ReactiveExtensionStoreClient storeClient) {
        this.storeClient = storeClient;
    }

    @Override
    public List<ExtensionStore> listByNamePrefix(String prefix) {
        return storeClient.listByNamePrefix(prefix).collectList().block(TIMEOUT);
    }

    @Override
    public Page<ExtensionStore> listByNamePrefix(String prefix, Pageable pageable) {
        return storeClient.listByNamePrefix(prefix, pageable).block(TIMEOUT);
    }

    @Override
    public List<ExtensionStore> listBy(String prefix, String nameCursor, int limit) {
        return storeClient.listBy(prefix, nameCursor, limit).collectList().block(TIMEOUT);
    }

    @Override
    public List<ExtensionStore> listByNames(List<String> names) {
        return storeClient.listByNames(names).collectList().block(TIMEOUT);
    }

    @Override
    public Optional<ExtensionStore> fetchByName(String name) {
        return storeClient.fetchByName(name).blockOptional(TIMEOUT);
    }

    @Override
    public ExtensionStore create(String name, byte[] data) {
        return storeClient.create(name, data).block(TIMEOUT);
    }

    @Override
    public ExtensionStore update(String name, Long version, byte[] data) {
        return storeClient.update(name, version, data).block(TIMEOUT);
    }

    @Override
    public ExtensionStore delete(String name, Long version) {
        return storeClient.delete(name, version).block(TIMEOUT);
    }
}
