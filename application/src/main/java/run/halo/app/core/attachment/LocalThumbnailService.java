package run.halo.app.core.attachment;

import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;
import run.halo.app.core.attachment.extension.LocalThumbnail;
import run.halo.app.infra.ExternalLinkProcessor;
import run.halo.app.infra.exception.NotFoundException;

public interface LocalThumbnailService {

    /**
     * Gets original image URI for the given thumbnail URI.
     *
     * @param thumbnailUri The thumbnail URI string
     * @return The original image URI, {@link NotFoundException} will be thrown if the thumbnail
     * record does not exist by the given thumbnail URI
     */
    Mono<URI> getOriginalImageUri(URI thumbnailUri);

    /**
     * <p>Gets thumbnail file resource for the given year, size and filename.</p>
     * {@link Mono#empty()} will be returned if the thumbnail file does not generate yet or the
     * thumbnail record does not exist.
     *
     * @param thumbnailUri The thumbnail URI string
     * @return The thumbnail file resource
     */
    Mono<Resource> getThumbnail(URI thumbnailUri);

    /**
     * <p>Gets thumbnail file resource for the given URI and size.</p>
     * {@link Mono#empty()} will be returned if the thumbnail file does not generate yet.
     *
     * @param originalImageUri original image URI to get thumbnail
     * @param size thumbnail size
     */
    Mono<Resource> getThumbnail(URI originalImageUri, ThumbnailSize size);

    /**
     * Generate thumbnail file for the given thumbnail.
     * Do nothing if the thumbnail file already exists.
     *
     * @param thumbnail The thumbnail to generate.
     * @return The generated thumbnail file resource.
     */
    Mono<Resource> generate(LocalThumbnail thumbnail);

    /**
     * Creates a {@link LocalThumbnail} record for the given image URL and size.
     * The thumbnail file will be generated asynchronously according to the thumbnail record.
     *
     * @param imageUrl original image URL
     * @param size thumbnail size to generate
     * @return The created thumbnail record.
     */
    Mono<LocalThumbnail> create(URL imageUrl, ThumbnailSize size);

    /**
     * Deletes the all size thumbnail files for the given image URI.
     * If the image URI is not absolute, it will be processed by {@link ExternalLinkProcessor}.
     *
     * @param imageUri original image URI to delete thumbnails
     * @return A {@link Mono} indicates the completion of the deletion.
     */
    Mono<Void> delete(URI imageUri);

    /**
     * Ensures the image URI is an url path if it's an in-site image.
     * If it's not an in-site image, it will return directly.
     */
    @NonNull
    URI ensureInSiteUriIsRelative(URI imageUri);

    Path toFilePath(String thumbRelativeUnixPath);

    URI buildThumbnailUri(String year, ThumbnailSize size, String filename);
}
