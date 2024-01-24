package run.halo.app.plugin;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.DefaultPluginManager;
import org.pf4j.ExtensionFactory;
import org.pf4j.ExtensionFinder;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginDescriptorFinder;
import org.pf4j.PluginFactory;
import org.pf4j.PluginRuntimeException;
import org.pf4j.PluginState;
import org.pf4j.PluginStateEvent;
import org.pf4j.PluginStateListener;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.data.util.Lazy;
import run.halo.app.plugin.event.HaloPluginBeforeStopEvent;
import run.halo.app.plugin.event.HaloPluginLoadedEvent;
import run.halo.app.plugin.event.HaloPluginStartedEvent;
import run.halo.app.plugin.event.HaloPluginStoppedEvent;

/**
 * PluginManager to hold the main ApplicationContext.
 * It provides methods for managing the plugin lifecycle.
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
public class HaloPluginManager extends DefaultPluginManager
    implements DisposableBean, SpringPluginManager {

    private final Map<String, PluginStartingError> startingErrors = new HashMap<>();

    private final ApplicationContext rootContext;

    private final Lazy<ApplicationContext> sharedContext;

    public HaloPluginManager(Path pluginsRoot, ApplicationContext rootContext) {
        super(pluginsRoot);
        this.rootContext = rootContext;
        // We have to initialize share context lazily because the root context has not refreshed
        this.sharedContext = Lazy.of(() -> SharedApplicationContextFactory.create(rootContext));
    }

    @Override
    protected void initialize() {
        super.initialize();

        // add additional listener
        addPluginStateListener(new PluginStartedEventAdapter());
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
        return new SpringPluginFactory(contextFactory);
    }

    @Override
    protected PluginDescriptorFinder createPluginDescriptorFinder() {
        return new YamlPluginDescriptorFinder();
    }

    @Override
    protected PluginWrapper createPluginWrapper(PluginDescriptor pluginDescriptor, Path pluginPath,
        ClassLoader pluginClassLoader) {
        // create the plugin wrapper
        log.debug("Creating wrapper for plugin '{}'", pluginPath);
        var pluginWrapper =
            new HaloPluginWrapper(this, pluginDescriptor, pluginPath, pluginClassLoader);
        pluginWrapper.setPluginFactory(getPluginFactory());
        return pluginWrapper;
    }

    @Override
    protected PluginState stopPlugin(String pluginId, boolean stopDependents) {
        checkPluginId(pluginId);
        PluginWrapper pluginWrapper = getPlugin(pluginId);
        PluginDescriptor pluginDescriptor = pluginWrapper.getDescriptor();
        PluginState pluginState = pluginWrapper.getPluginState();
        if (PluginState.STOPPED == pluginState) {
            log.debug("Already stopped plugin '{}'", getPluginLabel(pluginDescriptor));
            return PluginState.STOPPED;
        }

        // test for disabled plugin
        if (PluginState.DISABLED == pluginState) {
            // do nothing
            return pluginState;
        }

        rootContext.publishEvent(new HaloPluginBeforeStopEvent(this, pluginWrapper));

        if (stopDependents) {
            List<String> dependents = dependencyResolver.getDependents(pluginId);
            while (!dependents.isEmpty()) {
                String dependent = dependents.remove(0);
                stopPlugin(dependent, false);
                dependents.addAll(0, dependencyResolver.getDependents(dependent));
            }
        }

        log.info("Stop plugin '{}'", getPluginLabel(pluginDescriptor));
        pluginWrapper.getPlugin().stop();
        pluginWrapper.setPluginState(PluginState.STOPPED);
        // release plugin resources
        releaseAdditionalResources(pluginId);

        startedPlugins.remove(pluginWrapper);

        rootContext.publishEvent(new HaloPluginStoppedEvent(this, pluginWrapper));
        firePluginStateEvent(new PluginStateEvent(this, pluginWrapper, pluginState));

        return pluginWrapper.getPluginState();
    }

    @Override
    public void startPlugins() {
        throw new UnsupportedOperationException(
            "The operation of starting all plugins is not supported."
        );
    }

    @Override
    public PluginState startPlugin(String pluginId) {
        try {
            return super.startPlugin(pluginId);
        } catch (Throwable t) {
            // TODO Do not release additional resources here.
            // releaseAdditionalResources(pluginId);
            throw t;
        }
    }

    @Override
    public void stopPlugins() {
        doStopPlugins();
    }

    private void doStopPlugins() {
        startingErrors.clear();
        // stop started plugins in reverse order
        Collections.reverse(startedPlugins);
        Iterator<PluginWrapper> itr = startedPlugins.iterator();
        while (itr.hasNext()) {
            PluginWrapper pluginWrapper = itr.next();
            PluginState pluginState = pluginWrapper.getPluginState();
            if (PluginState.STARTED == pluginState) {
                try {
                    rootContext.publishEvent(
                        new HaloPluginBeforeStopEvent(this, pluginWrapper));
                    log.info("Stop plugin '{}'", getPluginLabel(pluginWrapper.getDescriptor()));
                    if (pluginWrapper.getPlugin() != null) {
                        pluginWrapper.getPlugin().stop();
                    }
                    pluginWrapper.setPluginState(PluginState.STOPPED);
                    itr.remove();
                    releaseAdditionalResources(pluginWrapper.getPluginId());

                    rootContext.publishEvent(
                        new HaloPluginStoppedEvent(this, pluginWrapper));
                    firePluginStateEvent(new PluginStateEvent(this, pluginWrapper, pluginState));
                } catch (PluginRuntimeException e) {
                    log.error(e.getMessage(), e);
                    startingErrors.put(pluginWrapper.getPluginId(), PluginStartingError.of(
                        pluginWrapper.getPluginId(), e.getMessage(), e.toString()));
                }
            }
        }
    }

    /**
     * Release plugin holding release on stop.
     */
    public void releaseAdditionalResources(String pluginId) {
    }

    @Override
    protected PluginWrapper loadPluginFromPath(Path pluginPath) {
        PluginWrapper pluginWrapper = super.loadPluginFromPath(pluginPath);
        rootContext.publishEvent(new HaloPluginLoadedEvent(this, pluginWrapper));
        return pluginWrapper;
    }


    @Override
    public void destroy() throws Exception {
        stopPlugins();
    }

    @Override
    public ApplicationContext getRootContext() {
        return rootContext;
    }

    @Override
    public ApplicationContext getSharedContext() {
        return sharedContext.get();
    }

    private class PluginStartedEventAdapter implements PluginStateListener {

        @Override
        public void pluginStateChanged(PluginStateEvent event) {
            if (!PluginState.STARTED.equals(event.getPluginState())) {
                return;
            }
            // Indicate the state is started.
            var pluginWrapper = event.getPlugin();
            rootContext.publishEvent(new HaloPluginStartedEvent(event.getSource(), pluginWrapper));
        }
    }
}
