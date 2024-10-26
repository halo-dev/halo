package run.halo.app.core.attachment.reconciler;

import static run.halo.app.extension.ExtensionUtil.addFinalizers;
import static run.halo.app.extension.ExtensionUtil.removeFinalizers;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
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
import run.halo.app.extension.controller.RequeueException;

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
            if (addFinalizers(attachment.getMetadata(), Set.of(Constant.FINALIZER_NAME))) {
                client.update(attachment);
            }

            var annotations = attachment.getMetadata().getAnnotations();
            if (annotations != null) {
                var permalink = attachmentService.getPermalink(attachment)
                    .map(URI::toASCIIString)
                    .blockOptional()
                    .orElseThrow(() -> new RequeueException(new Result(true, null),
                        "Attachment handler is unavailable, requeue the request"
                    ));
                log.debug("Set permalink {} for attachment {}", permalink, request.name());
                var status = nullSafeStatus(attachment);
                status.setPermalink(permalink);
            }
            var permalink = nullSafeStatus(attachment).getPermalink();
            if (StringUtils.isNotBlank(permalink) && AttachmentUtils.isImage(attachment)) {
                populateThumbnails(permalink, attachment.getStatus());
            }
            updateStatus(request.name(), attachment.getStatus());
        });
        return null;
    }

    private static AttachmentStatus nullSafeStatus(Attachment attachment) {
        var status = attachment.getStatus();
        if (status == null) {
            status = new AttachmentStatus();
            attachment.setStatus(status);
        }
        return status;
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
        var timeout = Duration.ofSeconds(20);
        Optional.ofNullable(attachment.getStatus())
            .map(AttachmentStatus::getPermalink)
            .map(URI::create)
            .ifPresent(uri -> thumbnailService.delete(uri).block(timeout));

        attachmentService.delete(attachment).block(timeout);
    }
}
