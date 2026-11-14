package run.halo.app.extension.indexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
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
import run.halo.app.extension.index.IndexSnapshot;
import run.halo.app.extension.index.IndexSnapshotManager;
import run.halo.app.extension.index.IndicesInitializer;
import run.halo.app.extension.index.IndicesSnapshot;
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
            var loaded = snapshotManager.load(type);
            if (loaded.isPresent()) {
                try {
                    restoreFromSnapshot(type, prefix, loaded.get());
                    return;
                } catch (Exception e) {
                    log.warn(
                            "Failed to restore indices for type {} from snapshot, falling back to full build",
                            type.getName(),
                            e);
                    // purge the restored-but-possibly-stale state before the insert-only full build,
                    // otherwise rows deleted from the database would survive as ghost entries; the full
                    // build below re-inserts everything that actually exists in the database
                    loaded.ifPresent(snapshot ->
                            snapshot.versions().keySet().forEach(name -> indexEngine.deleteByName(type, name)));
                }
            }
            fullBuild(type, prefix);
        } finally {
            indexEngine.getIndicesManager().markReady(type);
        }
    }

    private <E extends Extension> void restoreFromSnapshot(Class<E> type, String prefix, IndicesSnapshot snapshot) {
        var watch = new StopWatch("Restore indices for " + type.getName());
        watch.start("restore snapshot");
        var indices = indexEngine.getIndicesManager().get(type);
        var currentFingerprints = indices.currentFingerprints();
        var matched = snapshot.indices().stream()
                .filter(s -> Objects.equals(s.fingerprint(), currentFingerprints.get(s.name())))
                .toList();
        var matchedNames = matched.stream().map(IndexSnapshot::name).collect(Collectors.toSet());
        var dirtyIndexNames = currentFingerprints.keySet().stream()
                .filter(name -> !matchedNames.contains(name))
                .collect(Collectors.toSet());
        indices.restore(new IndicesSnapshot(matched, snapshot.versions()));
        watch.stop();
        if (dirtyIndexNames.isEmpty()) {
            deltaRecover(type, prefix, snapshot.versions(), watch);
        } else {
            log.info("Indices {} of type {} need rebuild, scanning full table", dirtyIndexNames, type.getName());
            mergedScanRecover(type, prefix, snapshot.versions(), dirtyIndexNames, watch);
        }
        log.info("Restored indices for type {}, summary: {}", type.getName(), watch.prettyPrint(TimeUnit.MILLISECONDS));
    }

    /** Delta recovery via the narrow name/version query; no full-table data scan. */
    private <E extends Extension> void deltaRecover(
            Class<E> type, String prefix, Map<String, Long> manifest, StopWatch watch) {
        watch.start("delta recover");
        var dbRows = client.listNameVersionsByNamePrefix(prefix);
        var manifestByStoreName = new HashMap<String, Long>();
        manifest.forEach((name, version) -> manifestByStoreName.put(prefix + "/" + name, version));
        var dbStoreNames = new HashSet<String>();
        var staleStoreNames = new ArrayList<String>();
        for (var row : dbRows) {
            dbStoreNames.add(row.name());
            var recorded = manifestByStoreName.get(row.name());
            if (recorded == null || !recorded.equals(row.version())) {
                staleStoreNames.add(row.name());
            }
        }
        for (var from = 0; from < staleStoreNames.size(); from += BATCH_SIZE) {
            var batch = staleStoreNames.subList(from, Math.min(from + BATCH_SIZE, staleStoreNames.size()));
            var stores = client.listByNames(batch);
            indexEngine.insert(stores.stream().map(es -> this.extensionConverter.<E>convertFrom(type, es))::iterator);
        }
        manifestByStoreName.keySet().stream()
                .filter(storeName -> !dbStoreNames.contains(storeName))
                .map(storeName -> storeName.substring(prefix.length() + 1))
                .forEach(name -> indexEngine.deleteByName(type, name));
        watch.stop();
    }

    /** Merged full-table scan: rebuild dirty indices and apply the delta in one pass. */
    private <E extends Extension> void mergedScanRecover(
            Class<E> type, String prefix, Map<String, Long> manifest, Set<String> dirtyIndexNames, StopWatch watch) {
        watch.start("merged scan recover");
        var indices = indexEngine.getIndicesManager().get(type);
        var seenNames = new HashSet<String>();
        List<ExtensionStore> stores;
        String nameCursor = null;
        do {
            stores = client.listBy(prefix, nameCursor, BATCH_SIZE);
            for (var store : stores) {
                var extension = this.extensionConverter.<E>convertFrom(type, store);
                var name = extension.getMetadata().getName();
                seenNames.add(name);
                var recorded = manifest.get(name);
                if (recorded != null && recorded.equals(store.getVersion())) {
                    // unchanged row: only fill the dirty indices
                    indices.updateIndices(extension, dirtyIndexNames);
                } else {
                    // new or changed row: upsert all indices
                    indexEngine.insert(List.of(extension));
                }
            }
            if (!stores.isEmpty()) {
                nameCursor = stores.getLast().getName();
            }
        } while (!stores.isEmpty());
        manifest.keySet().stream()
                .filter(name -> !seenNames.contains(name))
                .forEach(name -> indexEngine.deleteByName(type, name));
        watch.stop();
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
