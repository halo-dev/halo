package run.halo.app.extension;

import java.util.List;
import org.springframework.lang.NonNull;
import run.halo.app.extension.event.SchemeAddedEvent;
import run.halo.app.extension.event.SchemeRemovedEvent;

/**
 * Manages scheme watchers.
 *
 * @deprecated Please listen to {@link SchemeAddedEvent} and {@link SchemeRemovedEvent} instead.
 */
@Deprecated
public interface SchemeWatcherManager {

    void register(@NonNull SchemeWatcher watcher);

    void unregister(@NonNull SchemeWatcher watcher);

    List<SchemeWatcher> watchers();

    interface SchemeWatcher {

        void onChange(ChangeEvent event);

    }

    @Deprecated
    interface ChangeEvent {

    }

    @Deprecated
    class SchemeRegistered implements ChangeEvent {
        private final Scheme newScheme;

        public SchemeRegistered(Scheme newScheme) {
            this.newScheme = newScheme;
        }

        public Scheme getNewScheme() {
            return newScheme;
        }
    }

    @Deprecated
    class SchemeUnregistered implements ChangeEvent {

        private final Scheme deletedScheme;

        public SchemeUnregistered(Scheme deletedScheme) {
            this.deletedScheme = deletedScheme;
        }

        public Scheme getDeletedScheme() {
            return deletedScheme;
        }

    }
}
