package run.halo.app.extension.controller;

import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Watcher;
import run.halo.app.extension.controller.Reconciler.Request;

@Slf4j
public class RequestSynchronizer implements Synchronizer<Request> {

    private final ExtensionClient client;

    private final Class<? extends Extension> type;

    private final boolean syncAllOnStart;

    private volatile boolean disposed = false;

    private volatile boolean started = false;

    private final Watcher watcher;

    private final Predicate<Extension> listPredicate;

    public RequestSynchronizer(boolean syncAllOnStart,
        ExtensionClient client,
        Extension extension,
        Watcher watcher,
        Predicate<Extension> listPredicate) {
        this.syncAllOnStart = syncAllOnStart;
        this.client = client;
        this.type = extension.getClass();
        this.watcher = watcher;
        if (listPredicate == null) {
            listPredicate = e -> true;
        }
        this.listPredicate = listPredicate;
    }

    @Override
    public void start() {
        if (isDisposed() || started) {
            return;
        }
        log.info("Starting request({}) synchronizer...", type);
        started = true;

        if (syncAllOnStart) {
            client.list(type, listPredicate::test, null)
                .forEach(watcher::onAdd);
        }
        client.watch(this.watcher);
        log.info("Started request({}) synchronizer.", type);
    }

    public boolean isStarted() {
        return started;
    }

    @Override
    public void dispose() {
        disposed = true;
        watcher.dispose();
    }

    @Override
    public boolean isDisposed() {
        return this.disposed;
    }
}
