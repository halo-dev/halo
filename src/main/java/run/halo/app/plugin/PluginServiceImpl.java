package run.halo.app.plugin;

import java.util.List;
import org.pf4j.PluginState;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import run.halo.app.core.extension.Plugin;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.plugin.resources.JsBundleRuleProvider;
import run.halo.app.plugin.resources.ReverseProxyRouterFunctionFactory;

/**
 * Default implementation of {@link PluginService}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Service
public class PluginServiceImpl implements PluginService {

    private final ExtensionClient extensionClient;

    private final HaloPluginManager haloPluginManager;

    private final JsBundleRuleProvider jsBundleRule;

    public PluginServiceImpl(ExtensionClient extensionClient,
        HaloPluginManager haloPluginManager, JsBundleRuleProvider jsBundleRule) {
        this.extensionClient = extensionClient;
        this.haloPluginManager = haloPluginManager;
        this.jsBundleRule = jsBundleRule;
    }

    /**
     * list all plugins including loaded and unloaded.
     *
     * @return plugin info
     */
    public List<Plugin> list() {
        return extensionClient.list(Plugin.class, null, null);
    }

    @Override
    public Plugin startup(String pluginName) {
        Assert.notNull(pluginName, "The pluginName must not be null.");
        PluginState pluginState = haloPluginManager.startPlugin(pluginName);
        Plugin plugin = getByName(pluginName);
        Plugin.PluginStatus status = plugin.getStatus();
        if (status == null) {
            status = new Plugin.PluginStatus();
        }
        status.setStatus(pluginState);

        // TODO Check whether the JS bundle rule exists. If it does not exist, do not populate
        // populate stylesheet path
        String jsBundleRoute = ReverseProxyRouterFunctionFactory.buildRoutePath(pluginName,
            jsBundleRule.jsRule(pluginName));
        String cssBundleRoute = ReverseProxyRouterFunctionFactory.buildRoutePath(pluginName,
            jsBundleRule.cssRule(pluginName));
        status.setEntry(jsBundleRoute);
        status.setStylesheet(cssBundleRoute);
        extensionClient.update(plugin);
        return plugin;
    }

    @Override
    public Plugin stop(String pluginName) {
        Assert.notNull(pluginName, "The pluginName must not be null.");
        PluginState pluginState = haloPluginManager.stopPlugin(pluginName);
        Plugin plugin = getByName(pluginName);
        plugin.getStatus().setStatus(pluginState);
        return plugin;
    }

    public Plugin getByName(String pluginName) {
        Assert.notNull(pluginName, "The pluginName must not be null.");
        return extensionClient.fetch(Plugin.class, pluginName)
            .orElseThrow(() ->
                new PluginNotFoundException(String.format("Plugin [%s] not found", pluginName)));
    }
}
