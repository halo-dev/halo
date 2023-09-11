package run.halo.app.plugin;

import lombok.extern.slf4j.Slf4j;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

/**
 * This class will be extended by all plugins and serve as the common class between a plugin and
 * the application.
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
public class BasePlugin extends Plugin {

    protected PluginContext context;

    @Deprecated
    public BasePlugin(PluginWrapper wrapper) {
        super(wrapper);
        log.info("Initialized plugin {}", wrapper.getPluginId());
    }

    /**
     * Constructor a plugin with the given plugin context.
     * TODO Mark {@link PluginContext} as final to prevent modification.
     *
     * @param pluginContext plugin context must not be null.
     */
    public BasePlugin(PluginContext pluginContext) {
        this.context = pluginContext;
    }

    /**
     * use {@link #BasePlugin(PluginContext)} instead of.
     *
     * @deprecated since 2.10.0
     */
    public BasePlugin() {
    }

    /**
     * Compatible with old constructors, if the plugin is not use
     * {@link #BasePlugin(PluginContext)} constructor then base plugin factory will use this
     * method to set plugin context.
     *
     * @param context plugin context must not be null.
     */
    final void setContext(PluginContext context) {
        Assert.notNull(context, "Plugin context must not be null");
        this.context = context;
    }

    @NonNull
    public PluginContext getContext() {
        return context;
    }
}
