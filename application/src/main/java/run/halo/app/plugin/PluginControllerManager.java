package run.halo.app.plugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import org.springframework.context.event.EventListener;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.ControllerManager;
import run.halo.app.extension.controller.DefaultControllerManager;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.Reconciler.Request;
import run.halo.app.plugin.event.HaloPluginBeforeStopEvent;
import run.halo.app.plugin.event.HaloPluginStartedEvent;

@Component
public class PluginControllerManager {

    private final Map<String, ControllerManager> controllerManagerMap;

    private final ExtensionClient client;

    public PluginControllerManager(ExtensionClient client) {
        this.client = client;
        controllerManagerMap = new ConcurrentHashMap<>();
    }

    @EventListener
    public void onPluginStarted(HaloPluginStartedEvent event) {
        var plugin = event.getPlugin();

        var controllerManager = controllerManagerMap.computeIfAbsent(plugin.getPluginId(),
            id -> new DefaultControllerManager(client));

        getReconcilers(plugin.getPluginId())
            .forEach(controllerManager::start);
    }

    @EventListener
    public void onPluginBeforeStop(HaloPluginBeforeStopEvent event) {
        // remove controller manager
        var plugin = event.getPlugin();
        var controllerManager = controllerManagerMap.remove(plugin.getPluginId());
        if (controllerManager != null) {
            // stop all reconcilers
            getReconcilers(plugin.getPluginId())
                .forEach(controllerManager::stop);
        }
    }

    private Stream<Reconciler<Request>> getReconcilers(String pluginId) {
        var context = ExtensionContextRegistry.getInstance().getByPluginId(pluginId);
        return context.<Reconciler<Request>>getBeanProvider(
                ResolvableType.forClassWithGenerics(Reconciler.class, Request.class))
            .orderedStream();
    }
}
