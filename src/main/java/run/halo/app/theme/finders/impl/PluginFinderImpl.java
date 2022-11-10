package run.halo.app.theme.finders.impl;

import java.util.Optional;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import run.halo.app.core.extension.Plugin;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.plugin.HaloPluginManager;
import run.halo.app.theme.finders.Finder;
import run.halo.app.theme.finders.PluginFinder;

/**
 * Plugin finder implementation.
 *
 * @author guqing
 * @since 2.0.0
 */
@Finder("pluginFinder")
@AllArgsConstructor
public class PluginFinderImpl implements PluginFinder {
    private final HaloPluginManager haloPluginManager;
    private final ReactiveExtensionClient client;

    @Override
    public boolean enabled(String pluginName) {
        if (StringUtils.isBlank(pluginName)) {
            return false;
        }
        PluginWrapper pluginWrapper = haloPluginManager.getPlugin(pluginName);
        if (pluginWrapper == null) {
            return false;
        }
        return PluginState.STARTED.equals(pluginWrapper.getPluginState());
    }

    @Override
    public boolean hasInstalled(String pluginName) {
        if (StringUtils.isBlank(pluginName)) {
            return false;
        }
        Optional<Plugin> plugin = client.fetch(Plugin.class, pluginName).blockOptional();
        return plugin.isPresent();
    }
}
