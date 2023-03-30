package run.halo.app.extension.gc;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import run.halo.app.extension.controller.Controller;
import run.halo.app.infra.ExtensionInitializedEvent;

@Component
public class GcControllerInitializer
    implements ApplicationListener<ExtensionInitializedEvent>, DisposableBean {

    private final Controller gcController;

    public GcControllerInitializer(GcReconciler gcReconciler) {
        this.gcController = gcReconciler.setupWith(null);
    }

    @Override
    public void onApplicationEvent(ExtensionInitializedEvent event) {
        gcController.start();
    }

    @Override
    public void destroy() throws Exception {
        gcController.dispose();
    }
}
