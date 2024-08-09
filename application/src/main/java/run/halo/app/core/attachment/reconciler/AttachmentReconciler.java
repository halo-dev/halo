package run.halo.app.core.attachment.reconciler;

import static run.halo.app.extension.ExtensionUtil.addFinalizers;
import static run.halo.app.extension.ExtensionUtil.removeFinalizers;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.attachment.AttachmentUtils;
import run.halo.app.core.attachment.ThumbnailService;
import run.halo.app.core.attachment.ThumbnailSize;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.attachment.Attachment.AttachmentStatus;
import run.halo.app.core.extension.attachment.Constant;
import run.halo.app.core.extension.service.AttachmentService;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.Reconciler.Request;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttachmentReconciler implements Reconciler<Request> {

    private final ExtensionClient client;

    private final AttachmentService attachmentService;

    private final ThumbnailService thumbnailService;

    @Override
    public Result reconcile(Request request) {
        client.fetch(Attachment.class, request.name()).ifPresent(attachment -> {
            if (ExtensionUtil.isDeleted(attachment)) {
                if (removeFinalizers(attachment.getMetadata(), Set.of(Constant.FINALIZER_NAME))) {
                    cleanUpResources(attachment);
                    client.update(attachment);
                }
                return;
            }
            // add finalizer
            addFinalizers(attachment.getMetadata(), Set.of(Constant.FINALIZER_NAME));

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
                        if (AttachmentUtils.isImage(attachment)) {
                            populateThumbnails(permalink, status);
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

    void populateThumbnails(String permalink, AttachmentStatus status) {
        var imageUri = URI.create(permalink);
        Flux.fromArray(ThumbnailSize.values())
            .flatMap(size -> thumbnailService.generate(imageUri, size)
                .map(thumbUri -> Map.entry(size.name(), thumbUri.toString()))
            )
            .collectMap(Map.Entry::getKey, Map.Entry::getValue)
            .doOnNext(status::setThumbnails)
            .block();
    }

    void updateStatus(String attachmentName, AttachmentStatus status) {
        client.fetch(Attachment.class, attachmentName)
            .filter(attachment -> !Objects.deepEquals(attachment.getStatus(), status))
            .ifPresent(attachment -> {
                attachment.setStatus(status);
                client.update(attachment);
            });
    }

    void cleanUpResources(Attachment attachment) {
        attachmentService.delete(attachment).block();
    }
}
