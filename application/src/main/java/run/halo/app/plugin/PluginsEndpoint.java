package run.halo.app.plugin;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import run.halo.app.core.endpoint.WebSocketEndpoint;
import run.halo.app.core.extension.Plugin;
import run.halo.app.infra.exception.NotFoundException;
import run.halo.app.theme.finders.Finder;

/**
 * Exposes read-only runtime diagnostics for plugins.
 *
 * @author webjing
 * @since 2.25.0
 */
@WebEndpoint(id = "plugins")
@Component
class PluginsEndpoint {

    private final SpringPluginManager pluginManager;

    private final PluginGetter pluginGetter;

    PluginsEndpoint(SpringPluginManager pluginManager, PluginGetter pluginGetter) {
        this.pluginManager = pluginManager;
        this.pluginGetter = pluginGetter;
    }

    @ReadOperation
    public List<PluginRuntimeInfo> plugins() {
        return pluginManager.startedPlugins().stream()
                .map(pluginWrapper -> {
                    var plugin = pluginWrapper.getPlugin();
                    var context = getApplicationContext(plugin);
                    var pluginExtension = getPluginExtension(pluginWrapper.getPluginId());
                    return buildRuntimeInfo(pluginWrapper, pluginExtension, context);
                })
                .toList();
    }

    private @Nullable Plugin getPluginExtension(String pluginName) {
        try {
            return pluginGetter.getPlugin(pluginName);
        } catch (NotFoundException ignored) {
            return null;
        }
    }

    private static @Nullable ApplicationContext getApplicationContext(org.pf4j.Plugin plugin) {
        if (plugin instanceof SpringPlugin springPlugin) {
            try {
                return springPlugin.getApplicationContext();
            } catch (IllegalStateException ignored) {
                return null;
            }
        }
        return null;
    }

    private PluginRuntimeInfo buildRuntimeInfo(
            org.pf4j.PluginWrapper pluginWrapper,
            @Nullable Plugin pluginExtension,
            @Nullable ApplicationContext context) {
        var classLoader = pluginWrapper.getPluginClassLoader();
        var classLoaderName =
                classLoader == null ? null : classLoader.getClass().getName();
        return PluginRuntimeInfo.builder()
                .pluginName(pluginWrapper.getPluginId())
                .displayName(displayName(pluginExtension))
                .version(pluginWrapper.getDescriptor().getVersion())
                .state(pluginWrapper.getPluginState().toString())
                .classLoaderName(classLoaderName)
                .loadedExtensionClassCount(pluginManager
                        .getExtensionClassNames(pluginWrapper.getPluginId())
                        .size())
                .beanDefinitionCount(beanDefinitionCount(context))
                .singletonBeanCount(singletonBeanCount(context))
                .routerFunctionCount(routerFunctionCount(context))
                .finderCount(finderCount(context))
                .websocketEndpointCount(webSocketEndpointCount(context))
                .extensionMappings(extensionMappings(context))
                .build();
    }

    private static @Nullable String displayName(@Nullable Plugin pluginExtension) {
        if (pluginExtension == null || pluginExtension.getSpec() == null) {
            return null;
        }
        return pluginExtension.getSpec().getDisplayName();
    }

    private static int beanDefinitionCount(@Nullable ApplicationContext context) {
        return context == null ? 0 : context.getBeanDefinitionCount();
    }

    private static int singletonBeanCount(@Nullable ApplicationContext context) {
        if (context instanceof ConfigurableApplicationContext configurableContext) {
            return configurableContext.getBeanFactory().getSingletonCount();
        }
        return 0;
    }

    private static int routerFunctionCount(@Nullable ApplicationContext context) {
        if (context == null) {
            return 0;
        }
        return (int) context.getBeanProvider(RouterFunction.class).stream().count();
    }

    private static int finderCount(@Nullable ApplicationContext context) {
        if (context == null) {
            return 0;
        }
        return context.getBeanNamesForAnnotation(Finder.class).length;
    }

    private static int webSocketEndpointCount(@Nullable ApplicationContext context) {
        if (context == null) {
            return 0;
        }
        return (int) context.getBeanProvider(WebSocketEndpoint.class).stream().count();
    }

    private static Map<String, Integer> extensionMappings(@Nullable ApplicationContext context) {
        if (!(context instanceof PluginApplicationContext pluginApplicationContext)) {
            return Map.of();
        }
        return pluginApplicationContext.extensionNamesMapping().entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                        entry -> entry.getKey().toString(),
                        entry -> entry.getValue().size()));
    }

    @lombok.Builder
    record PluginRuntimeInfo(
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
}
