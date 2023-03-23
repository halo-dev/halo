package run.halo.app.plugin.event;

import org.pf4j.PluginWrapper;
import org.springframework.context.ApplicationEvent;

/**
 * This event will be published to <b>application context</b> once plugin is started.
 *
 * @author guqing
 */
public class HaloPluginStartedEvent extends ApplicationEvent {

    private final PluginWrapper plugin;


    public HaloPluginStartedEvent(Object source, PluginWrapper plugin) {
        super(source);
        this.plugin = plugin;
    }

    public PluginWrapper getPlugin() {
        return plugin;
    }
}
