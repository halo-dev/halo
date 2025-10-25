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
    public List<Extensions> listByNamePrefix(String prefix) {
        return storeClient.listByNamePrefix(prefix).collectList().block(TIMEOUT);
    }

    @Override
    public Page<Extensions> listByNamePrefix(String prefix, Pageable pageable) {
        return storeClient.listByNamePrefix(prefix, pageable).block(TIMEOUT);
    }

    @Override
    public List<Extensions> listBy(String prefix, String nameCursor, int limit) {
        return storeClient.listBy(prefix, nameCursor, limit).collectList().block(TIMEOUT);
    }

    @Override
    public List<Extensions> listByNames(List<String> names) {
        return storeClient.listByNames(names).collectList().block(TIMEOUT);
    }

    @Override
    public Optional<Extensions> fetchByName(String name) {
        return storeClient.fetchByName(name).blockOptional(TIMEOUT);
    }

    @Override
    public Extensions create(String name, byte[] data) {
        return storeClient.create(name, data).block(TIMEOUT);
    }

    @Override
    public Extensions update(String name, Long version, byte[] data) {
        return storeClient.update(name, version, data).block(TIMEOUT);
    }

    @Override
    public Extensions delete(String name, Long version) {
        return storeClient.delete(name, version).block(TIMEOUT);
    }
}
