package run.halo.app.core.attachment.reconciler;

import static run.halo.app.extension.ExtensionUtil.addFinalizers;
import static run.halo.app.extension.ExtensionUtil.removeFinalizers;

import java.net.URI;
import java.time.Duration;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import run.halo.app.core.attachment.AttachmentChangedEvent;
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
import run.halo.app.extension.controller.RequeueException;

@Slf4j
@Component
@RequiredArgsConstructor
class AttachmentReconciler implements Reconciler<Request> {

    private final ExtensionClient client;

    private final AttachmentService attachmentService;

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Result reconcile(Request request) {
        client.fetch(Attachment.class, request.name()).ifPresent(attachment -> {
            if (ExtensionUtil.isDeleted(attachment)) {
                if (removeFinalizers(attachment.getMetadata(),
                    Set.of(Constant.FINALIZER_NAME))) {
                    cleanUpResources(attachment);
                    client.update(attachment);
                    this.eventPublisher.publishEvent(new AttachmentChangedEvent(this, attachment));
                }
                return;
            }
            // add finalizer
            addFinalizers(attachment.getMetadata(), Set.of(Constant.FINALIZER_NAME));

            if (attachment.getStatus() == null) {
                attachment.setStatus(new AttachmentStatus());
            }
            var permalink = attachmentService.getPermalink(attachment)
                .map(URI::toASCIIString)
                .blockOptional(Duration.ofSeconds(10))
                .orElseThrow(() -> new RequeueException(new Result(true, null),
                    "Attachment handler is unavailable, requeue the request"
                ));
            log.debug("Set attachment permalink: {} for {}", permalink, request.name());
            attachment.getStatus().setPermalink(permalink);
            var thumbnails = attachmentService.getThumbnailLinks(attachment)
                .map(map -> map.keySet()
                    .stream()
                    .collect(Collectors.toMap(Enum::name, k -> map.get(k).toString()))
                )
                .blockOptional(Duration.ofSeconds(10))
                .orElseThrow(() -> new RequeueException(new Result(true, null), """
                    Attachment handler is unavailable for getting thumbnails links, \
                    requeue the request\
                    """
                ));
            attachment.getStatus().setThumbnails(thumbnails);
            log.debug("Set attachment thumbnails: {} for {}", thumbnails, request.name());
            client.update(attachment);
            this.eventPublisher.publishEvent(new AttachmentChangedEvent(this, attachment));
        });
        return null;
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new Attachment())
            .build();
    }

    void cleanUpResources(Attachment attachment) {
        attachmentService.delete(attachment).block(Duration.ofSeconds(20));
    }
}
