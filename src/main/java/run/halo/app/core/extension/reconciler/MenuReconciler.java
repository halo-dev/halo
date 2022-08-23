package run.halo.app.core.extension.reconciler;

import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Reconciler;

public class MenuReconciler implements Reconciler<Reconciler.Request> {

    private final ExtensionClient client;

    public MenuReconciler(ExtensionClient client) {
        this.client = client;
    }

    @Override
    public Result reconcile(Request request) {
        return new Result(false, null);
    }

}
