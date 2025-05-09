package run.halo.app.extension;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import run.halo.app.extension.event.SchemeAddedEvent;
import run.halo.app.extension.event.SchemeRemovedEvent;
import run.halo.app.extension.index.IndexSpecRegistry;
import run.halo.app.extension.index.IndexSpecs;

@Component
public class DefaultSchemeManager implements SchemeManager {

    private final List<Scheme> schemes;

    private final IndexSpecRegistry indexSpecRegistry;

    private final ApplicationEventPublisher eventPublisher;

    public DefaultSchemeManager(IndexSpecRegistry indexSpecRegistry,
        ApplicationEventPublisher eventPublisher) {
        this.indexSpecRegistry = indexSpecRegistry;
        this.eventPublisher = eventPublisher;
        // we have to use CopyOnWriteArrayList at here to prevent concurrent modification between
        // registering and listing.
        schemes = new CopyOnWriteArrayList<>();
    }

    @Override
    public void register(@NonNull Scheme scheme) {
        if (!schemes.contains(scheme)) {
            indexSpecRegistry.indexFor(scheme);
            schemes.add(scheme);
            eventPublisher.publishEvent(new SchemeAddedEvent(this, scheme));
        }
    }

    @Override
    public void register(@NonNull Scheme scheme, Consumer<IndexSpecs> specsConsumer) {
        if (schemes.contains(scheme)) {
            return;
        }
        var indexSpecs = indexSpecRegistry.indexFor(scheme);
        specsConsumer.accept(indexSpecs);
        schemes.add(scheme);
        eventPublisher.publishEvent(new SchemeAddedEvent(this, scheme));
    }

    @Override
    public void unregister(@NonNull Scheme scheme) {
        if (schemes.contains(scheme)) {
            indexSpecRegistry.removeIndexSpecs(scheme);
            schemes.remove(scheme);
            eventPublisher.publishEvent(new SchemeRemovedEvent(this, scheme));
        }
    }

    @Override
    @NonNull
    public List<Scheme> schemes() {
        return Collections.unmodifiableList(schemes);
    }

}
