package run.halo.app.plugin;

import java.nio.file.Path;
import java.util.List;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.context.ApplicationContext;

public interface SpringPluginManager extends PluginManager {

    ApplicationContext getRootContext();

    ApplicationContext getSharedContext();

    /**
     * Reload the plugin and the plugins that depend on it.
     *
     * @param pluginId plugin id
     * @param loadLocation new load location
     * @return true if reload successfully, otherwise false
     */
    boolean reloadPlugin(String pluginId, Path loadLocation);

    /**
     * Get all dependents recursively.
     *
     * @param pluginId plugin id
     * @return a list of plugin wrapper. The order of the list is from the farthest dependent to
     * the nearest dependent.
     */
    List<PluginWrapper> getDependents(String pluginId);
}
