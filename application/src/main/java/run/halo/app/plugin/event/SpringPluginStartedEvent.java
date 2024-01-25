package run.halo.app.plugin.event;

import org.springframework.context.ApplicationEvent;
import run.halo.app.plugin.SpringPlugin;

public class SpringPluginStartedEvent extends ApplicationEvent {

    private final SpringPlugin springPlugin;

    public SpringPluginStartedEvent(Object source, SpringPlugin springPlugin) {
        super(source);
        this.springPlugin = springPlugin;
    }

    public SpringPlugin getSpringPlugin() {
        return springPlugin;
    }
}
