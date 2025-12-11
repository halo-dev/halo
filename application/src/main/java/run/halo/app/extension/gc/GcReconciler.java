package run.halo.app.extension.gc;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ExtensionConverter;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.DefaultController;
import run.halo.app.extension.controller.DefaultQueue;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.RequestQueue;
import run.halo.app.extension.event.SchemeAddedEvent;
import run.halo.app.extension.index.IndexEngine;
import run.halo.app.extension.store.ReactiveExtensionStoreClient;

@Slf4j
@Component
class GcReconciler implements Reconciler<GcRequest> {

    private final ExtensionClient client;

    private final ReactiveExtensionStoreClient storeClient;

    private final ExtensionConverter converter;

    private final IndexEngine indexEngine;

    private final SchemeManager schemeManager;

    private final RequestQueue<GcRequest> queue;

    private final GcSynchronizer synchronizer;

    private final ReactiveTransactionManager transactionManager;

    private Scheduler scheduler;

    GcReconciler(ExtensionClient client,
        ReactiveExtensionStoreClient storeClient,
        ExtensionConverter converter,
        SchemeManager schemeManager,
        IndexEngine indexEngine, ReactiveTransactionManager transactionManager) {
        this.client = client;
        this.storeClient = storeClient;
        this.converter = converter;
        this.indexEngine = indexEngine;
        this.transactionManager = transactionManager;
        this.queue = new DefaultQueue<>(Instant::now, Duration.ofMillis(500));
        this.synchronizer = new GcSynchronizer(client, queue, schemeManager);
        this.schemeManager = schemeManager;
        this.scheduler = Schedulers.boundedElastic();
    }

    @Override
    public Result reconcile(GcRequest request) {
        log.debug("Extension {} is being deleted", request);
        var scheme = schemeManager.get(request.gvk());
        client.fetch(scheme.type(), request.name())
            .filter(deletable())
            .ifPresent(extension -> doDelete(extension).blockOptional(Duration.ofSeconds(30)));
        return null;
    }

    private <E extends Extension> Mono<Void> doDelete(E extension) {
        var extensionStore = converter.convertTo(extension);
        var tx = TransactionalOperator.create(transactionManager);

        return storeClient.delete(extensionStore.getName(), extensionStore.getVersion())
            .flatMap(deleted -> Mono.fromRunnable(() -> indexEngine.delete(List.of(extension)))
                .subscribeOn(this.scheduler)
            )
            .as(tx::transactional)
            .then()
            .doOnSuccess(ignored ->
                log.info("Extension {}/{} was deleted", extension.groupVersionKind(), extension)
            );
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
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

    @EventListener
    void onSchemeAddedEvent(SchemeAddedEvent event) {
        synchronizer.onApplicationEvent(event);
    }

    private Predicate<Extension> deletable() {
        return extension -> CollectionUtils.isEmpty(extension.getMetadata().getFinalizers())
            && extension.getMetadata().getDeletionTimestamp() != null;
    }
}
