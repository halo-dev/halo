package run.halo.app.plugin;

import java.util.List;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.context.ApplicationContext;

public interface SpringPluginManager extends PluginManager {

    ApplicationContext getRootContext();

    ApplicationContext getSharedContext();

    /**
     * Get all dependents recursively.
     *
     * @param pluginId plugin id
     * @return a list of plugin wrapper. The order of the list is from the farthest dependent to
     * the nearest dependent.
     */
    List<PluginWrapper> getDependents(String pluginId);
}
