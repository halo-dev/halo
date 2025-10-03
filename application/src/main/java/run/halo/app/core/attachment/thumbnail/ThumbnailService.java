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

}
