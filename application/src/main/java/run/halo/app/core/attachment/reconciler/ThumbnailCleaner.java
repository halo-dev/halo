package run.halo.app.core.attachment.reconciler;

import static run.halo.app.extension.ExtensionUtil.addFinalizers;
import static run.halo.app.extension.ExtensionUtil.removeFinalizers;

import java.net.URI;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import run.halo.app.core.attachment.AttachmentUtils;
import run.halo.app.core.attachment.LocalThumbnailService;
import run.halo.app.core.attachment.ThumbnailProvider;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ExtensionMatcher;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;

@Component
@RequiredArgsConstructor
public class ThumbnailCleaner implements Reconciler<Reconciler.Request> {
    private static final String FINALIZER = "thumbnail-cleaner";

    private final ThumbnailProvider thumbnailProvider;
    private final LocalThumbnailService localThumbnailService;
    private final ExtensionClient client;

    @Override
    public Result reconcile(Request request) {
        client.fetch(Attachment.class, request.name())
            .ifPresent(attachment -> {
                if (ExtensionUtil.isDeleted(attachment)) {
                    if (removeFinalizers(attachment.getMetadata(), Set.of(FINALIZER))) {
                        deleteThumbnails(attachment);
                        client.update(attachment);
                    }
                    return;
                }
                addFinalizers(attachment.getMetadata(), Set.of(FINALIZER));
                client.update(attachment);
            });
        return Result.doNotRetry();
    }

    void deleteThumbnails(Attachment attachment) {
        var uri = Optional.ofNullable(attachment.getStatus())
            .map(Attachment.AttachmentStatus::getPermalink)
            .filter(StringUtils::isNotBlank)
            .map(URI::create)
            .orElseThrow();
        if (uri.isAbsolute()) {
            thumbnailProvider.delete(AttachmentUtils.toUrl(uri)).block();
        } else {
            // Local thumbnails maybe a relative path, so we need to process it.
            localThumbnailService.delete(uri).block();
        }
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        final ExtensionMatcher extensionMatcher = extension -> {
            var attachment = (Attachment) extension;
            return AttachmentUtils.isImage(attachment);
        };
        return builder
            .extension(new Attachment())
            .onAddMatcher(extensionMatcher)
            .onUpdateMatcher(extensionMatcher)
            .onDeleteMatcher(extensionMatcher)
            .syncAllOnStart(false)
            .build();
    }
}
