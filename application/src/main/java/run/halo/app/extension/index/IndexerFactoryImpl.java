package run.halo.app.extension.index;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ExtensionStoreUtil;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.SchemeManager;

/**
 * <p>A default implementation of {@link IndexerFactory}.</p>
 *
 * @author guqing
 * @since 2.12.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IndexerFactoryImpl implements IndexerFactory {
    private final ConcurrentMap<String, Indexer> keySpaceIndexer = new ConcurrentHashMap<>();

    private final IndexSpecRegistry indexSpecRegistry;
    private final SchemeManager schemeManager;

    @Override
    @NonNull
    public Indexer createIndexerFor(Class<? extends Extension> extensionType,
        ExtensionIterator<? extends Extension> extensionIterator) {
        var scheme = schemeManager.get(extensionType);
        var keySpace = indexSpecRegistry.getKeySpace(scheme);
        if (keySpaceIndexer.containsKey(keySpace)) {
            throw new IllegalArgumentException("Indexer already exists for type: " + keySpace);
        }
        if (!indexSpecRegistry.contains(scheme)) {
            indexSpecRegistry.indexFor(scheme);
        }
        var specs = indexSpecRegistry.getIndexSpecs(scheme);
        var indexDescriptors = specs.getIndexSpecs()
            .stream()
            .map(IndexDescriptor::new)
            .toList();

        final long startTimeMs = System.currentTimeMillis();
        log.info("Start building index for type: {}, please wait...", keySpace);
        var indexBuilder = IndexBuilderImpl.of(indexDescriptors, extensionIterator);
        indexBuilder.startBuildingIndex();
        var indexer =
            new DefaultIndexer(indexDescriptors, indexBuilder.getIndexEntries());
        keySpaceIndexer.put(keySpace, indexer);
        log.info("Index for type: {} built successfully, cost {} ms", keySpace,
            System.currentTimeMillis() - startTimeMs);
        return indexer;
    }

    @Override
    @NonNull
    public Indexer getIndexer(GroupVersionKind gvk) {
        var scheme = schemeManager.get(gvk);
        var indexer = keySpaceIndexer.get(indexSpecRegistry.getKeySpace(scheme));
        if (indexer == null) {
            throw new IllegalArgumentException("No indexer found for type: " + gvk);
        }
        return indexer;
    }

    @Override
    public boolean contains(GroupVersionKind gvk) {
        var schemeOpt = schemeManager.fetch(gvk);
        return schemeOpt.isPresent()
            && keySpaceIndexer.containsKey(indexSpecRegistry.getKeySpace(schemeOpt.get()));
    }

    @Override
    public void removeIndexer(Scheme scheme) {
        var keySpace = ExtensionStoreUtil.buildStoreNamePrefix(scheme);
        keySpaceIndexer.remove(keySpace);
        indexSpecRegistry.removeIndexSpecs(scheme);
    }
}
