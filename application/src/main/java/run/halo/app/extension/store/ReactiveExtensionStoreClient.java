package run.halo.app.extension.store;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveExtensionStoreClient {

    Flux<ExtensionStore> listByNamePrefix(String prefix);

    Mono<Page<ExtensionStore>> listByNamePrefix(String prefix, Pageable pageable);

    Mono<Long> countByNamePrefix(String prefix);

    /**
     * List stores by names and return data according to given names order.
     *
     * @param names store names to list
     * @return a flux of extension stores
     */
    Flux<ExtensionStore> listByNames(List<String> names);

    Mono<ExtensionStore> fetchByName(String name);

    Mono<ExtensionStore> create(String name, byte[] data);

    Mono<ExtensionStore> update(String name, Long version, byte[] data);

    Mono<ExtensionStore> delete(String name, Long version);

}
