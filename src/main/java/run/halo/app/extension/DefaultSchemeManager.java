package run.halo.app.extension;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import run.halo.app.extension.SchemeWatcherManager.SchemeRegistered;
import run.halo.app.extension.SchemeWatcherManager.SchemeUnregistered;

public class DefaultSchemeManager implements SchemeManager {

    private final List<Scheme> schemes;

    @Nullable
    private final SchemeWatcherManager watcherManager;

    public DefaultSchemeManager(@Nullable SchemeWatcherManager watcherManager) {
        this.watcherManager = watcherManager;
        schemes = new LinkedList<>();
    }

    @Override
    public void register(@NonNull Scheme scheme) {
        if (!schemes.contains(scheme)) {
            schemes.add(scheme);
            getWatchers().forEach(watcher -> watcher.onChange(new SchemeRegistered(scheme)));
        }
    }

    @Override
    public void unregister(@NonNull Scheme scheme) {
        if (schemes.contains(scheme)) {
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
