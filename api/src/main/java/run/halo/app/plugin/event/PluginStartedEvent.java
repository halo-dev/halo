package run.halo.app.plugin.event;

import org.springframework.context.ApplicationEvent;

/**
 * The event that is published when a plugin is really started, and is only for plugin internal use.
 *
 * @author johnniang
 * @since 2.17.0
 */
public class PluginStartedEvent extends ApplicationEvent {

    public PluginStartedEvent(Object source) {
        super(source);
    }

}
