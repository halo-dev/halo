package run.halo.app.extension.controller;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.Watcher;
import run.halo.app.extension.controller.Reconciler.Request;
import run.halo.app.extension.index.IndexedQueryEngine;

@Slf4j
public class RequestSynchronizer implements Synchronizer<Request> {

    private final ExtensionClient client;

    private final GroupVersionKind type;

    private final boolean syncAllOnStart;

    private volatile boolean disposed = false;

    private final IndexedQueryEngine indexedQueryEngine;

    private final Watcher watcher;

    private final ListOptions listOptions;

    @Getter
    private volatile boolean started = false;

    public RequestSynchronizer(boolean syncAllOnStart,
        ExtensionClient client,
        Extension extension,
        Watcher watcher,
        ListOptions listOptions) {
        this.syncAllOnStart = syncAllOnStart;
        this.client = client;
        this.type = extension.groupVersionKind();
        this.watcher = watcher;
        this.indexedQueryEngine = client.indexedQueryEngine();
        this.listOptions = listOptions;
    }

    @Override
    public void start() {
        if (isDisposed() || started) {
            return;
        }
        log.info("Starting request({}) synchronizer...", type);
        started = true;

        if (syncAllOnStart) {
            indexedQueryEngine.retrieveAll(type, listOptions, Sort.by("metadata.creationTimestamp"))
                .forEach(name -> watcher.onAdd(new Request(name)));
        }
        client.watch(this.watcher);
        log.info("Started request({}) synchronizer.", type);
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
