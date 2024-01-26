package run.halo.app.plugin;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class DefaultSharedEventListenerRegistry implements
    ApplicationListener<ApplicationEvent>, SharedEventListenerRegistry {

    private final List<ApplicationListener<ApplicationEvent>> listeners;

    public DefaultSharedEventListenerRegistry() {
        listeners = new CopyOnWriteArrayList<>();
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (!event.getClass().isAnnotationPresent(SharedEvent.class)) {
            return;
        }
        listeners.forEach(listener -> listener.onApplicationEvent(event));
    }

    public void register(ApplicationListener<ApplicationEvent> listener) {
        this.listeners.add(listener);
    }

    public void unregister(ApplicationListener<ApplicationEvent> listener) {
        this.listeners.remove(listener);
    }
}
