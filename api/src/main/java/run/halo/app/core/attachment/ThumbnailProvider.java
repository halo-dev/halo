package run.halo.app.core.attachment;

import java.net.URI;
import java.net.URL;
import lombok.Builder;
import lombok.Data;
import org.pf4j.ExtensionPoint;
import reactor.core.publisher.Mono;

public interface ThumbnailProvider extends ExtensionPoint {

    /**
     * Generate thumbnail URI for given image URL and size.
     *
     * @param context Thumbnail context including image URI and size
     * @return Generated thumbnail URI
     */
    Mono<URI> generate(ThumbnailContext context);

    /**
     * Delete thumbnail file for given image URL.
     *
     * @param imageUrl original image URL
     */
    Mono<Void> delete(URL imageUrl);

    /**
     * Whether the provider supports the given image URI.
     *
     * @return {@code true} if supports, {@code false} otherwise
     */
    Mono<Boolean> supports(ThumbnailContext context);

    @Data
    @Builder
    class ThumbnailContext {
        private final URL imageUrl;
        private final ThumbnailSize size;
    }
}
