package run.halo.app.core.extension.attachment.endpoint;

import org.pf4j.ExtensionPoint;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.attachment.Policy;

public interface AttachmentUploadHandler extends ExtensionPoint {

    Mono<Attachment> upload(UploadOption uploadRequest);

    // TODO Add delete method here
    // Mono<Attachment> delete();

    record UploadOption(FilePart file,
                        Policy policy,
                        String username) {
    }
}
