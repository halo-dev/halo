package run.halo.app.extension.gc;

import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import run.halo.app.extension.controller.Controller;
import run.halo.app.infra.InitializationPhase;

@Component
class GcControllerInitializer implements SmartLifecycle {

    private volatile boolean running;

    private final Controller gcController;

    public GcControllerInitializer(GcReconciler gcReconciler) {
        this.gcController = gcReconciler.setupWith(null);
    }

    @Override
    public void start() {
        if (running) {
            return;
        }
        running = true;
        gcController.start();
    }

    @Override
    public void stop() {
        if (!running) {
            return;
        }
        running = false;
        gcController.dispose();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return InitializationPhase.GC_CONTROLLER.getPhase();
    }
}
