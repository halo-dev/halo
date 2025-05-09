package run.halo.app.extension.controller;

import static org.springframework.core.ResolvableType.forClassWithGenerics;

import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.SmartLifecycle;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Reconciler.Request;
import run.halo.app.infra.InitializationPhase;

@Slf4j
public class DefaultControllerManager implements ApplicationContextAware, SmartLifecycle {

    private final ExtensionClient client;

    private ApplicationContext applicationContext;

    /**
     * Map with key: reconciler class name, value: controller self.
     */
    private final ConcurrentHashMap<String, Controller> controllers;

    private volatile boolean running;

    public DefaultControllerManager(ExtensionClient client) {
        this.client = client;
        controllers = new ConcurrentHashMap<>();
    }

    @Override
    public void start() {
        if (this.running) {
            return;
        }
        this.running = true;
        // register reconcilers in system after scheme initialized
        applicationContext.<Reconciler<Request>>getBeanProvider(
                forClassWithGenerics(Reconciler.class, Request.class))
            .orderedStream()
            .forEach(this::start);
    }

    void start(Reconciler<Request> reconciler) {
        var builder = new ControllerBuilder(reconciler, client);
        var controller = reconciler.setupWith(builder);
        controllers.put(reconciler.getClass().getName(), controller);
        controller.start();
    }

    @Override
    public void stop() {
        if (!running) {
            return;
        }
        this.running = false;
        log.info("Shutting down {} controllers...", controllers.size());
        controllers.forEach((name, controller) -> disposeSilently(controller));
        log.info("Shutdown {} controllers.", controllers.size());
    }

    private static void disposeSilently(Controller controller) {
        if (controller == null) {
            return;
        }
        try {
            log.info("Shutting down controller {}...", controller.getName());
            controller.dispose();
            log.info("Shutdown controller {} successfully", controller.getName());
        } catch (Throwable t) {
            log.error("Failed to dispose controller {}", controller.getName(), t);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return InitializationPhase.CONTROLLERS.getPhase();
    }

}
