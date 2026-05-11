package run.halo.app.infra;

import java.net.URI;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * {@link DataBuffer} stream fetcher from uri.
 *
 * @author guqing
 * @since 2.6.0
 */
public interface ReactiveUrlDataBufferFetcher {

    /**
     * Fetch data buffer flux from uri.
     *
     * @param uri uri to fetch
     * @return data buffer flux
     */
    Flux<DataBuffer> fetch(URI uri);

    /**
     * Get head of the uri.
     *
     * @param uri uri to fetch
     * @return response entity
     */
    Mono<HttpHeaders> head(URI uri);

    /**
     * Gets the response entity of the uri, which contains the status code, headers and body.
     *
     * @param uri uri to fetch
     * @return response entity
     */
    Mono<ResponseEntity<Flux<DataBuffer>>> fetchResponseEntity(URI uri);
}
