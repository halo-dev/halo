package run.halo.app.extension.index;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.lang.NonNull;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ExtensionStoreUtil;
import run.halo.app.extension.Scheme;

/**
 * <p>A default implementation of {@link IndexSpecRegistry}.</p>
 *
 * @author guqing
 * @since 2.12.0
 */
public class IndexSpecRegistryImpl implements IndexSpecRegistry {
    private final ConcurrentMap<String, IndexSpecs> extensionIndexSpecs = new ConcurrentHashMap<>();

    @Override
    public IndexSpecs indexFor(Scheme scheme) {
        var keySpace = getKeySpace(scheme);
        var indexSpecs = new DefaultIndexSpecs();
        useDefaultIndexSpec(scheme.type(), indexSpecs);
        extensionIndexSpecs.put(keySpace, indexSpecs);
        return indexSpecs;
    }

    @Override
    public IndexSpecs getIndexSpecs(Scheme scheme) {
        var keySpace = getKeySpace(scheme);
        var result = extensionIndexSpecs.get(keySpace);
        if (result == null) {
            throw new IllegalArgumentException(
                "No index specs found for extension type: " + scheme.groupVersionKind()
                    + ", make sure you have called indexFor() before calling getIndexSpecs()");

        }
        return result;
    }

    @Override
    public boolean contains(Scheme scheme) {
        var keySpace = getKeySpace(scheme);
        return extensionIndexSpecs.containsKey(keySpace);
    }

    @Override
    public void removeIndexSpecs(Scheme scheme) {
        var keySpace = getKeySpace(scheme);
        extensionIndexSpecs.remove(keySpace);
    }

    @Override
    @NonNull
    public String getKeySpace(Scheme scheme) {
        return ExtensionStoreUtil.buildStoreNamePrefix(scheme);
    }

    <E extends Extension> void useDefaultIndexSpec(Class<E> extensionType,
        IndexSpecs indexSpecs) {
        var nameIndexSpec = PrimaryKeySpecUtils.primaryKeyIndexSpec(extensionType);
        indexSpecs.add(nameIndexSpec);

        var creationTimestampIndexSpec = new IndexSpec()
            .setName("metadata.creationTimestamp")
            .setOrder(IndexSpec.OrderType.ASC)
            .setUnique(false)
            .setIndexFunc(IndexAttributeFactory.simpleAttribute(extensionType,
                e -> e.getMetadata().getCreationTimestamp().toString())
            );
        indexSpecs.add(creationTimestampIndexSpec);

        var deletionTimestampIndexSpec = new IndexSpec()
            .setName("metadata.deletionTimestamp")
            .setOrder(IndexSpec.OrderType.ASC)
            .setUnique(false)
            .setIndexFunc(IndexAttributeFactory.simpleAttribute(extensionType,
                e -> Objects.toString(e.getMetadata().getDeletionTimestamp(), null))
            );
        indexSpecs.add(deletionTimestampIndexSpec);

        indexSpecs.add(LabelIndexSpecUtils.labelIndexSpec(extensionType));
    }
}
