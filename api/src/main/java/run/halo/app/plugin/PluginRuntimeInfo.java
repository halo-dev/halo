package run.halo.app.plugin;

import java.util.Map;
import lombok.Builder;

/**
 * Read-only runtime diagnostics information for a plugin.
 *
 * @param pluginName plugin metadata name
 * @param displayName plugin display name
 * @param version plugin descriptor version
 * @param state current PF4J plugin state
 * @param classLoaderName plugin class loader implementation name
 * @param loadedExtensionClassCount number of extension classes discovered by the plugin manager
 * @param beanDefinitionCount number of bean definitions in the plugin application context
 * @param singletonBeanCount number of singleton beans in the plugin application context
 * @param routerFunctionCount number of RouterFunction beans exposed by the plugin
 * @param finderCount number of theme Finder beans exposed by the plugin
 * @param websocketEndpointCount number of WebSocketEndpoint beans exposed by the plugin
 * @param extensionMappings counts of plugin extension resources grouped by GroupVersionKind
 * @author webjing
 * @since 2.25.0
 */
@Builder
public record PluginRuntimeInfo(
        String pluginName,
        String displayName,
        String version,
        String state,
        String classLoaderName,
        int loadedExtensionClassCount,
        int beanDefinitionCount,
        int singletonBeanCount,
        int routerFunctionCount,
        int finderCount,
        int websocketEndpointCount,
        Map<String, Integer> extensionMappings) {}
