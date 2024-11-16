package run.halo.app.infra;

import java.net.URI;
import org.springframework.http.HttpRequest;
import reactor.core.publisher.Mono;

/**
 * {@link ExternalLinkProcessor} to process an in-site link to an external link.
 *
 * @author guqing
 * @see ExternalUrlSupplier
 * @since 2.9.0
 */
public interface ExternalLinkProcessor {

    /**
     * If the link is in-site link, then process it to an external link with
     * {@link ExternalUrlSupplier#getRaw()}, otherwise return the original link.
     *
     * @param link link to process
     * @return processed link or original link
     */
    String processLink(String link);

    /**
     * Process the URI to an external URL.
     * <p>
     * If the URI is an in-site link, then process it to an external link with
     * {@link ExternalUrlSupplier#getRaw()} or {@link ExternalUrlSupplier#getURL(HttpRequest)},
     * otherwise return the original URI.
     * </p>
     *
     * @param uri uri to process
     * @return processed URI or original URI
     */
    Mono<URI> processLink(URI uri);

}
