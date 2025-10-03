package run.halo.app.core.attachment.thumbnail;

import java.nio.file.Path;
import java.util.Optional;
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
     * Generates thumbnail for the source image.
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

    /**
     * Resolves the thumbnail path for the given source image and thumbnail size. Mainly for
     * preflight check.
     *
     * @param source the source image path
     * @param size the thumbnail size
     * @return the resolved thumbnail path
     */
    Optional<Path> resolveThumbnailPath(Path source, ThumbnailSize size);

}
