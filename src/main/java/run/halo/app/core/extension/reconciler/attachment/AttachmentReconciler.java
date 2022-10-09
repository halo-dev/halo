package run.halo.app.core.extension.reconciler.attachment;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.UriUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.attachment.Attachment.AttachmentStatus;
import run.halo.app.core.extension.attachment.Constant;
import run.halo.app.core.extension.attachment.Policy;
import run.halo.app.core.extension.attachment.endpoint.AttachmentHandler;
import run.halo.app.core.extension.attachment.endpoint.AttachmentHandler.DeleteOption;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.Reconciler.Request;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.infra.exception.NotFoundException;
import run.halo.app.plugin.ExtensionComponentsFinder;

@Slf4j
public class AttachmentReconciler implements Reconciler<Request> {

    private final ExtensionClient client;

    private final ExtensionComponentsFinder extensionComponentsFinder;

    private final ExternalUrlSupplier externalUrl;

    public AttachmentReconciler(ExtensionClient client,
        ExtensionComponentsFinder extensionComponentsFinder,
        ExternalUrlSupplier externalUrl) {
        this.client = client;
        this.extensionComponentsFinder = extensionComponentsFinder;
        this.externalUrl = externalUrl;
    }

    @Override
    public Result reconcile(Request request) {
        client.fetch(Attachment.class, request.name()).ifPresent(attachment -> {
            // TODO Handle the finalizer
            if (attachment.getMetadata().getDeletionTimestamp() != null) {
                Policy policy =
                    client.fetch(Policy.class, attachment.getSpec().getPolicyRef().getName())
                        .orElseThrow();
                var configMap =
                    client.fetch(ConfigMap.class, policy.getSpec().getConfigMapRef().getName())
                        .orElseThrow();
                var deleteOption = new DeleteOption(attachment, policy, configMap);
                Flux.fromIterable(extensionComponentsFinder.getExtensions(AttachmentHandler.class))
                    .concatMap(handler -> handler.delete(deleteOption)).next().switchIfEmpty(
                        Mono.error(() -> new NotFoundException(
                            "No suitable handler found to delete the attachment")))
                    .doOnNext(deleted -> removeFinalizer(deleted.getMetadata().getName())).block();
                return;
            }
            // add finalizer
            addFinalizerIfNotSet(request.name(), attachment.getMetadata().getFinalizers());

            var annotations = attachment.getMetadata().getAnnotations();
            if (annotations != null) {
                String permalink = null;
                var localRelativePath = annotations.get(Constant.LOCAL_REL_PATH_ANNO_KEY);
                if (localRelativePath != null) {
                    // TODO Add router function here.
                    var encodedPath = UriUtils.encodePath("/upload/" + localRelativePath, UTF_8);
                    permalink = externalUrl.get().resolve(encodedPath).normalize().toString();
                } else {
                    var externalLink = annotations.get(Constant.EXTERNAL_LINK_ANNO_KEY);
                    if (externalLink != null) {
                        // TODO Set the external link into status
                        permalink = externalLink;
                    }
                }
                if (permalink != null) {
                    log.debug("Set permalink {} for attachment {}", permalink, request.name());
                    var status = attachment.getStatus();
                    if (status == null) {
                        status = new AttachmentStatus();
                        attachment.setStatus(status);
                    }
                    status.setPermalink(permalink);
                }
            }
            updateStatus(request.name(), attachment.getStatus());
        });
        return null;
    }

    void updateStatus(String attachmentName, AttachmentStatus status) {
        client.fetch(Attachment.class, attachmentName)
            .filter(attachment -> !Objects.deepEquals(attachment.getStatus(), status))
            .ifPresent(attachment -> {
                attachment.setStatus(status);
                client.update(attachment);
            });
    }

    void removeFinalizer(String attachmentName) {
        client.fetch(Attachment.class, attachmentName).ifPresent(attachment -> {
            var finalizers = attachment.getMetadata().getFinalizers();
            if (finalizers != null && finalizers.remove(Constant.FINALIZER_NAME)) {
                // update it
                client.update(attachment);
            }
        });
    }

    void addFinalizerIfNotSet(String attachmentName, Set<String> existingFinalizers) {
        if (existingFinalizers != null && existingFinalizers.contains(Constant.FINALIZER_NAME)) {
            return;
        }

        client.fetch(Attachment.class, attachmentName).ifPresent(attachment -> {
            var finalizers = attachment.getMetadata().getFinalizers();
            if (finalizers == null) {
                finalizers = new HashSet<>();
                attachment.getMetadata().setFinalizers(finalizers);
            }
            finalizers.add(Constant.FINALIZER_NAME);
            client.update(attachment);
        });
    }

}
