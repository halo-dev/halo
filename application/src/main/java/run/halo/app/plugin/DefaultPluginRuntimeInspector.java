package run.halo.app.plugin;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import run.halo.app.core.endpoint.WebSocketEndpoint;
import run.halo.app.core.extension.Plugin;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.infra.exception.NotFoundException;
import run.halo.app.theme.finders.Finder;

/**
 * Default implementation of {@link PluginRuntimeInspector}.
 *
 * @author webjing
 * @since 2.25.0
 */
@Component
class DefaultPluginRuntimeInspector implements PluginRuntimeInspector {

    private final SpringPluginManager pluginManager;

    private final PluginGetter pluginGetter;

    DefaultPluginRuntimeInspector(SpringPluginManager pluginManager, PluginGetter pluginGetter) {
        this.pluginManager = pluginManager;
        this.pluginGetter = pluginGetter;
    }

    @Override
    public List<PluginRuntimeInfo> list() {
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

    private static ApplicationContext getApplicationContext(org.pf4j.Plugin plugin) {
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
            org.pf4j.PluginWrapper pluginWrapper, @Nullable Plugin pluginExtension, ApplicationContext context) {
        var classLoader = pluginWrapper.getPluginClassLoader();
        var classLoaderName = classLoader == null ? null : classLoader.getClass().getName();
        return PluginRuntimeInfo.builder()
                .pluginName(pluginWrapper.getPluginId())
                .displayName(displayName(pluginExtension))
                .version(pluginWrapper.getDescriptor().getVersion())
                .state(pluginWrapper.getPluginState().toString())
                .classLoaderName(classLoaderName)
                .loadedExtensionClassCount(pluginManager.getExtensionClassNames(pluginWrapper.getPluginId()).size())
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

    private static int beanDefinitionCount(ApplicationContext context) {
        return context == null ? 0 : context.getBeanDefinitionCount();
    }

    private static int singletonBeanCount(ApplicationContext context) {
        if (context instanceof ConfigurableApplicationContext configurableContext) {
            return configurableContext.getBeanFactory().getSingletonCount();
        }
        return 0;
    }

    private static int routerFunctionCount(ApplicationContext context) {
        if (context == null) {
            return 0;
        }
        return context.getBeanProvider(RouterFunction.class)
                .orderedStream()
                .mapToInt(ignored -> 1)
                .sum();
    }

    private static int finderCount(ApplicationContext context) {
        return context == null ? 0 : context.getBeansWithAnnotation(Finder.class).size();
    }

    private static int webSocketEndpointCount(ApplicationContext context) {
        if (context == null) {
            return 0;
        }
        return (int) context.getBeanProvider(WebSocketEndpoint.class).orderedStream().count();
    }

    private static Map<String, Integer> extensionMappings(ApplicationContext context) {
        if (!(context instanceof PluginApplicationContext pluginApplicationContext)) {
            return Map.of();
        }
        return pluginApplicationContext.extensionNamesMapping().entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                        entry -> toMappingKey(entry.getKey()),
                        entry -> entry.getValue().size()));
    }

    private static String toMappingKey(GroupVersionKind gvk) {
        return gvk.toString();
    }
}
