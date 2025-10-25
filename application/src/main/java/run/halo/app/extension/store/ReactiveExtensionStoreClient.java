package run.halo.app.extension.store;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveExtensionStoreClient {

    Flux<Extensions> listByNamePrefix(String prefix);

    Mono<Page<Extensions>> listByNamePrefix(String prefix, Pageable pageable);

    /**
     * List stores by name prefix, after the given cursor name, and limit the result size.
     *
     * @param prefix the name prefix
     * @param nameCursor cursor name, exclusive and can be null
     * @param limit the max result size
     * @return a flux of extension stores
     */
    Flux<Extensions> listBy(String prefix, @Nullable String nameCursor, int limit);

    Mono<Long> countByNamePrefix(String prefix);

    /**
     * List stores by names and return data according to given names order.
     *
     * @param names store names to list
     * @return a flux of extension stores
     */
    Flux<Extensions> listByNames(List<String> names);

    Mono<Extensions> fetchByName(String name);

    Mono<Extensions> create(String name, byte[] data);

    Mono<Extensions> update(String name, Long version, byte[] data);

    Mono<Extensions> delete(String name, Long version);

}
