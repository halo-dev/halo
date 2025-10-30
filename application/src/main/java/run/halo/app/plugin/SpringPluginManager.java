package run.halo.app.plugin;

import java.util.List;
import org.jspecify.annotations.NonNull;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.context.ApplicationContext;

/**
 * Plugin manager for Spring-based applications.
 *
 * @author johnniang
 * @since 2.12.0
 */
public interface SpringPluginManager extends PluginManager {

    /**
     * Gets the root application context.
     *
     * @return the root application context
     */
    @NonNull
    ApplicationContext getRootContext();

    /**
     * Get the shared application context among plugins.
     *
     * @return the shared application context
     */
    @NonNull
    ApplicationContext getSharedContext();

    /**
     * Get all dependents recursively.
     *
     * @param pluginId plugin id
     * @return a list of plugin wrapper. The order of the list is from the farthest dependent to
     * the nearest dependent.
     * @since 2.16.0
     */
    @NonNull
    List<PluginWrapper> getDependents(@NonNull String pluginId);
}
