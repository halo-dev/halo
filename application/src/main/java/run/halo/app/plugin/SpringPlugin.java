package run.halo.app.plugin;

import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationContext;

/**
 * Spring based plugin.
 *
 * @author johnniang
 * @since 2.22.0
 */
public interface SpringPlugin {

    /**
     * Gets plugin context.
     *
     * @return plugin context
     */
    @NonNull
    PluginContext getPluginContext();

    /**
     * Gets application context of the plugin.
     *
     * @return application context of the plugin
     */
    @NonNull
    ApplicationContext getApplicationContext();

}
