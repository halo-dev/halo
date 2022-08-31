package run.halo.app.core.extension.attachment.endpoint;

import org.pf4j.ExtensionPoint;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.attachment.Policy;
import run.halo.app.extension.ConfigMap;

public interface AttachmentHandler extends ExtensionPoint {

    Mono<Attachment> upload(UploadOption uploadOption);

    // TODO
    default Mono<Void> delete(DeleteOption deleteOption) {
        return Mono.empty();
    }

    // TODO Add delete method here
    // Mono<Attachment> delete();

    record UploadOption(FilePart file,
                        Policy policy,
                        String username,
                        ConfigMap configMap) {
    }

    record DeleteOption() {
    }
}
