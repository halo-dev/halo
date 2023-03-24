package run.halo.app.plugin.event;

import org.pf4j.PluginWrapper;
import org.springframework.context.ApplicationEvent;

/**
 * @author guqing
 * @since 2.0.0
 */
public class HaloPluginLoadedEvent extends ApplicationEvent {
    private final PluginWrapper pluginWrapper;


    public HaloPluginLoadedEvent(Object source, PluginWrapper pluginWrapper) {
        super(source);
        this.pluginWrapper = pluginWrapper;
    }

    public PluginWrapper getPluginWrapper() {
        return pluginWrapper;
    }
}
