package run.halo.app.plugin;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.Lifecycle;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SharedEventDispatcher {

    private final SpringPluginManager pluginManager;

    private final ApplicationEventPublisher publisher;

    @EventListener(ApplicationEvent.class)
    void onApplicationEvent(ApplicationEvent event) {
        if (AnnotationUtils.findAnnotation(event.getClass(), SharedEvent.class) == null) {
            return;
        }
        // we should copy the plugins list to avoid ConcurrentModificationException
        var startedPlugins = pluginManager.startedPlugins();
        // broadcast event to all started plugins except the publisher
        for (var startedPlugin : startedPlugins) {
            var plugin = startedPlugin.getPlugin();
            if (!(plugin instanceof SpringPlugin springPlugin)) {
                continue;
            }
            var context = springPlugin.getApplicationContext();
            // make sure the context is running before publishing the event
            if (context instanceof Lifecycle lifecycle && lifecycle.isRunning()) {
                context.publishEvent(new HaloSharedEventDelegator(this, event));
            }
        }
    }

    @EventListener(PluginSharedEventDelegator.class)
    void onApplicationEvent(PluginSharedEventDelegator event) {
        publisher.publishEvent(event.getDelegate());
    }

}
