package run.halo.app.plugin.event;

import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.springframework.context.ApplicationEvent;

/**
 * Plugin state changed event.
 *
 * @author guqing
 * @date 2021-11-06
 */
public class HaloPluginStateChangedEvent extends ApplicationEvent {

    private final PluginWrapper plugin;

    private final PluginState oldState;

    public HaloPluginStateChangedEvent(Object source, PluginWrapper wrapper, PluginState oldState) {
        super(source);
        this.plugin = wrapper;
        this.oldState = oldState;
    }

    public PluginWrapper getPlugin() {
        return plugin;
    }

    public PluginState getOldState() {
        return oldState;
    }

    public PluginState getState() {
        return this.plugin.getPluginState();
    }
}
