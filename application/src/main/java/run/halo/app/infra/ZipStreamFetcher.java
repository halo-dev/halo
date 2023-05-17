package run.halo.app.infra;

import java.io.InputStream;
import java.net.URI;
import reactor.core.publisher.Mono;

/**
 * <p>Zip stream fetcher.</p>
 *
 * @author guqing
 * @since 2.6.0
 */
@FunctionalInterface
public interface ZipStreamFetcher {

    /**
     * <p>Fetch zip input stream from uri.</p>
     * Note: the caller should close the zip input stream.
     *
     * @param uri uri to fetch
     * @return zip input stream, null if not found
     */
    Mono<InputStream> fetch(URI uri);
}
