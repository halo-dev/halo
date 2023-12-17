package run.halo.app.extension.gc;

import static run.halo.app.extension.Comparators.compareCreationTimestamp;

import java.util.function.Predicate;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.SchemeWatcherManager;
import run.halo.app.extension.SchemeWatcherManager.SchemeRegistered;
import run.halo.app.extension.Watcher;
import run.halo.app.extension.controller.RequestQueue;
import run.halo.app.extension.controller.Synchronizer;

class GcSynchronizer implements Synchronizer<GcRequest> {

    private final ExtensionClient client;

    private final SchemeManager schemeManager;

    private final SchemeWatcherManager schemeWatcherManager;

    private boolean disposed = false;

    private boolean started = false;

    private final Watcher watcher;

    GcSynchronizer(ExtensionClient client, RequestQueue<GcRequest> queue,
        SchemeManager schemeManager, SchemeWatcherManager schemeWatcherManager) {
        this.client = client;
        this.schemeManager = schemeManager;
        this.watcher = new GcWatcher(queue);
        this.schemeWatcherManager = schemeWatcherManager;
    }

    @Override
    public void dispose() {
        if (isDisposed()) {
            return;
        }
        this.disposed = true;
        this.watcher.dispose();
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    @Override
    public void start() {
        if (isDisposed() || started) {
            return;
        }
        this.started = true;
        this.schemeWatcherManager.register(event -> {
            if (event instanceof SchemeRegistered registeredEvent) {
                var newScheme = registeredEvent.getNewScheme();
                client.list(newScheme.type(), deleted(), compareCreationTimestamp(true))
                    .forEach(watcher::onDelete);
            }
        });
        client.watch(watcher);
        schemeManager.schemes().stream()
            .map(Scheme::type)
            .forEach(type -> client.list(type, deleted(), compareCreationTimestamp(true))
                .forEach(watcher::onDelete));
    }

    private <E extends Extension> Predicate<E> deleted() {
        return extension -> extension.getMetadata().getDeletionTimestamp() != null;
    }

}
