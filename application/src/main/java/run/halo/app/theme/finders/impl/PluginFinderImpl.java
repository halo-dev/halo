package run.halo.app.theme.finders.impl;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.PluginManager;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.springframework.util.Assert;
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
    private final PluginManager pluginManager;

    @Override
    public boolean available(String pluginName) {
        if (StringUtils.isBlank(pluginName)) {
            return false;
        }
        PluginWrapper pluginWrapper = pluginManager.getPlugin(pluginName);
        if (pluginWrapper == null) {
            return false;
        }
        return PluginState.STARTED.equals(pluginWrapper.getPluginState());
    }

    @Override
    public boolean available(String pluginName, String requiresVersion) {
        Assert.notNull(requiresVersion, "Requires version must not be null.");
        if (!this.available(pluginName)) {
            return false;
        }
        var pluginWrapper = pluginManager.getPlugin(pluginName);
        var pluginVersion = pluginWrapper.getDescriptor().getVersion();
        return pluginManager.getVersionManager()
            .checkVersionConstraint(pluginVersion, requiresVersion);
    }
}
