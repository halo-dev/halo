package run.halo.app.plugin;

import lombok.Getter;
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
@Getter
@Slf4j
public class BasePlugin extends Plugin {

    protected PluginContext context;

    /**
     * Constructor a plugin with the given plugin context.
     *
     * @param pluginContext plugin context must not be null.
     */
    public BasePlugin(PluginContext pluginContext) {
        this.context = pluginContext;
    }

    @Deprecated(since = "2.7.0", forRemoval = true)
    public BasePlugin(PluginWrapper wrapper) {
        super(wrapper);
        log.warn("Deprecated constructor 'BasePlugin(PluginWrapper wrapper)' called, please use "
                + "'BasePlugin(PluginContext pluginContext)' instead for plugin '{}',This "
                + "constructor will be removed in 2.19.0",
            wrapper.getPluginId());
    }

    public BasePlugin() {
    }
}
