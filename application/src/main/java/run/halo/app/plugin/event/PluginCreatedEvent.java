package run.halo.app.plugin.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import run.halo.app.core.extension.Plugin;

/**
 * The {@link Plugin} created event.
 *
 * @author guqing
 * @since 2.0.0
 */
@Getter
public class PluginCreatedEvent extends ApplicationEvent {
    private final String pluginName;

    public PluginCreatedEvent(Object source, String pluginName) {
        super(source);
        this.pluginName = pluginName;
    }
}
