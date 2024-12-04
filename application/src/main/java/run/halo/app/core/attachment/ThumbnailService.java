package run.halo.app.core.attachment;

import java.net.URI;
import reactor.core.publisher.Mono;
import run.halo.app.infra.ExternalLinkProcessor;

public interface ThumbnailService {

    /**
     * Generate thumbnail by the given image uri and size.
     * <p>if the imageUri is not absolute, it will be processed by {@link ExternalLinkProcessor}
     * .</p>
     * <p>if externalUrl is not configured, it will return empty.</p>
     *
     * @param imageUri image uri to generate thumbnail
     * @param size thumbnail size to generate
     * @return generated thumbnail uri if success, otherwise empty.
     */
    Mono<URI> generate(URI imageUri, ThumbnailSize size);

    /**
     * <p>Get thumbnail by the given image uri and size.</p>
     * <p>It depends on the {@link #generate(URI, ThumbnailSize)} method, currently the thumbnail
     * generation is limited to the attachment service, that is, the thumbnail is strongly
     * associated with the attachment.</p>
     *
     * @return if thumbnail exists, return the thumbnail uri, otherwise return the original image
     * uri
     */
    Mono<URI> get(URI imageUri, ThumbnailSize size);

    Mono<Void> delete(URI imageUri);
}
