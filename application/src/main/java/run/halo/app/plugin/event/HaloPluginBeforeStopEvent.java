package run.halo.app.plugin.event;

import org.pf4j.PluginWrapper;
import org.springframework.context.ApplicationEvent;

/**
 * @author guqing
 * @since 2.0.0
 */
public class HaloPluginBeforeStopEvent extends ApplicationEvent {
    private final PluginWrapper plugin;

    public HaloPluginBeforeStopEvent(Object source, PluginWrapper plugin) {
        super(source);
        this.plugin = plugin;
    }

    public PluginWrapper getPlugin() {
        return plugin;
    }
}
