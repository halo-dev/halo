package run.halo.app.plugin;

import lombok.extern.slf4j.Slf4j;
import org.pf4j.Plugin;
import org.springframework.lang.NonNull;

/**
 * This class will be extended by all plugins and serve as the common class between a plugin and
 * the application.
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
public class BasePlugin extends Plugin {

    protected final PluginContext context;

    /**
     * Constructor a plugin with the given plugin context.
     *
     * @param pluginContext plugin context must not be null.
     */
    public BasePlugin(PluginContext pluginContext) {
        this.context = pluginContext;
    }

    @NonNull
    public PluginContext getContext() {
        return context;
    }
}
