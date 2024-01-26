package run.halo.app.plugin;

import org.springframework.context.ApplicationContext;

public interface PluginApplicationContextFactory {

    /**
     * Create and refresh application context.
     *
     * @param pluginId plugin id
     * @return refresh application context for the plugin.
     */
    ApplicationContext create(String pluginId);

}
