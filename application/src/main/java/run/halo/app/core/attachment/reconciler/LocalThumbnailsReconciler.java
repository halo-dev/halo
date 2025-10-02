package run.halo.app.core.attachment.reconciler;

import static run.halo.app.extension.ExtensionUtil.addFinalizers;
import static run.halo.app.extension.ExtensionUtil.removeFinalizers;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import run.halo.app.core.attachment.AttachmentRootGetter;
import run.halo.app.core.attachment.extension.LocalThumbnail;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;

@Slf4j
@Component
@RequiredArgsConstructor
@Deprecated(forRemoval = true, since = "2.22.0")
class LocalThumbnailsReconciler implements Reconciler<Reconciler.Request> {

    private static final String CLEAN_UP_FINALIZER = "thumbnail-cleaner";

    private final ExtensionClient client;

    private final AttachmentRootGetter attachmentRoot;

    @Override
    public Result reconcile(Request request) {
        client.fetch(LocalThumbnail.class, request.name())
            .ifPresent(thumbnail -> {
                if (ExtensionUtil.isDeleted(thumbnail)) {
                    if (removeFinalizers(thumbnail.getMetadata(), Set.of(CLEAN_UP_FINALIZER))) {
                        // clean up thumbnail file
                        cleanUpThumbnailFile(thumbnail);
                        client.update(thumbnail);
                    }
                    return;
                }
                // Cleanup all existing local thumbnails
                addFinalizers(thumbnail.getMetadata(), Set.of(CLEAN_UP_FINALIZER));
                log.info("Cleaning up local thumbnail: {}", thumbnail.getMetadata().getName());
                client.delete(thumbnail);
            });
        return Result.doNotRetry();
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new LocalThumbnail())
            .build();
    }

    private void cleanUpThumbnailFile(LocalThumbnail thumbnail) {
        var filePath = thumbnail.getSpec().getFilePath();
        if (StringUtils.hasText(filePath)) {
            var thumbnailFile = attachmentRoot.get().resolve(filePath);
            try {
                if (Files.deleteIfExists(thumbnailFile)) {
                    log.info("Deleted thumbnail file: {} for {}",
                        thumbnailFile, thumbnail.getMetadata().getName());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
