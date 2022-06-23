package run.halo.app.plugin;

import java.util.List;
import run.halo.app.core.extension.Plugin;

/**
 * Service for plugin.
 *
 * @author guqing
 * @since 2.0.0
 */
public interface PluginService {

    /**
     * Lists plugin from repository.
     *
     * @return all loaded plugins.
     */
    List<Plugin> list();

    /**
     * Start the plugin according to the plugin name.
     *
     * @param pluginName plugin name
     * @return plugin custom resource
     */
    Plugin startup(String pluginName);

    Plugin stop(String pluginName);

    /**
     * Gets {@link Plugin} by plugin name.
     *
     * @param pluginName plugin name
     * @return plugin custom resource
     */
    Plugin getByName(String pluginName);
}
