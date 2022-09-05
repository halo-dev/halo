package run.halo.app.core.extension.attachment.endpoint;

import org.pf4j.ExtensionPoint;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.attachment.Policy;
import run.halo.app.extension.ConfigMap;

public interface AttachmentHandler extends ExtensionPoint {

    Mono<Attachment> upload(UploadContext context);

    Mono<Attachment> delete(DeleteContext context);

    interface UploadContext {

        FilePart file();

        Policy policy();

        ConfigMap configMap();

    }

    interface DeleteContext {
        Attachment attachment();

        Policy policy();

        ConfigMap configMap();
    }

    record UploadOption(FilePart file,
                        Policy policy,
                        ConfigMap configMap) implements UploadContext {
    }

    record DeleteOption(Attachment attachment, Policy policy, ConfigMap configMap)
        implements DeleteContext {
    }
}
