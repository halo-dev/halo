package run.halo.app.plugin;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.halo.app.core.extension.Plugin;

/**
 * Plugin manager controller.
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
@RestController
// TODO Optimize prefix configuration
@RequestMapping("/apis/plugin.halo.run/v1alpha1/plugins")
public class PluginLifeCycleManagerController {

    private final PluginService pluginService;

    public PluginLifeCycleManagerController(PluginService pluginService) {
        this.pluginService = pluginService;
    }

    @GetMapping
    public List<Plugin> list() {
        return pluginService.list();
    }

    @PutMapping("/{pluginName}/startup")
    public Plugin start(@PathVariable String pluginName) {
        return pluginService.startup(pluginName);
    }

    @PutMapping("/{pluginName}/stop")
    public Plugin stop(@PathVariable String pluginName) {
        return pluginService.stop(pluginName);
    }
}
