package run.halo.app.extension.controller;

public interface ControllerManager {

    /**
     * Register and start a reconciler.
     *
     * @param reconciler reconciler must not be null.
     */
    void start(Reconciler<Reconciler.Request> reconciler);

    /**
     * Unregister and stop a reconciler.
     *
     * @param reconciler reconciler must not be null.
     */
    void stop(Reconciler<Reconciler.Request> reconciler);

}
