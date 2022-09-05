package run.halo.app.core.extension.reconciler.attachment;

import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginManager;
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
import run.halo.app.infra.exception.NotFoundException;

@Slf4j
public class AttachmentReconciler implements Reconciler<Request> {

    private final ExtensionClient client;

    private final PluginManager pluginManager;

    public AttachmentReconciler(ExtensionClient client, PluginManager pluginManager) {
        this.client = client;
        this.pluginManager = pluginManager;
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
                Flux.fromIterable(pluginManager.getExtensions(AttachmentHandler.class))
                    .concatMap(handler -> handler.delete(deleteOption))
                    .next()
                    .switchIfEmpty(Mono.error(() -> new NotFoundException(
                        "No suitable handler found to delete the attachment")))
                    .doOnNext(deleted -> removeFinalizer(deleted.getMetadata().getName()))
                    .block();
                return;
            }
            var annotations = attachment.getMetadata().getAnnotations();
            if (annotations != null) {
                String permalink = null;
                var localRelativePath = annotations.get(Constant.LOCAL_REL_PATH_ANNO_KEY);
                if (localRelativePath != null) {
                    // TODO Add router function here.
                    permalink = "http://localhost:8090/upload/" + localRelativePath;
                    permalink = UriUtils.encodePath(permalink, StandardCharsets.UTF_8);
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
                    }
                    status.setPermalink(permalink);

                    // update status
                    attachment.setStatus(status);
                    client.update(attachment);
                }
            }
        });

        return null;
    }

    void removeFinalizer(String attachmentName) {
        client.fetch(Attachment.class, attachmentName)
            .ifPresent(attachment -> {
                var finalizers = attachment.getMetadata().getFinalizers();
                if (finalizers != null && finalizers.remove(Constant.FINALIZER_NAME)) {
                    // update it
                    client.update(attachment);
                }
            });
    }

}
