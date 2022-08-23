package run.halo.app.core.extension.reconciler;

import lombok.extern.slf4j.Slf4j;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.Reconciler.Request;

@Slf4j
public class UserReconciler implements Reconciler<Request> {

    private final ExtensionClient client;

    public UserReconciler(ExtensionClient client) {
        this.client = client;
    }

    @Override
    public Result reconcile(Request request) {
        //TODO Add reconciliation logic here for User extension.
        return new Result(false, null);
    }

}
