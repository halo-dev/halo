package run.halo.app.core.extension.service;

import java.net.URI;
import java.time.Duration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.attachment.Attachment;

/**
 * AttachmentService.
 *
 * @author johnniang
 * @since 2.5.0
 */
public interface AttachmentService {

    /**
     * Uploads the given attachment to specific storage using handlers in plugins. Please note
     * that we will make sure the request is authenticated, or an unauthorized exception throws.
     * <p>
     * If no handler can be found to upload the given attachment, ServerError exception will be
     * thrown.
     *
     * @param policyName is attachment policy name.
     * @param groupName is group name the attachment belongs.
     * @param filename is filename of the attachment.
     * @param content is binary data of the attachment.
     * @param mediaType is media type of the attachment.
     * @return attachment.
     */
    Mono<Attachment> upload(@NonNull String policyName,
        @Nullable String groupName,
        @NonNull String filename,
        @NonNull Flux<DataBuffer> content,
        @Nullable MediaType mediaType);

    /**
     * Deletes an attachment using handlers in plugins.
     * <p>
     * If no handler can be found to delete the given attachment, Mono.empty() will return.
     *
     * @param attachment is to be deleted.
     * @return deleted attachment.
     */
    Mono<Attachment> delete(Attachment attachment);

    /**
     * Gets permalink using handlers in plugins.
     * <p>
     * If no handler can be found to delete the given attachment, Mono.empty() will return.
     *
     * @param attachment is created attachment.
     * @return permalink
     */
    Mono<URI> getPermalink(Attachment attachment);

    /**
     * Gets shared URL using handlers in plugins.
     * <p>
     * If no handler can be found to delete the given attachment, Mono.empty() will return.
     *
     * @param attachment is created attachment.
     * @param ttl is time to live of the shared URL.
     * @return time-to-live shared URL. Please note that, if the attachment is stored in local, the
     * shared URL is equal to permalink.
     */
    Mono<URI> getSharedURL(Attachment attachment, Duration ttl);

}
