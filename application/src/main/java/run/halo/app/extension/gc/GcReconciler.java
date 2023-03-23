package run.halo.app.extension.gc;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ExtensionConverter;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.DefaultController;
import run.halo.app.extension.controller.DefaultQueue;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.store.ExtensionStoreClient;

@Slf4j
@Component
class GcReconciler implements Reconciler<GcRequest> {

    private final ExtensionClient client;

    private final ExtensionStoreClient storeClient;

    private final ExtensionConverter converter;

    private final SchemeManager schemeManager;

    GcReconciler(ExtensionClient client, ExtensionStoreClient storeClient,
        ExtensionConverter converter, SchemeManager schemeManager) {
        this.client = client;
        this.storeClient = storeClient;
        this.converter = converter;
        this.schemeManager = schemeManager;
    }


    @Override
    public Result reconcile(GcRequest request) {
        log.debug("Extension {} is being deleted", request);

        client.fetch(request.gvk(), request.name())
            .filter(deletable())
            .ifPresent(extension -> {
                var extensionStore = converter.convertTo(extension);
                storeClient.delete(extensionStore.getName(), extensionStore.getVersion());
                log.debug("Extension {} was deleted", request);
            });

        return null;
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        var queue = new DefaultQueue<GcRequest>(Instant::now, Duration.ofMillis(500));
        var synchronizer = new GcSynchronizer(client, queue, schemeManager);
        return new DefaultController<>(
            "garbage-collector-controller",
            this,
            queue,
            synchronizer,
            Duration.ofMillis(500),
            Duration.ofSeconds(1000),
            // TODO Make it configurable
            10);
    }

    private Predicate<Extension> deletable() {
        return extension -> CollectionUtils.isEmpty(extension.getMetadata().getFinalizers())
            && extension.getMetadata().getDeletionTimestamp() != null;
    }
}
