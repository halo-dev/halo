package run.halo.app.core.attachment.thumbnail;

import java.net.URI;
import java.util.Map;
import reactor.core.publisher.Mono;
import run.halo.app.core.attachment.ThumbnailSize;

/**
 * Service for managing thumbnails.
 *
 * @author johnniang
 * @since 2.22.0
 */
public interface ThumbnailService {

    /**
     * Get the thumbnail link for the given image URI and size.
     *
     * @param permalink the permalink of the image
     * @param size the size of the thumbnail
     * @return the thumbnail link
     */
    Mono<URI> get(URI permalink, ThumbnailSize size);

    /**
     * Get all thumbnail links for the given image URI.
     *
     * @param permalink the permalink of the image
     * @return the map of thumbnail size to thumbnail link
     */
    Mono<Map<ThumbnailSize, URI>> get(URI permalink);

    /**
     * Resolve a URI that is safe to put into an HTTP {@code Location} header.
     *
     * <p>Relative references and same-site http(s) absolute URLs may fall back to the input URI when no thumbnail is
     * available. Other absolute or scheme-relative URLs may only resolve to an absolute http(s) URI already recorded on
     * an Attachment ({@code status.thumbnails} or {@code status.permalink}); unknown, non-http or relative external
     * URIs yield empty so callers can reject the request instead of echoing the input.
     *
     * @param permalink the image URI from the request
     * @param size the thumbnail size
     * @return a trusted redirect target, or empty if the URI must not be redirected to
     */
    Mono<URI> getSafeRedirectUri(URI permalink, ThumbnailSize size);
}
