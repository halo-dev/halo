package run.halo.app.extension;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import run.halo.app.extension.event.SchemeAddedEvent;
import run.halo.app.extension.event.SchemeRemovedEvent;
import run.halo.app.extension.index.IndexEngine;
import run.halo.app.extension.index.IndexSpecs;
import run.halo.app.extension.index.ValueIndexSpec;

@Component
public class DefaultSchemeManager implements SchemeManager {

    private final List<Scheme> schemes;

    private final IndexEngine indexEngine;

    private final ApplicationEventPublisher eventPublisher;

    public DefaultSchemeManager(IndexEngine indexEngine,
        ApplicationEventPublisher eventPublisher) {
        this.indexEngine = indexEngine;
        this.eventPublisher = eventPublisher;
        // we have to use CopyOnWriteArrayList at here to prevent concurrent modification between
        // registering and listing.
        schemes = new CopyOnWriteArrayList<>();
    }

    @Override
    public <E extends Extension> void register(Class<E> type,
        Consumer<IndexSpecs<E>> specsConsumer) {
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
    public void unregister(@NonNull Scheme scheme) {
        if (schemes.contains(scheme)) {
            indexEngine.getIndicesManager().remove(scheme.type());
            schemes.remove(scheme);
            eventPublisher.publishEvent(new SchemeRemovedEvent(this, scheme));
        }
    }

    @Override
    @NonNull
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
            Assert.isTrue(!specMap.containsKey(indexSpec.getName()),
                "Index spec with name " + indexSpec.getName() + " already exists.");
            this.specMap.put(indexSpec.getName(), indexSpec);
        }

        @Override
        public List<ValueIndexSpec<E, ?>> getIndexSpecs() {
            return specMap.values().stream().toList();
        }

    }
}
