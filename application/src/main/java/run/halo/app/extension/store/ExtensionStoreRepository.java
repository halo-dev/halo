package run.halo.app.extension.store;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    Flux<ExtensionStore> findAllByNameStartingWith(String prefix, Pageable pageable);

    Mono<Long> countByNameStartingWith(String prefix);

    /**
     * <p>Finds all ExtensionStore by name in, the result no guarantee the same order as the given
     * names, so if you want this, please order the result by yourself.</p>
     *
     * @param names names to find
     * @return a flux of extension stores
     */
    Flux<ExtensionStore> findByNameIn(List<String> names);
}
