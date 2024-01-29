package run.halo.app.plugin;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

public interface SharedEventListenerRegistry {

    void register(ApplicationListener<ApplicationEvent> listener);

    void unregister(ApplicationListener<ApplicationEvent> listener);

}
