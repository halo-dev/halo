package run.halo.app.content;

import java.time.Duration;
import java.time.Instant;
import org.springframework.context.SmartLifecycle;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.DefaultController;
import run.halo.app.extension.controller.DefaultQueue;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.RequestQueue;

/**
 * An abstract class for reconciling events.
 *
 * @author guqing
 * @since 2.15.0
 */
public abstract class AbstractEventReconciler<E> implements Reconciler<E>, SmartLifecycle {
    protected final RequestQueue<E> queue;

    protected final Controller controller;

    protected volatile boolean running = false;

    protected final ExtensionClient client;

    private final String controllerName;

    protected AbstractEventReconciler(String controllerName, ExtensionClient client) {
        this.client = client;
        this.controllerName = controllerName;
        this.queue = new DefaultQueue<>(Instant::now);
        this.controller = this.setupWith(null);
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return new DefaultController<>(
            controllerName,
            this,
            queue,
            null,
            Duration.ofMillis(100),
            Duration.ofMinutes(10)
        );
    }

    @Override
    public void start() {
        controller.start();
        running = true;
    }

    @Override
    public void stop() {
        running = false;
        controller.dispose();
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
