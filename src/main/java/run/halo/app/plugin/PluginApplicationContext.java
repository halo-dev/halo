package run.halo.app.plugin;

import java.lang.reflect.Field;
import java.util.Set;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * The generic IOC container for plugins.
 * The plugin-classes loaded through the same plugin-classloader will be put into the same
 * {@link PluginApplicationContext} for bean creation.
 *
 * @author guqing
 * @since 2.0.0
 */
public class PluginApplicationContext extends GenericApplicationContext {

    private String pluginId;

    /**
     * <p>覆盖父类方法中判断context parent不为空时使用parent context广播事件的逻辑。
     * 如果主应用桥接事件到插件中且设置了parent会导致发布事件时死循环.</p>
     *
     * @param event the event to publish (may be an {@link ApplicationEvent} or a payload object
     * to be turned into a {@link PayloadApplicationEvent})
     * @param eventType the resolved event type, if known
     */
    @Override
    protected void publishEvent(@NonNull Object event, @Nullable ResolvableType eventType) {
        Assert.notNull(event, "Event must not be null");

        // Decorate event as an ApplicationEvent if necessary
        ApplicationEvent applicationEvent;
        if (event instanceof ApplicationEvent) {
            applicationEvent = (ApplicationEvent) event;
        } else {
            applicationEvent = new PayloadApplicationEvent<>(this, event);
            if (eventType == null) {
                eventType = ((PayloadApplicationEvent<?>) applicationEvent).getResolvableType();
            }
        }

        // Multicast right now if possible - or lazily once the multicaster is initialized
        Set<ApplicationEvent> earlyApplicationEvents = getEarlyApplicationEvents();
        if (earlyApplicationEvents != null) {
            earlyApplicationEvents.add(applicationEvent);
        } else {
            getApplicationEventMulticaster().multicastEvent(applicationEvent, eventType);
        }
    }

    private ApplicationEventMulticaster getApplicationEventMulticaster() {
        ConfigurableListableBeanFactory beanFactory = getBeanFactory();
        return beanFactory.getBean(APPLICATION_EVENT_MULTICASTER_BEAN_NAME,
            ApplicationEventMulticaster.class);
    }

    @SuppressWarnings("unchecked")
    protected Set<ApplicationEvent> getEarlyApplicationEvents() {
        try {
            Field earlyApplicationEventsField =
                AbstractApplicationContext.class.getDeclaredField("earlyApplicationEvents");
            return (Set<ApplicationEvent>) earlyApplicationEventsField.get(this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // ignore this exception
            return null;
        }
    }

    public String getPluginId() {
        return pluginId;
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }
}
