package run.halo.app.extension.controller;

import run.halo.app.extension.Extension;
import run.halo.app.extension.Watcher;
import run.halo.app.extension.WatcherPredicates;
import run.halo.app.extension.controller.Reconciler.Request;

public class ExtensionWatcher implements Watcher {

    private final RequestQueue<Request> queue;

    private volatile boolean disposed = false;

    private Runnable disposeHook;
    private final WatcherPredicates predicates;

    public ExtensionWatcher(RequestQueue<Request> queue, WatcherPredicates predicates) {
        this.queue = queue;
        this.predicates = predicates;
    }

    @Override
    public void onAdd(Extension extension) {
        if (isDisposed() || !predicates.onAddPredicate().test(extension)) {
            return;
        }
        // TODO filter the event
        queue.addImmediately(new Request(extension.getMetadata().getName()));
    }

    @Override
    public void onUpdate(Extension oldExtension, Extension newExtension) {
        if (isDisposed() || !predicates.onUpdatePredicate().test(oldExtension, newExtension)) {
            return;
        }
        // TODO filter the event
        queue.addImmediately(new Request(newExtension.getMetadata().getName()));
    }

    @Override
    public void onDelete(Extension extension) {
        if (isDisposed() || !predicates.onDeletePredicate().test(extension)) {
            return;
        }
        // TODO filter the event
        queue.addImmediately(new Request(extension.getMetadata().getName()));
    }

    @Override
    public void registerDisposeHook(Runnable dispose) {
        this.disposeHook = dispose;
    }

    @Override
    public void dispose() {
        disposed = true;
        if (this.disposeHook != null) {
            this.disposeHook.run();
        }
    }

    @Override
    public boolean isDisposed() {
        return this.disposed;
    }

}
