package run.halo.app.extension.store;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveExtensionStoreClient {

    Flux<ExtensionStore> listByNamePrefix(String prefix);

    Mono<ExtensionStore> fetchByName(String name);

    Mono<ExtensionStore> create(String name, byte[] data);

    Mono<ExtensionStore> update(String name, Long version, byte[] data);

    Mono<ExtensionStore> delete(String name, Long version);

}
