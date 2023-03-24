package run.halo.app.plugin;

import lombok.extern.slf4j.Slf4j;
import org.pf4j.Plugin;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;

/**
 * This class will be extended by all plugins and serve as the common class between a plugin and
 * the application.
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
public class BasePlugin extends Plugin {

    public BasePlugin(PluginWrapper wrapper) {
        super(wrapper);
        log.info("Initialized plugin {}", wrapper.getPluginId());
    }

    private PluginManager getPluginManager() {
        return getWrapper().getPluginManager();
    }
}
