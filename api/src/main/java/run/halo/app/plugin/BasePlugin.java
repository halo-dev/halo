package run.halo.app.plugin;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.Plugin;

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

    public BasePlugin() {
    }
}
