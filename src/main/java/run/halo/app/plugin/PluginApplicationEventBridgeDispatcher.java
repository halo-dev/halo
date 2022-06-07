package run.halo.app.plugin;

import java.lang.reflect.AnnotatedElement;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

/**
 * <p>The main application bridges the events marked with {@code SharedEvent} annotation to the
 * enabled plug-in so that it can be listened by the plugin.</p>
 *
 * @author guqing
 * @see SharedEvent
 * @see PluginApplicationContext
 * @since 2.0.0
 */
@Slf4j
@Component
public class PluginApplicationEventBridgeDispatcher
    implements ApplicationListener<ApplicationEvent> {

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (!isSharedEventAnnotationPresent(event.getClass())) {
            return;
        }
        List<PluginApplicationContext> pluginApplicationContexts =
            ExtensionContextRegistry.getInstance().getPluginApplicationContexts();
        for (PluginApplicationContext pluginApplicationContext : pluginApplicationContexts) {
            log.debug("Bridging broadcast event [{}] to plugin [{}]", event,
                pluginApplicationContext.getPluginId());
            pluginApplicationContext.publishEvent(event);
        }
    }

    private boolean isSharedEventAnnotationPresent(AnnotatedElement annotatedElement) {
        return AnnotationUtils.findAnnotation(annotatedElement, SharedEvent.class) != null;
    }
}
