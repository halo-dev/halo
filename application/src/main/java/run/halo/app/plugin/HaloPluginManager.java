package run.halo.app.plugin;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.*;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.data.util.Lazy;
import run.halo.app.infra.SystemVersionSupplier;
import run.halo.app.plugin.event.PluginStartedEvent;

/**
 * PluginManager to hold the main ApplicationContext. It provides methods for managing the plugin lifecycle.
 *
 * @author guqing
 * @author johnniang
 * @since 2.0.0
 */
@Slf4j
class HaloPluginManager extends DefaultPluginManager implements SpringPluginManager, InitializingBean, DisposableBean {

    private final ApplicationContext rootContext;

    private Lazy<ApplicationContext> sharedContext;

    private final PluginProperties pluginProperties;

    private final PluginsRootGetter pluginsRootGetter;

    private final SystemVersionSupplier systemVersionSupplier;

    public HaloPluginManager(
            ApplicationContext rootContext,
            PluginProperties pluginProperties,
            SystemVersionSupplier systemVersionSupplier,
            PluginsRootGetter pluginsRootGetter) {
        this.pluginProperties = pluginProperties;
        this.rootContext = rootContext;
        this.pluginsRootGetter = pluginsRootGetter;
        this.systemVersionSupplier = systemVersionSupplier;
    }

    @Override
    protected void initialize() {
        // Leave the implementation empty because the super#initialize eagerly initializes
        // components before properties set.
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.runtimeMode = pluginProperties.getRuntimeMode();
        this.sharedContext = Lazy.of(() -> SharedApplicationContextFactory.create(rootContext));
        setExactVersionAllowed(pluginProperties.isExactVersionAllowed());
        setSystemVersion(systemVersionSupplier.get().toStableVersion().toString());

        super.initialize();
        // the listener must be after the super#initialize
        addPluginStateListener(new PluginStartedListener());
    }

    @Override
    protected ExtensionFactory createExtensionFactory() {
        return new SpringExtensionFactory(this);
    }

    @Override
    protected ExtensionFinder createExtensionFinder() {
        var finder = new SpringComponentsFinder(this);
        addPluginStateListener(finder);
        return finder;
    }

    @Override
    protected PluginFactory createPluginFactory() {
        var contextFactory = new DefaultPluginApplicationContextFactory(this);
        var pluginGetter = rootContext.getBean(PluginGetter.class);
        return new SpringPluginFactory(contextFactory, pluginGetter);
    }

    @Override
    protected PluginDescriptorFinder createPluginDescriptorFinder() {
        return new YamlPluginDescriptorFinder();
    }

    @Override
    protected PluginLoader createPluginLoader() {
        var compoundLoader = new CompoundPluginLoader();
        compoundLoader.add(new DevPluginLoader(this, this.pluginProperties), this::isDevelopment);
        compoundLoader.add(new JarPluginLoader(this));
        return compoundLoader;
    }

    @Override
    protected PluginStatusProvider createPluginStatusProvider() {
        if (PropertyPluginStatusProvider.isPropertySet(pluginProperties)) {
            return new PropertyPluginStatusProvider(pluginProperties);
        }
        // Only plugins root is writeable
        return new DefaultPluginStatusProvider(pluginsRootGetter.get());
    }

    @Override
    protected PluginRepository createPluginRepository() {
        var developmentPluginRepository = new DefaultDevelopmentPluginRepository(getPluginsRoots());
        developmentPluginRepository.setFixedPaths(pluginProperties.getFixedPluginPath());
        return new CompoundPluginRepository()
                .add(developmentPluginRepository, this::isDevelopment)
                .add(new JarPluginRepository(getPluginsRoots()))
                .add(new DefaultPluginRepository(getPluginsRoots()));
    }

    @Override
    protected List<Path> createPluginsRoot() {
        return List.of(pluginsRootGetter.get());
    }

    @Override
    public void startPlugins() {
        throw new UnsupportedOperationException("The operation of starting all plugins is not supported.");
    }

