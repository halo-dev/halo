package run.halo.app.extension.index;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;
import org.apache.commons.io.IOUtils;
import run.halo.app.extension.Extension;

class DefaultIndicesManager implements IndicesManager {

    private final ConcurrentMap<Class<? extends Extension>, Indices<? extends Extension>>
        indicesMap;

    DefaultIndicesManager() {
        indicesMap = new ConcurrentHashMap<>();
    }

    @Override
    public <E extends Extension> void add(Class<E> type, List<ValueIndexSpec<E, ?>> indexSpecs) {
        indicesMap.computeIfAbsent(type, t -> {
            var indices = new ArrayList<Index<E, ?>>();
            Stream.concat(indexSpecs.stream(), this.<E>createDefaultIndexSpecs().stream())
                .distinct()
                .forEach(indexSpec -> {
                    if (indexSpec instanceof MultiValueIndexSpec<E, ?> spec) {
                        indices.add(new MultiValueIndex<>(spec));
                    } else if (indexSpec instanceof SingleValueIndexSpec<E, ?> spec) {
                        indices.add(new SingleValueIndex<>(spec));
                    }
                    // ignore other implementations, should never happen
                });
            indices.add(new LabelIndexImpl<>());
            return new DefaultIndices<>(indices);
        });
    }

    @Override
    public void close() throws IOException {
        IOUtils.close(indicesMap.values().toArray(Indices[]::new));
        indicesMap.clear();
    }

    @Override
    public <E extends Extension> Indices<E> get(Class<E> type) {
        var indices = (Indices<E>) indicesMap.get(type);
        if (indices == null) {
            throw new IllegalArgumentException("No indices found for type: " + type.getName());
        }
        return indices;
    }

    @Override
    public <E extends Extension> void remove(Class<E> type) {
        var indices = indicesMap.remove(type);
        IOUtils.closeQuietly(indices);
    }

    private <E extends Extension> List<ValueIndexSpec<E, ?>> createDefaultIndexSpecs() {
        var metadataNameSpec =
            SingleValueIndexSpec.<E, String>builder("metadata.name", String.class)
                .indexFunc(e -> e.getMetadata().getName())
                .unique(true)
                .nullable(false)
                .build();
        var creationTimestampSpec =
            SingleValueIndexSpec.<E, Instant>builder("metadata.creationTimestamp", Instant.class)
                .indexFunc(e -> e.getMetadata().getCreationTimestamp())
                .unique(false)
                .nullable(false)
                .build();
        var deletionTimestampSpec =
            SingleValueIndexSpec.<E, Instant>builder("metadata.deletionTimestamp", Instant.class)
                .indexFunc(e -> e.getMetadata().getDeletionTimestamp())
                .unique(false)
                .nullable(true)
                .build();
        return List.of(metadataNameSpec, creationTimestampSpec, deletionTimestampSpec);
    }
}
