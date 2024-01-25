package run.halo.app.plugin;

import static org.springframework.util.ResourceUtils.CLASSPATH_URL_PREFIX;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginRuntimeException;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.Exceptions;
import run.halo.app.PluginApplicationContextFactory;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.plugin.event.HaloPluginBeforeStopEvent;
import run.halo.app.plugin.event.HaloPluginStartedEvent;
import run.halo.app.plugin.event.HaloPluginStoppedEvent;
import run.halo.app.plugin.event.SpringPluginStartedEvent;
import run.halo.app.plugin.event.SpringPluginStoppedEvent;
import run.halo.app.plugin.event.SpringPluginStoppingEvent;
import run.halo.app.theme.DefaultTemplateNameResolver;
import run.halo.app.theme.ViewNameResolver;
import run.halo.app.theme.finders.FinderRegistry;

@Slf4j
public class DefaultPluginApplicationContextFactory implements PluginApplicationContextFactory {

    private final SpringPluginManager pluginManager;

    public DefaultPluginApplicationContextFactory(SpringPluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    /**
     * Create and refresh application context. Make sure the plugin has already loaded
     * before.
     *
     * @param pluginId plugin id
     * @return refresh application context for the plugin.
     */
    @Override
    public ApplicationContext create(String pluginId) {
        log.debug("Preparing to create application context for plugin {}", pluginId);
        var pluginWrapper = pluginManager.getPlugin(pluginId);

        var context = new PluginApplicationContext(pluginId);
        context.registerShutdownHook();
        context.setParent(pluginManager.getSharedContext());

        var classLoader = pluginWrapper.getPluginClassLoader();
        var resourceLoader = new DefaultResourceLoader(classLoader);
        context.setResourceLoader(resourceLoader);
        var mutablePropertySources = context.getEnvironment().getPropertySources();

        resolvePropertySources(pluginId, resourceLoader)
            .forEach(mutablePropertySources::addLast);

        var beanFactory = context.getBeanFactory();
        context.registerBean(AggregatedRouterFunction.class);
        beanFactory.registerSingleton("pluginWrapper", pluginWrapper);

        var rootContext = pluginManager.getRootContext();
        rootContext.getBeanProvider(ViewNameResolver.class)
            .ifAvailable(viewNameResolver -> {
                var templateNameResolver =
                    new DefaultTemplateNameResolver(viewNameResolver, context);
                beanFactory.registerSingleton("templateNameResolver", templateNameResolver);
            });

        rootContext.getBeanProvider(ReactiveExtensionClient.class)
            .ifAvailable(client -> {
                var reactiveSettingFetcher = new DefaultReactiveSettingFetcher(client, pluginId);
                var settingFetcher = new DefaultSettingFetcher(reactiveSettingFetcher);
                beanFactory.registerSingleton("reactiveSettingFetcher", reactiveSettingFetcher);
                beanFactory.registerSingleton("settingFetcher", settingFetcher);
            });

        var classNames = pluginManager.getExtensionClassNames(pluginId);
        classNames.stream()
            .map(className -> {
                try {
                    return classLoader.loadClass(className);
                } catch (ClassNotFoundException e) {
                    throw new PluginRuntimeException(String.format("""
                        Failed to load class %s for plugin %s.\
                        """, className, pluginId), e);
                }
            })
            .forEach(context::register);

        rootContext.getBeanProvider(PluginRequestMappingHandlerMapping.class)
            .ifAvailable(handlerMapping -> {
                var handlerMappingManager =
                    new PluginHandlerMappingManager(pluginId, handlerMapping);
                beanFactory.registerSingleton("pluginHandlerMappingManager", handlerMappingManager);
            });

        context.registerBean(PluginControllerManager.class);
        beanFactory.registerSingleton("springPluginStoppedEventAdapter",
            new SpringPluginStoppedEventAdapter(pluginId));
        beanFactory.registerSingleton("haloPluginEventBridge", new HaloPluginEventBridge());

        rootContext.getBeanProvider(FinderRegistry.class)
            .ifAvailable(finderRegistry -> {
                var finderManager = new FinderManager(pluginId, finderRegistry);
                beanFactory.registerSingleton("finderManager", finderManager);
            });

        rootContext.getBeanProvider(PluginRouterFunctionRegistry.class)
            .ifUnique(registry -> {
                var pluginRouterFunctionManager = new PluginRouterFunctionManager(registry);
                beanFactory.registerSingleton(
                    "pluginRouterFunctionManager",
                    pluginRouterFunctionManager
                );
            });

        log.debug("Created application context for plugin {}", pluginId);
        log.debug("Refreshing application context for plugin {}", pluginId);

        context.refresh();
        log.debug("Refreshed application context for plugin {}", pluginId);
        return context;
    }

    private static class FinderManager {

        private final String pluginId;

        private final FinderRegistry finderRegistry;

        private FinderManager(String pluginId, FinderRegistry finderRegistry) {
            this.pluginId = pluginId;
            this.finderRegistry = finderRegistry;
        }

        @EventListener
        public void onApplicationEvent(ContextClosedEvent ignored) {
            this.finderRegistry.unregister(this.pluginId);
        }

        @EventListener
        public void onApplicationEvent(ContextRefreshedEvent event) {
            this.finderRegistry.register(this.pluginId, event.getApplicationContext());
        }

    }

    private static class PluginRouterFunctionManager {

        private final PluginRouterFunctionRegistry routerFunctionRegistry;

        private Collection<RouterFunction<ServerResponse>> routerFunctions;

        private PluginRouterFunctionManager(PluginRouterFunctionRegistry routerFunctionRegistry) {
            this.routerFunctionRegistry = routerFunctionRegistry;
        }

        @EventListener
        public void onApplicationEvent(ContextClosedEvent ignored) {
            if (routerFunctions != null) {
                routerFunctionRegistry.unregister(routerFunctions);
            }
        }

        @EventListener
        public void onApplicationEvent(ContextRefreshedEvent event) {
            var routerFunctions = event.getApplicationContext()
                .<RouterFunction<ServerResponse>>getBeanProvider(
                    ResolvableType.forClassWithGenerics(RouterFunction.class, ServerResponse.class)
                )
                .orderedStream()
                .toList();
            routerFunctionRegistry.register(routerFunctions);
            this.routerFunctions = routerFunctions;
        }
    }


    private static class PluginHandlerMappingManager {
        private final String pluginId;

        private final PluginRequestMappingHandlerMapping handlerMapping;

        private PluginHandlerMappingManager(String pluginId,
            PluginRequestMappingHandlerMapping handlerMapping) {
            this.pluginId = pluginId;
            this.handlerMapping = handlerMapping;
        }

        @EventListener
        public void onApplicationEvent(ContextRefreshedEvent event) {
            var context = event.getApplicationContext();
            context.getBeansWithAnnotation(Controller.class)
                .values()
                .forEach(controller ->
                    handlerMapping.registerHandlerMethods(this.pluginId, controller)
                );
        }

        @EventListener
        public void onApplicationEvent(ContextClosedEvent ignored) {
            handlerMapping.unregister(this.pluginId);
        }
    }

    private class SpringPluginStoppedEventAdapter
        implements ApplicationListener<ContextClosedEvent> {

        private final String pluginId;

        private SpringPluginStoppedEventAdapter(String pluginId) {
            this.pluginId = pluginId;
        }

        @Override
        public void onApplicationEvent(ContextClosedEvent event) {
            var plugin = pluginManager.getPlugin(pluginId).getPlugin();
            if (plugin instanceof SpringPlugin springPlugin) {
                event.getApplicationContext()
                    .publishEvent(new SpringPluginStoppedEvent(this, springPlugin));
            }
        }
    }

    private class HaloPluginEventBridge {

        @EventListener
        public void onApplicationEvent(SpringPluginStartedEvent event) {
            var pluginContext = event.getSpringPlugin().getPluginContext();
            var pluginWrapper = pluginManager.getPlugin(pluginContext.getName());

            pluginManager.getRootContext()
                .publishEvent(new HaloPluginStartedEvent(this, pluginWrapper));
        }

        @EventListener
        public void onApplicationEvent(SpringPluginStoppingEvent event) {
            var pluginContext = event.getSpringPlugin().getPluginContext();
            var pluginWrapper = pluginManager.getPlugin(pluginContext.getName());
            pluginManager.getRootContext()
                .publishEvent(new HaloPluginBeforeStopEvent(this, pluginWrapper));
        }

        @EventListener
        public void onApplicationEvent(SpringPluginStoppedEvent event) {
            var pluginContext = event.getSpringPlugin().getPluginContext();
            var pluginWrapper = pluginManager.getPlugin(pluginContext.getName());
            pluginManager.getRootContext()
                .publishEvent(new HaloPluginStoppedEvent(this, pluginWrapper));
        }

    }

    private List<PropertySource<?>> resolvePropertySources(String pluginId,
        ResourceLoader resourceLoader) {
        var haloProperties = pluginManager.getRootContext()
            .getBeanProvider(HaloProperties.class)
            .getIfAvailable();
        if (haloProperties == null) {
            return List.of();
        }

        var propertySourceLoader = new YamlPropertySourceLoader();
        var propertySources = new ArrayList<PropertySource<?>>();
        var configsPath = haloProperties.getWorkDir().resolve("plugins").resolve("configs");
        // resolve user defined config
        Stream.of(
                configsPath.resolve(pluginId + ".yaml"),
                configsPath.resolve(pluginId + ".yml")
            )
            .map(path -> resourceLoader.getResource(path.toUri().toString()))
            .forEach(resource -> {
                var sources =
                    loadPropertySources("user-defined-config", resource, propertySourceLoader);
                propertySources.addAll(sources);
            });

        // resolve default config
        Stream.of(
                CLASSPATH_URL_PREFIX + "/config.yaml",
                CLASSPATH_URL_PREFIX + "/config.yaml"
            )
            .map(resourceLoader::getResource)
            .forEach(resource -> {
                var sources = loadPropertySources("default-config", resource, propertySourceLoader);
                propertySources.addAll(sources);
            });
        return propertySources;
    }

    private List<PropertySource<?>> loadPropertySources(String propertySourceName,
        Resource resource,
        PropertySourceLoader propertySourceLoader) {
        if (log.isDebugEnabled()) {
            log.debug("Loading property sources from {}", resource);
        }
        if (!resource.exists()) {
            return List.of();
        }
        try {
            return propertySourceLoader.load(propertySourceName, resource);
        } catch (IOException e) {
            throw Exceptions.propagate(e);
        }
    }
}
