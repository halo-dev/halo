package run.halo.app.plugin;

import java.lang.reflect.AnnotatedElement;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginWrapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
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
@ConditionalOnClass(HaloPluginManager.class)
public class PluginApplicationEventBridgeDispatcher
    implements ApplicationListener<ApplicationEvent> {

    private final HaloPluginManager haloPluginManager;

    public PluginApplicationEventBridgeDispatcher(HaloPluginManager haloPluginManager) {
        this.haloPluginManager = haloPluginManager;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (!isSharedEventAnnotationPresent(event.getClass())) {
            return;
        }

        for (PluginWrapper startedPlugin : haloPluginManager.getStartedPlugins()) {
            PluginApplicationContext pluginApplicationContext =
                haloPluginManager.getPluginApplicationContext(startedPlugin.getPluginId());
            log.debug("Bridging broadcast event [{}] to plugin [{}]", event,
                startedPlugin.getPluginId());
            pluginApplicationContext.publishEvent(event);
        }
    }

    private boolean isSharedEventAnnotationPresent(AnnotatedElement annotatedElement) {
        return AnnotationUtils.findAnnotation(annotatedElement, SharedEvent.class) != null;
    }
}
