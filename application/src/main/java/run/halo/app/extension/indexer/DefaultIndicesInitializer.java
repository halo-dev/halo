package run.halo.app.extension.indexer;

import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ExtensionConverter;
import run.halo.app.extension.ExtensionStoreUtil;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.event.SchemeAddedEvent;
import run.halo.app.extension.index.IndexEngine;
import run.halo.app.extension.index.IndexSnapshotManager;
import run.halo.app.extension.index.IndicesInitializer;
import run.halo.app.extension.store.ExtensionStore;
import run.halo.app.extension.store.ExtensionStoreClient;

@Component
@Slf4j
class DefaultIndicesInitializer implements IndicesInitializer {

    private static final int BATCH_SIZE = 100;

    private final IndexEngine indexEngine;

    private final ExtensionStoreClient client;

    private final ExtensionConverter extensionConverter;

    private final IndexSnapshotManager snapshotManager;

    DefaultIndicesInitializer(
            IndexEngine indexEngine,
            ExtensionStoreClient client,
            ExtensionConverter extensionConverter,
            IndexSnapshotManager snapshotManager) {
        this.indexEngine = indexEngine;
        this.client = client;
        this.extensionConverter = extensionConverter;
        this.snapshotManager = snapshotManager;
    }

    /**
     * Must run before any other {@link SchemeAddedEvent} listener that queries indices (e.g. the GC synchronizer):
     * event listeners execute synchronously on the publishing thread, and index queries block on
     * {@link run.halo.app.extension.index.IndicesManager#awaitReady} until this initializer marks the indices ready. If
     * a querying listener ran first, it would deadlock the publishing thread until the readiness timeout.
     */
    @EventListener
    @Order(Ordered.HIGHEST_PRECEDENCE)
    void onSchemeAddedEvent(SchemeAddedEvent event) {
        this.initialize(event.getScheme());
    }

    @Override
    public void initialize(Scheme scheme) {
        doInitialize(scheme);
    }

    public <E extends Extension> void doInitialize(Scheme scheme) {
        var type = (Class<E>) scheme.type();
        var prefix = ExtensionStoreUtil.buildStoreNamePrefix(scheme);
        try {
            // gate queries until the build/restore below completes
            indexEngine.getIndicesManager().markBuilding(type);
            snapshotManager.initialize(scheme, () -> fullBuild(type, prefix));
        } catch (RuntimeException | Error failure) {
            indexEngine.getIndicesManager().remove(type);
            throw failure;
        }
        indexEngine.getIndicesManager().markReady(type);
    }

    private <E extends Extension> void fullBuild(Class<E> type, String prefix) {
        List<ExtensionStore> extensionStores;
        String nameCursor = null;
        log.info("Start to initialize indices for type: {}, prefix: {}", type.getName(), prefix);
        var watch = new StopWatch("Initialize indices for " + type.getName());
        var indexedCount = 0L;
        do {
            watch.start("Indexing from " + (nameCursor == null ? "@start" : nameCursor));
            extensionStores = client.listBy(prefix, nameCursor, BATCH_SIZE);
            indexEngine.insert(
                    extensionStores.stream().map(es -> this.extensionConverter.convertFrom(type, es))::iterator);
            if (!extensionStores.isEmpty()) {
                nameCursor = extensionStores.getLast().getName();
            }
            indexedCount += extensionStores.size();
            watch.stop();
        } while (!extensionStores.isEmpty());
        log.info(
                "Total indexed count: {}, initialization summary: {}",
                indexedCount,
                watch.prettyPrint(TimeUnit.MILLISECONDS));
    }
}