    @Override
    public void stopPlugins() {
        // Stop started plugins one by one. PF4J's stopPlugin stops a plugin's
        // dependents first and ignores plugins that are not started, so the
        // dependency order is handled by PF4J itself. Copy the list because
        // stopping a plugin mutates it. A failure of one plugin must not
        // prevent the others from being stopped.
        var pluginsToStop = new ArrayList<>(startedPlugins());
        log.debug(
                "Stopping {} started plugin(s): {}",
                pluginsToStop.size(),
                pluginsToStop.stream().map(PluginWrapper::getPluginId).toList());
        for (var pw : pluginsToStop) {
            try {
                var pluginState = stopPlugin(pw.getPluginId());
                log.debug("Plugin '{}' stop result: {}.", pw.getPluginId(), pluginState);
            } catch (Exception e) {
                log.warn("Error stopping plugin '{}' during shutdown.", pw.getPluginId(), e);
            }
        }
    }

    @Override
    public void unloadPlugins() {
        // Unload resolved plugins one by one to release their class loaders.
        // PF4J's unloadPlugin unloads a plugin's dependents first and ignores
        // plugins that are not loaded. Copy the list because unloading a plugin
        // mutates it. A failure of one plugin must not prevent the others from
        // being unloaded.
        var pluginsToUnload = new ArrayList<>(getResolvedPlugins());
        log.debug(
                "Unloading {} resolved plugin(s): {}",
                pluginsToUnload.size(),
                pluginsToUnload.stream().map(PluginWrapper::getPluginId).toList());
        for (var pw : pluginsToUnload) {
            try {
                var unloaded = unloadPlugin(pw.getPluginId());
                log.debug("Plugin '{}' unload result: {}.", pw.getPluginId(), unloaded);
            } catch (Exception e) {
                log.warn("Error unloading plugin '{}' during shutdown.", pw.getPluginId(), e);
            }
        }
    }

    @Override
    public void destroy() {
        log.info("Stopping and unloading all plugins before application context shutdown...");
        stopPlugins();
        unloadPlugins();
        log.info("All plugins stopped and unloaded.");
    }

    @Override
    public ApplicationContext getRootContext() {
        return rootContext;
    }

    @Override
    public ApplicationContext getSharedContext() {
        return sharedContext.get();
    }

    @Override
    public List<PluginWrapper> getDependents(String pluginId) {
        if (getPlugin(pluginId) == null) {
            return List.of();
        }

        var dependents = new ArrayList<PluginWrapper>();
        var stack = new Stack<String>();
        dependencyResolver.getDependents(pluginId).forEach(stack::push);
        while (!stack.isEmpty()) {
            var dependent = stack.pop();
            var pluginWrapper = getPlugin(dependent);
            if (pluginWrapper != null) {
                dependents.add(pluginWrapper);
                dependencyResolver.getDependents(dependent).forEach(stack::push);
            }
        }
        return dependents;
    }

    @Override
    public List<PluginWrapper> startedPlugins() {
        return List.copyOf(super.getStartedPlugins()).stream()
                // Make sure the plugin is really started
                .filter(p -> p.getPluginState().isStarted())
                .toList();
    }

    /**
     * Listener for plugin started event.
     *
     * @author johnniang
     * @since 2.17.0
     */
    private static class PluginStartedListener implements PluginStateListener {

        @Override
        public void pluginStateChanged(PluginStateEvent event) {
            if (PluginState.STARTED.equals(event.getPluginState())) {
                var plugin = event.getPlugin().getPlugin();
                if (plugin instanceof SpringPlugin springPlugin) {
                    try {
                        springPlugin.getApplicationContext().publishEvent(new PluginStartedEvent(this));
                    } catch (Throwable t) {
                        var pluginId = event.getPlugin().getPluginId();
                        log.warn("Error while publishing plugin started event for plugin {}", pluginId, t);
                    }
                }
            }
        }
    }
}
