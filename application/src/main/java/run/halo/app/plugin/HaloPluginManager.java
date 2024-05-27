package run.halo.app.plugin;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.CompoundPluginLoader;
import org.pf4j.CompoundPluginRepository;
import org.pf4j.DefaultPluginManager;
import org.pf4j.DefaultPluginRepository;
import org.pf4j.ExtensionFactory;
import org.pf4j.ExtensionFinder;
import org.pf4j.JarPluginLoader;
import org.pf4j.JarPluginRepository;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginDescriptorFinder;
import org.pf4j.PluginFactory;
import org.pf4j.PluginLoader;
import org.pf4j.PluginRepository;
import org.pf4j.PluginStatusProvider;
import org.pf4j.PluginWrapper;
import org.springframework.context.ApplicationContext;
import org.springframework.data.util.Lazy;
import run.halo.app.infra.SystemVersionSupplier;

/**
 * PluginManager to hold the main ApplicationContext.
 * It provides methods for managing the plugin lifecycle.
 *
 * @author guqing
 * @author johnniang
 * @since 2.0.0
 */
@Slf4j
public class HaloPluginManager extends DefaultPluginManager implements SpringPluginManager {

    private final ApplicationContext rootContext;

    private final Lazy<ApplicationContext> sharedContext;

    private final PluginProperties pluginProperties;

    public HaloPluginManager(ApplicationContext rootContext,
        PluginProperties pluginProperties,
        SystemVersionSupplier systemVersionSupplier) {
        this.pluginProperties = pluginProperties;
        this.rootContext = rootContext;
        // We have to initialize share context lazily because the root context has not refreshed
        this.sharedContext = Lazy.of(() -> SharedApplicationContextFactory.create(rootContext));
        super.runtimeMode = pluginProperties.getRuntimeMode();

        setExactVersionAllowed(pluginProperties.isExactVersionAllowed());
        setSystemVersion(systemVersionSupplier.get().getNormalVersion());

        super.initialize();
    }

    @Override
    protected void initialize() {
        // Leave the implementation empty because the super#initialize eagerly initializes
        // components before properties set.
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
        return super.createPluginStatusProvider();
    }

    @Override
    protected PluginRepository createPluginRepository() {
        var developmentPluginRepository =
            new DefaultDevelopmentPluginRepository(getPluginsRoots());
        developmentPluginRepository
            .setFixedPaths(pluginProperties.getFixedPluginPath());
        return new CompoundPluginRepository()
            .add(developmentPluginRepository, this::isDevelopment)
            .add(new JarPluginRepository(getPluginsRoots()))
            .add(new DefaultPluginRepository(getPluginsRoots()));
    }

    @Override
    protected List<Path> createPluginsRoot() {
        var pluginsRoot = pluginProperties.getPluginsRoot();
        if (StringUtils.isNotBlank(pluginsRoot)) {
            return List.of(Paths.get(pluginsRoot));
        }
        return super.createPluginsRoot();
    }

    @Override
    public void startPlugins() {
        throw new UnsupportedOperationException(
            "The operation of starting all plugins is not supported."
        );
    }

    @Override
    public void stopPlugins() {
        throw new UnsupportedOperationException(
            "The operation of stopping all plugins is not supported."
        );
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
}
