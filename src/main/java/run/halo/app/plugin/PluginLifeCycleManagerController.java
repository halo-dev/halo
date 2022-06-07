package run.halo.app.plugin;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginState;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Plugin manager controller.
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
@RestController
@RequestMapping("/plugins")
public class PluginLifeCycleManagerController {

    private final PluginService pluginService;
    private final HaloPluginManager pluginManager;

    public PluginLifeCycleManagerController(PluginService pluginService,
        HaloPluginManager pluginManager) {
        this.pluginService = pluginService;
        this.pluginManager = pluginManager;
    }

    @GetMapping
    public List<Plugin> list() {
        return pluginService.list();
    }

    @GetMapping("/{pluginName}/startup")
    public PluginState start(@PathVariable String pluginName) {
        return pluginManager.startPlugin(pluginName);
    }

    @GetMapping("/{pluginName}/stops")
    public PluginState stop(@PathVariable String pluginName) {
        return pluginManager.stopPlugin(pluginName);
    }
}
