package run.halo.app.plugin;

import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginWrapper;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;

/**
 * Plugin mapping manager.
 *
 * @author guqing
 * @see RequestMappingHandlerMapping
 */
@Slf4j
public class PluginRequestMappingManager {

    private final PluginRequestMappingHandlerMapping requestMappingHandlerMapping;

    public PluginRequestMappingManager(
        PluginRequestMappingHandlerMapping pluginRequestMappingHandlerMapping) {
        this.requestMappingHandlerMapping = pluginRequestMappingHandlerMapping;
    }

    public void registerHandlerMappings(PluginWrapper pluginWrapper) {
        String pluginId = pluginWrapper.getPluginId();
        getControllerBeans(pluginId)
            .forEach(handler ->
                requestMappingHandlerMapping.registerHandlerMethods(pluginId, handler));
    }

    public void removeHandlerMappings(String pluginId) {
        requestMappingHandlerMapping.unregister(pluginId);
    }

    private Collection<Object> getControllerBeans(String pluginId) {
        GenericApplicationContext pluginContext =
            ExtensionContextRegistry.getInstance().getByPluginId(pluginId);
        return pluginContext.getBeansWithAnnotation(Controller.class).values();
    }
}
