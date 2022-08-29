package run.halo.app.core.extension.reconciler.attachment;

import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.Reconciler.Request;

public class AttachmentReconciler implements Reconciler<Request> {

    private final ExtensionClient client;

    public AttachmentReconciler(ExtensionClient client) {
        this.client = client;
    }

    @Override
    public Result reconcile(Request request) {
        return null;
    }

}
