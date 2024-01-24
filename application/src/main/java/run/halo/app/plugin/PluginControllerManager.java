package run.halo.app.plugin;

import static org.springframework.core.ResolvableType.forClassWithGenerics;

import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import reactor.core.Disposable;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;

public class PluginControllerManager {

    private final ConcurrentHashMap<String, Controller> controllers;

    private final ExtensionClient client;

    public PluginControllerManager(ExtensionClient client) {
        this.client = client;
        controllers = new ConcurrentHashMap<>();
    }

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        event.getApplicationContext()
            .<Reconciler<Reconciler.Request>>getBeanProvider(
                forClassWithGenerics(Reconciler.class, Reconciler.Request.class))
            .orderedStream()
            .forEach(this::start);
    }

    @EventListener
    public void onApplicationEvent(ContextClosedEvent event) throws Exception {
        controllers.values()
            .forEach(Disposable::dispose);
        controllers.clear();
    }

    private void start(Reconciler<Reconciler.Request> reconciler) {
        var builder = new ControllerBuilder(reconciler, client);
        var controller = reconciler.setupWith(builder);
        controllers.put(reconciler.getClass().getName(), controller);
        controller.start();
    }

}
