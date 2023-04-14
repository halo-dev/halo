package run.halo.app.core.extension.attachment.endpoint;

import java.net.URI;
import java.time.Duration;
import org.pf4j.ExtensionPoint;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.attachment.Policy;
import run.halo.app.extension.ConfigMap;

public interface AttachmentHandler extends ExtensionPoint {

    Mono<Attachment> upload(UploadContext context);

    Mono<Attachment> delete(DeleteContext context);

    /**
     * Gets a shared URL which could be accessed publicly.
     * 1. If the attachment is in local storage, the permalink will be returned.
     * 2. If the attachment is in s3 storage, the Presigned URL will be returned.
     * <p>
     * Please note that the default implementation is only for back compatibility.
     *
     * @param attachment contains detail of attachment.
     * @param policy is storage policy.
     * @param configMap contains configuration needed by handler.
     * @param ttl indicates how long the URL is alive.
     * @return shared URL which could be accessed publicly. Might be relative URL.
     */
    default Mono<URI> getSharedURL(Attachment attachment,
        Policy policy,
        ConfigMap configMap,
        Duration ttl) {
        return Mono.empty();
    }

    /**
     * Gets a permalink representing a unique attachment.
     * If the attachment is in local storage, the permalink will be returned.
     * If the attachment is in s3 storage, the Object URL will be returned.
     * <p>
     * Please note that the default implementation is only for back compatibility.
     *
     * @param attachment contains detail of attachment.
     * @param policy is storage policy.
     * @param configMap contains configuration needed by handler.
     * @return permalink representing a unique attachment. Might be relative URL.
     */
    default Mono<URI> getPermalink(Attachment attachment,
        Policy policy,
        ConfigMap configMap) {
        return Mono.empty();
    }

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

}
