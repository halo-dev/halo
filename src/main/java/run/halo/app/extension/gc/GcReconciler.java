package run.halo.app.extension.gc;

import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ExtensionConverter;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.store.ExtensionStoreClient;

@Slf4j
class GcReconciler implements Reconciler<GcRequest> {

    private final ExtensionClient client;

    private final ExtensionStoreClient storeClient;

    private final ExtensionConverter converter;

    GcReconciler(ExtensionClient client, ExtensionStoreClient storeClient,
        ExtensionConverter converter) {
        this.client = client;
        this.storeClient = storeClient;
        this.converter = converter;
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

    private Predicate<Extension> deletable() {
        return extension -> CollectionUtils.isEmpty(extension.getMetadata().getFinalizers())
            && extension.getMetadata().getDeletionTimestamp() != null;
    }
}
