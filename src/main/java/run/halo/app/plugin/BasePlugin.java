package run.halo.app.plugin;

import lombok.extern.slf4j.Slf4j;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

/**
 * This class will be extended by all plugins and serve as the common class between a plugin and
 * the application.
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
public abstract class BasePlugin extends Plugin {

    private PluginApplicationContext applicationContext;

    public BasePlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    /**
     * <p>Lazy initialization plugin application context,
     * avoid being unable to get context when system start scan plugin.</p>
     * <p>The plugin application context is not created until the plug-in is started.</p>
     *
     * @return Plugin application context.
     */
    public final synchronized PluginApplicationContext getApplicationContext() {
        if (applicationContext == null) {
            applicationContext =
                getPluginManager().getPluginApplicationContext(this.wrapper.getPluginId());
        }
        return applicationContext;
    }

    public HaloPluginManager getPluginManager() {
        return (HaloPluginManager) getWrapper().getPluginManager();
    }
}
