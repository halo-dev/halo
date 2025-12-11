package run.halo.app.core.attachment.thumbnail;

import java.nio.file.Path;
import org.springframework.core.io.Resource;
import reactor.core.publisher.Mono;
import run.halo.app.core.attachment.ThumbnailSize;

/**
 * Service for generating and deleting local image thumbnails.
 *
 * @author johnniang
 * @since 2.22.0
 */
public interface LocalThumbnailService {

    /**
     * Generates thumbnail for the source image. If the thumbnail already exists, it will return the
     * existing one.
     *
     * @param source the source image path
     * @param size the thumbnail size
     * @return the generated thumbnail resource
     */
    Mono<Resource> generate(Path source, ThumbnailSize size);

    /**
     * Deletes thumbnails associated with the source image.
     *
     * @param source the source image path
     */
    void delete(Path source);

}
