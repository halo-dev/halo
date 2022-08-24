package run.halo.app.extension.gc;

import run.halo.app.extension.Extension;
import run.halo.app.extension.Watcher;
import run.halo.app.extension.controller.RequestQueue;

class GcWatcher implements Watcher {

    private final RequestQueue<GcRequest> queue;

    private Runnable disposeHook;

    private boolean disposed = false;

    GcWatcher(RequestQueue<GcRequest> queue) {
        this.queue = queue;
    }

    @Override
    public void onAdd(Extension extension) {
        // TODO Should we ignore finalizers here?
        if (!isDisposed() && extension.getMetadata().getDeletionTimestamp() != null) {
            queue.addImmediately(
                new GcRequest(extension.groupVersionKind(), extension.getMetadata().getName()));
        }
    }

    @Override
    public void onUpdate(Extension oldExt, Extension newExt) {
        if (!isDisposed() && newExt.getMetadata().getDeletionTimestamp() != null) {
            queue.addImmediately(
                new GcRequest(newExt.groupVersionKind(), newExt.getMetadata().getName()));
        }
    }

    @Override
    public void onDelete(Extension extension) {
        if (!isDisposed() && extension.getMetadata().getDeletionTimestamp() != null) {
            queue.addImmediately(
                new GcRequest(extension.groupVersionKind(), extension.getMetadata().getName()));
        }
    }

    @Override
    public void registerDisposeHook(Runnable dispose) {
        this.disposeHook = dispose;
    }

    @Override
    public void dispose() {
        if (isDisposed()) {
            return;
        }
        this.disposed = true;
        if (this.disposeHook != null) {
            this.disposeHook.run();
        }
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }
}
