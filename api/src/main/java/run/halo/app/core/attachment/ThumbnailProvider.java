package run.halo.app.core.attachment;

import java.net.URI;
import java.net.URL;
import lombok.Builder;
import lombok.Data;
import org.pf4j.ExtensionPoint;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.attachment.endpoint.AttachmentHandler;

/**
 * Thumbnail provider extension.
 *
 * @since 2.22.0
 * @deprecated Use {@link AttachmentHandler} instead. We are planing to remove this extension
 * point in future release.
 */
@Deprecated(forRemoval = true, since = "2.22.0")
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
