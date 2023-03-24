package run.halo.app.extension.gc;

import java.util.function.Predicate;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.Watcher;
import run.halo.app.extension.controller.RequestQueue;
import run.halo.app.extension.controller.Synchronizer;

class GcSynchronizer implements Synchronizer<GcRequest> {

    private final ExtensionClient client;

    private final RequestQueue<GcRequest> queue;

    private final SchemeManager schemeManager;

    private boolean disposed = false;

    private boolean started = false;

    private final Watcher watcher;

    GcSynchronizer(ExtensionClient client, RequestQueue<GcRequest> queue,
        SchemeManager schemeManager) {
        this.client = client;
        this.queue = queue;
        this.schemeManager = schemeManager;
        this.watcher = new GcWatcher(queue);
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
        client.watch(watcher);
        schemeManager.schemes().stream()
            .map(Scheme::type)
            .forEach(type -> client.list(type, deleted(), null)
                .forEach(watcher::onDelete));
    }

    private <E extends Extension> Predicate<E> deleted() {
        return extension -> extension.getMetadata().getDeletionTimestamp() != null;
    }

}
