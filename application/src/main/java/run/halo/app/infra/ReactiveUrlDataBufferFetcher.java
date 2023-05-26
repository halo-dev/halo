package run.halo.app.infra;

import java.net.URI;
import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Flux;

/**
 * <p>{@link DataBuffer} stream fetcher from uri.</p>
 *
 * @author guqing
 * @since 2.6.0
 */
@FunctionalInterface
public interface ReactiveUrlDataBufferFetcher {

    /**
     * <p>Fetch data buffer flux from uri.</p>
     *
     * @param uri uri to fetch
     * @return data buffer flux
     */
    Flux<DataBuffer> fetch(URI uri);
}
