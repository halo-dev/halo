package run.halo.app.extension.store;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * This repository contains some basic operations on ExtensionStore entity.
 *
 * @author johnniang
 */
@Repository
public interface ExtensionStoreRepository extends R2dbcRepository<ExtensionStore, String> {

    /**
     * Finds all ExtensionStore by name prefix.
     *
     * @param prefix is the prefix of name.
     * @return all ExtensionStores which names starts with the given prefix.
     */
    Flux<ExtensionStore> findAllByNameStartingWith(String prefix);

}
