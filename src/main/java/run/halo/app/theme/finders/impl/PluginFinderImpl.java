package run.halo.app.theme.finders.impl;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
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

    @Override
    public boolean available(String pluginName) {
        if (StringUtils.isBlank(pluginName)) {
            return false;
        }
        PluginWrapper pluginWrapper = haloPluginManager.getPlugin(pluginName);
        if (pluginWrapper == null) {
            return false;
        }
        return PluginState.STARTED.equals(pluginWrapper.getPluginState());
    }
}
