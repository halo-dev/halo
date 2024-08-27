package run.halo.app.theme.finders;

import reactor.core.publisher.Mono;

/**
 * A dialect expression for image thumbnail.
 *
 * @author guqing
 * @since 2.19.0
 */
public interface ThumbnailFinder {

    /**
     * Generate thumbnail uri from given image uri and size.
     *
     * @param size the size of thumbnail to generate
     * @return the generated thumbnail url
     */
    Mono<String> gen(String uri, String size);
}
