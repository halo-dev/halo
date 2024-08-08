package run.halo.app.infra;

import java.net.URI;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * <p>{@link DataBuffer} stream fetcher from uri.</p>
 *
 * @author guqing
 * @since 2.6.0
 */
public interface ReactiveUrlDataBufferFetcher {

    /**
     * <p>Fetch data buffer flux from uri.</p>
     *
     * @param uri uri to fetch
     * @return data buffer flux
     */
    Flux<DataBuffer> fetch(URI uri);

    /**
     * <p>Get head of the uri.</p>
     *
     * @param uri uri to fetch
     * @return response entity
     */
    Mono<ResponseEntity<Void>> head(URI uri);
}
