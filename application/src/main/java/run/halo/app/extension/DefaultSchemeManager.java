package run.halo.app.extension;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import run.halo.app.extension.event.SchemeAddedEvent;
import run.halo.app.extension.event.SchemeRemovedEvent;
import run.halo.app.extension.index.IndexEngine;
import run.halo.app.extension.index.IndexSnapshotManager;
import run.halo.app.extension.index.IndexSpecs;
import run.halo.app.extension.index.ValueIndexSpec;

@Slf4j
@Component
public class DefaultSchemeManager implements SchemeManager {

    private final List<Scheme> schemes;

    private final IndexEngine indexEngine;

    private final ApplicationEventPublisher eventPublisher;

    private final IndexSnapshotManager snapshotManager;

    public DefaultSchemeManager(
            IndexEngine indexEngine, ApplicationEventPublisher eventPublisher, IndexSnapshotManager snapshotManager) {
        this.indexEngine = indexEngine;
        this.eventPublisher = eventPublisher;
        this.snapshotManager = snapshotManager;
        // we have to use CopyOnWriteArrayList at here to prevent concurrent modification between
        // registering and listing.
        schemes = new CopyOnWriteArrayList<>();
    }

    @Override
    public <E extends Extension> void register(Class<E> type, Consumer<IndexSpecs<E>> specsConsumer) {
        var scheme = Scheme.buildFromType(type);
        if (schemes.contains(scheme)) {
            return;
        }
        var indexSpecs = new DefaultIndexSpecs<E>();
        if (specsConsumer != null) {
            specsConsumer.accept(indexSpecs);
        }
        indexEngine.getIndicesManager().add(type, indexSpecs.getIndexSpecs());
        schemes.add(scheme);
        eventPublisher.publishEvent(new SchemeAddedEvent(this, scheme));
    }

    @Override
    public void unregister(Scheme scheme) {
        if (schemes.contains(scheme)) {
            saveIndexSnapshot(scheme);
            indexEngine.getIndicesManager().remove(scheme.type());
            schemes.remove(scheme);
            eventPublisher.publishEvent(new SchemeRemovedEvent(this, scheme));
        }
    }

    private void saveIndexSnapshot(Scheme scheme) {
        try {
            var indices = indexEngine.getIndicesManager().get(scheme.type());
            snapshotManager.save(scheme.type(), indices.dump());
        } catch (Exception e) {
            log.error("Failed to save index snapshot for type {}", scheme.type().getName(), e);
        }
    }

    @Override
    public List<Scheme> schemes() {
        return Collections.unmodifiableList(schemes);
    }

    private static class DefaultIndexSpecs<E extends Extension> implements IndexSpecs<E> {

        private final Map<String, ValueIndexSpec<E, ?>> specMap;

        private DefaultIndexSpecs() {
            this.specMap = new HashMap<>();
        }

        @Override
        public <K extends Comparable<K>> void add(ValueIndexSpec<E, K> indexSpec) {
            Assert.isTrue(
                    !specMap.containsKey(indexSpec.getName()),
                    "Index spec with name " + indexSpec.getName() + " already exists.");
            this.specMap.put(indexSpec.getName(), indexSpec);
        }

        @Override
        public List<ValueIndexSpec<E, ?>> getIndexSpecs() {
            return specMap.values().stream().toList();
        }
    }
}
