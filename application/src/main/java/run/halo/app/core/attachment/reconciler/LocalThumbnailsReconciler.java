package run.halo.app.core.attachment.reconciler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import run.halo.app.core.attachment.LocalThumbnailService;
import run.halo.app.core.extension.attachment.LocalThumbnail;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;

@Component
@RequiredArgsConstructor
public class LocalThumbnailsReconciler implements Reconciler<Reconciler.Request> {
    private final LocalThumbnailService localThumbnailService;
    private final ExtensionClient client;

    @Override
    public Result reconcile(Request request) {
        client.fetch(LocalThumbnail.class, request.name())
            .ifPresent(thumbnail -> {
                // Generate method can be called multiple times
                localThumbnailService.generate(thumbnail).block();
            });
        return Result.doNotRetry();
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new LocalThumbnail())
            .syncAllOnStart(false)
            .build();
    }
}
