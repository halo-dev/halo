package run.halo.app.core.attachment;

import java.net.URI;
import java.net.URL;
import org.springframework.core.io.Resource;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.attachment.LocalThumbnail;
import run.halo.app.infra.ExternalLinkProcessor;

public interface LocalThumbnailService {

    /**
     * Gets thumbnail file resource for the given year, size and filename.
     *
     * @param year directory name to archive the thumbnail to avoid too many files in one
     * directory which may cause file system performance issue
     * @param size thumbnail size
     * @param filename original image filename
     * @return The thumbnail file resource.
     */
    Mono<Resource> getThumbnail(String year, ThumbnailSize size, String filename);

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
}
