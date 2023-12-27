package run.halo.app.extension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import run.halo.app.extension.SchemeWatcherManager.SchemeRegistered;
import run.halo.app.extension.SchemeWatcherManager.SchemeUnregistered;
import run.halo.app.extension.index.IndexSpecRegistry;
import run.halo.app.extension.index.IndexSpecs;

public class DefaultSchemeManager implements SchemeManager {

    private final List<Scheme> schemes;

    private final IndexSpecRegistry indexSpecRegistry;

    @Nullable
    private final SchemeWatcherManager watcherManager;

    public DefaultSchemeManager(IndexSpecRegistry indexSpecRegistry,
        @Nullable SchemeWatcherManager watcherManager) {
        this.indexSpecRegistry = indexSpecRegistry;
        this.watcherManager = watcherManager;
        // we have to use CopyOnWriteArrayList at here to prevent concurrent modification between
        // registering and listing.
        schemes = new CopyOnWriteArrayList<>();
    }

    @Override
    public void register(@NonNull Scheme scheme) {
        if (!schemes.contains(scheme)) {
            indexSpecRegistry.indexFor(scheme);
            schemes.add(scheme);
            getWatchers().forEach(watcher -> watcher.onChange(new SchemeRegistered(scheme)));
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
        getWatchers().forEach(watcher -> watcher.onChange(new SchemeRegistered(scheme)));
    }

    @Override
    public void unregister(@NonNull Scheme scheme) {
        if (schemes.contains(scheme)) {
            indexSpecRegistry.removeIndexSpecs(scheme);
            schemes.remove(scheme);
            getWatchers().forEach(watcher -> watcher.onChange(new SchemeUnregistered(scheme)));
        }
    }

    @Override
    @NonNull
    public List<Scheme> schemes() {
        return Collections.unmodifiableList(schemes);
    }

    @NonNull
    private List<SchemeWatcherManager.SchemeWatcher> getWatchers() {
        if (this.watcherManager == null) {
            return Collections.emptyList();
        }
        return Optional.ofNullable(this.watcherManager.watchers()).orElse(Collections.emptyList());
    }
}
