package run.halo.app.extension.gc;

import java.util.List;
import org.springframework.data.domain.Sort;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.SchemeWatcherManager;
import run.halo.app.extension.SchemeWatcherManager.SchemeRegistered;
import run.halo.app.extension.Watcher;
import run.halo.app.extension.controller.RequestQueue;
import run.halo.app.extension.controller.Synchronizer;
import run.halo.app.extension.index.query.QueryFactory;
import run.halo.app.extension.router.selector.FieldSelector;

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
                listDeleted(newScheme.type()).forEach(watcher::onDelete);
            }
        });
        client.watch(watcher);
        schemeManager.schemes().stream()
            .map(Scheme::type)
            .forEach(type -> listDeleted(type).forEach(watcher::onDelete));
    }

    <E extends Extension> List<E> listDeleted(Class<E> type) {
        var options = new ListOptions()
            .setFieldSelector(
                FieldSelector.of(QueryFactory.isNotNull("metadata.deletionTimestamp"))
            );
        return client.listAll(type, options, Sort.by(Sort.Order.asc("metadata.creationTimestamp")));
    }
}
