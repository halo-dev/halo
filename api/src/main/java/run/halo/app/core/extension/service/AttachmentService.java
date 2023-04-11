package run.halo.app.core.extension.service;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.attachment.Attachment;

/**
 * AttachmentService
 *
 * @author johnniang
 * @since 2.5.0
 */
public interface AttachmentService {

    /**
     * Upload binary data as attachment. Please note that we will make sure the request is
     * authenticated, or an unauthorized exception throws.
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

}
