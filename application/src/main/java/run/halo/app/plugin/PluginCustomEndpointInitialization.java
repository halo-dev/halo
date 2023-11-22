package run.halo.app.plugin;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.core.extension.endpoint.CustomEndpointsBuilder;
import run.halo.app.plugin.event.HaloPluginStartedEvent;

/**
 * <p>An Initialization class for plugin's {@link RouterFunction}.</p>
 *
 * @author guqing
 * @since 2.11.0
 */
@Component
@RequiredArgsConstructor
public class PluginCustomEndpointInitialization {

    private final HaloPluginManager haloPluginManager;

    /**
     * Get all {@link CustomEndpoint} beans from plugin's {@link PluginApplicationContext} and
     * create a {@link RouterFunction} for them to register to the bean factory on plugin startup.
     */
    @Async
    @EventListener(HaloPluginStartedEvent.class)
    public void onPluginStartUp(HaloPluginStartedEvent event) {
        var pluginName = event.getPlugin().getPluginId();
        var applicationContext = haloPluginManager.getPluginApplicationContext(pluginName);

        var endpoints = applicationContext.getBeansOfType(CustomEndpoint.class).values();
        if (endpoints.isEmpty()) {
            return;
        }
        var endpointBuilder = new CustomEndpointsBuilder();
        endpoints.forEach(endpointBuilder::add);
        applicationContext.registerBean("pluginCustomEndpointRouter",
            RouterFunction.class, endpointBuilder::build);
    }
}
