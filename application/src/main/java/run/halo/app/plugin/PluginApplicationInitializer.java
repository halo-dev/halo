package run.halo.app.plugin;

import static org.springframework.util.ResourceUtils.CLASSPATH_URL_PREFIX;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.StopWatch;
import reactor.core.Exceptions;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.properties.HaloProperties;

/**
 * Plugin application initializer will create plugin application context by plugin id and
 * register beans to plugin application context.
 *
 * @author guqing
 * @since 2021-11-01
 */
@Slf4j
public class PluginApplicationInitializer {
    protected final HaloPluginManager haloPluginManager;

    private final ExtensionContextRegistry contextRegistry = ExtensionContextRegistry.getInstance();
    private final SharedApplicationContextHolder sharedApplicationContextHolder;
    private final ApplicationContext rootApplicationContext;

    private final HaloProperties haloProperties;

    public PluginApplicationInitializer(HaloPluginManager haloPluginManager,
        ApplicationContext rootApplicationContext) {
        Assert.notNull(haloPluginManager, "The haloPluginManager must not be null");
        Assert.notNull(rootApplicationContext, "The rootApplicationContext must not be null");
        this.haloPluginManager = haloPluginManager;
        this.rootApplicationContext = rootApplicationContext;
        sharedApplicationContextHolder = rootApplicationContext
            .getBean(SharedApplicationContextHolder.class);
        haloProperties = rootApplicationContext.getBean(HaloProperties.class);
    }

    private PluginApplicationContext createPluginApplicationContext(String pluginId) {
        PluginWrapper plugin = haloPluginManager.getPlugin(pluginId);
        ClassLoader pluginClassLoader = plugin.getPluginClassLoader();

        StopWatch stopWatch = new StopWatch("initialize-plugin-context");
        stopWatch.start("Create PluginApplicationContext");
        PluginApplicationContext pluginApplicationContext = new PluginApplicationContext();
        pluginApplicationContext.setClassLoader(pluginClassLoader);

        if (sharedApplicationContextHolder != null) {
            pluginApplicationContext.setParent(sharedApplicationContextHolder.getInstance());
        }

        // populate plugin to plugin application context
        pluginApplicationContext.setPluginId(pluginId);
        stopWatch.stop();

        stopWatch.start("Create DefaultResourceLoader");
        DefaultResourceLoader defaultResourceLoader = new DefaultResourceLoader(pluginClassLoader);
        pluginApplicationContext.setResourceLoader(defaultResourceLoader);

        var mutablePropertySources = pluginApplicationContext.getEnvironment().getPropertySources();
        resolvePropertySources(pluginId, pluginApplicationContext)
            .forEach(mutablePropertySources::addLast);

        stopWatch.stop();

        DefaultListableBeanFactory beanFactory =
            (DefaultListableBeanFactory) pluginApplicationContext.getBeanFactory();

        stopWatch.start("registerAnnotationConfigProcessors");
        AnnotationConfigUtils.registerAnnotationConfigProcessors(beanFactory);
        stopWatch.stop();

        beanFactory.registerSingleton("pluginWrapper", haloPluginManager.getPlugin(pluginId));

        populateSettingFetcher(pluginId, beanFactory);

        log.debug("Total millis: {} ms -> {}", stopWatch.getTotalTimeMillis(),
            stopWatch.prettyPrint());

        return pluginApplicationContext;
    }

    private void initApplicationContext(String pluginId) {
        if (contextRegistry.containsContext(pluginId)) {
            log.debug("Plugin application context for [{}] has bean initialized.", pluginId);
            return;
        }
        StopWatch stopWatch = new StopWatch();

        stopWatch.start("createPluginApplicationContext");
        PluginApplicationContext pluginApplicationContext =
            createPluginApplicationContext(pluginId);
        stopWatch.stop();

        stopWatch.start("findCandidateComponents");
        Set<Class<?>> candidateComponents = findCandidateComponents(pluginId);
        stopWatch.stop();

        stopWatch.start("registerBean");
        for (Class<?> component : candidateComponents) {
            log.debug("Register a plugin component class [{}] to context", component);
            pluginApplicationContext.registerBean(component);
        }
        stopWatch.stop();

        stopWatch.start("refresh plugin application context");
        pluginApplicationContext.refresh();
        stopWatch.stop();

        contextRegistry.register(pluginId, pluginApplicationContext);

        log.debug("initApplicationContext total millis: {} ms -> {}",
            stopWatch.getTotalTimeMillis(), stopWatch.prettyPrint());
    }

    private void populateSettingFetcher(String pluginName,
        DefaultListableBeanFactory listableBeanFactory) {
        ReactiveExtensionClient extensionClient =
            rootApplicationContext.getBean(ReactiveExtensionClient.class);
        ReactiveSettingFetcher reactiveSettingFetcher =
            new DefaultReactiveSettingFetcher(extensionClient, pluginName);
        listableBeanFactory.registerSingleton("settingFetcher",
            new DefaultSettingFetcher(reactiveSettingFetcher));
        listableBeanFactory.registerSingleton("reactiveSettingFetcher", reactiveSettingFetcher);
    }

    public void onStartUp(String pluginId) {
        initApplicationContext(pluginId);
    }

    @NonNull
    public PluginApplicationContext getPluginApplicationContext(String pluginId) {
        return contextRegistry.getByPluginId(pluginId);
    }

    public void contextDestroyed(String pluginId) {
        Assert.notNull(pluginId, "pluginId must not be null");
        contextRegistry.remove(pluginId);
    }

    private Set<Class<?>> findCandidateComponents(String pluginId) {
        StopWatch stopWatch = new StopWatch("findCandidateComponents");

        stopWatch.start("getExtensionClassNames");
        Set<String> extensionClassNames = haloPluginManager.getExtensionClassNames(pluginId);
        if (extensionClassNames == null) {
            log.debug("No components class names found for plugin [{}]", pluginId);
            extensionClassNames = Set.of();
        }
        stopWatch.stop();

        // add extensions for each started plugin
        PluginWrapper plugin = haloPluginManager.getPlugin(pluginId);
        log.debug("Registering extensions of the plugin '{}' as beans", pluginId);
        Set<Class<?>> candidateComponents = new HashSet<>();
        for (String extensionClassName : extensionClassNames) {
            log.debug("Load extension class '{}'", extensionClassName);
            try {
                stopWatch.start("loadClass");
                Class<?> extensionClass =
                    plugin.getPluginClassLoader().loadClass(extensionClassName);
                stopWatch.stop();

                candidateComponents.add(extensionClass);
            } catch (ClassNotFoundException e) {
                log.error(e.getMessage(), e);
            }
        }
        log.debug("total millis: {}ms -> {}", stopWatch.getTotalTimeMillis(),
            stopWatch.prettyPrint());
        return candidateComponents;
    }

    private List<PropertySource<?>> resolvePropertySources(String pluginId,
        ResourceLoader resourceLoader) {
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
        logConfigLocation(resource);
        if (resource.exists()) {
            try {
                return propertySourceLoader.load(propertySourceName, resource);
            } catch (IOException e) {
                throw Exceptions.propagate(e);
            }
        }
        return List.of();
    }

    private void logConfigLocation(Resource resource) {
        if (log.isDebugEnabled()) {
            log.debug("Loading property sources from {}", resource);
        }
    }
}
