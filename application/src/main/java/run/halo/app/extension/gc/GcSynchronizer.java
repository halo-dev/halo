package run.halo.app.extension.gc;

import java.util.List;
import org.springframework.context.ApplicationListener;
import org.springframework.data.domain.Sort;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.Watcher;
import run.halo.app.extension.controller.RequestQueue;
import run.halo.app.extension.controller.Synchronizer;
import run.halo.app.extension.event.SchemeAddedEvent;
import run.halo.app.extension.index.query.QueryFactory;
import run.halo.app.extension.router.selector.FieldSelector;

class GcSynchronizer implements Synchronizer<GcRequest>, ApplicationListener<SchemeAddedEvent> {

    private final ExtensionClient client;

    private final SchemeManager schemeManager;

    private boolean disposed = false;

    private boolean started = false;

    private final Watcher watcher;

    GcSynchronizer(ExtensionClient client,
        RequestQueue<GcRequest> queue,
        SchemeManager schemeManager) {
        this.client = client;
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
    public void onApplicationEvent(SchemeAddedEvent event) {
        if (started) {
            var scheme = event.getScheme();
            listDeleted(scheme.type()).forEach(watcher::onDelete);
        }
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
            .forEach(type -> listDeleted(type).forEach(watcher::onDelete));
    }

    <E extends Extension> List<E> listDeleted(Class<E> type) {
        var options = new ListOptions()
            .setFieldSelector(
                FieldSelector.of(QueryFactory.isNotNull("metadata.deletionTimestamp"))
            );
        // TODO Refine with scrolling query
        return client.listAll(type, options, Sort.by(Sort.Order.asc("metadata.creationTimestamp")));
    }
}
