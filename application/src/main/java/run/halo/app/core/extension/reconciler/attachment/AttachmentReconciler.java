package run.halo.app.core.extension.reconciler.attachment;

import java.net.URI;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.attachment.Attachment.AttachmentStatus;
import run.halo.app.core.extension.attachment.Constant;
import run.halo.app.core.extension.service.AttachmentService;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.Reconciler.Request;
import run.halo.app.infra.ExternalUrlSupplier;

@Slf4j
@Component
public class AttachmentReconciler implements Reconciler<Request> {

    private final ExtensionClient client;

    private final ExternalUrlSupplier externalUrl;

    private final AttachmentService attachmentService;

    public AttachmentReconciler(ExtensionClient client,
        ExternalUrlSupplier externalUrl, AttachmentService attachmentService) {
        this.client = client;
        this.externalUrl = externalUrl;
        this.attachmentService = attachmentService;
    }

    @Override
    public Result reconcile(Request request) {
        client.fetch(Attachment.class, request.name()).ifPresent(attachment -> {
            // TODO Handle the finalizer
            if (attachment.getMetadata().getDeletionTimestamp() != null) {
                attachmentService.delete(attachment)
                    .doOnNext(deletedAttachment -> {
                        removeFinalizer(attachment.getMetadata().getName());
                    })
                    .blockOptional();
                return;
            }
            // add finalizer
            addFinalizerIfNotSet(request.name(), attachment.getMetadata().getFinalizers());
            var annotations = attachment.getMetadata().getAnnotations();
            if (annotations != null) {
                attachmentService.getPermalink(attachment)
                    .map(URI::toString)
                    .switchIfEmpty(Mono.fromSupplier(() -> {
                        // Only for back-compatibility
                        return annotations.get(Constant.EXTERNAL_LINK_ANNO_KEY);
                    }))
                    .doOnNext(permalink -> {
                        log.debug("Set permalink {} for attachment {}", permalink, request.name());
                        var status = attachment.getStatus();
                        if (status == null) {
                            status = new AttachmentStatus();
                            attachment.setStatus(status);
                        }
                        status.setPermalink(permalink);
                    })
                    .blockOptional();
            }
            updateStatus(request.name(), attachment.getStatus());
        });
        return null;
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new Attachment())
            .build();
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
