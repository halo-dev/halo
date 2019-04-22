package run.halo.app.event;

import cn.hutool.core.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import run.halo.app.utils.ReflectionUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Application listener manager.
 *
 * @author johnniang
 * @date 19-4-21
 */
@Slf4j
@Deprecated
public class ApplicationListenerManager {

    /**
     * Listener Map.
     */
    private final Map<String, List<EventListener>> listenerMap = new ConcurrentHashMap<>();

    public ApplicationListenerManager(ApplicationContext applicationContext) {
        // TODO Need to refactor
        // Register all listener on starting up
        applicationContext.getBeansOfType(ApplicationListener.class).values().forEach(this::register);

        log.debug("Initialized event listeners");
    }

    public List<EventListener> getListeners(@Nullable Object event) {
        if (event == null) {
            return Collections.emptyList();
        }

        // Get listeners
        List<EventListener> listeners = listenerMap.get(event.getClass().getTypeName());
        // Clone the listeners
        return listeners == null ? Collections.emptyList() : new LinkedList<>(listeners);
    }

    public synchronized void register(@NonNull ApplicationListener<?> listener) {
        // Get actual generic type
        Type actualType = resolveActualGenericType(listener);

        if (actualType == null) {
            return;
        }

        // Add this listener
        listenerMap.computeIfAbsent(actualType.getTypeName(), (key) -> new LinkedList<>()).add(listener);
    }

    public synchronized void unRegister(@NonNull ApplicationListener<?> listener) {
        // Get actual generic type
        Type actualType = resolveActualGenericType(listener);

        if (actualType == null) {
            return;
        }

        // Remove it from listener map
        listenerMap.getOrDefault(actualType.getTypeName(), Collections.emptyList()).remove(listener);
    }

    @Nullable
    private Type resolveActualGenericType(@NonNull ApplicationListener<?> listener) {
        Assert.notNull(listener, "Application listener must not be null");

        log.debug("Attempting to resolve type of Application listener: [{}]", listener);

        ParameterizedType parameterizedType = ReflectionUtils.getParameterizedType(ApplicationListener.class, ((ApplicationListener) listener).getClass());

        return parameterizedType == null ? null : parameterizedType.getActualTypeArguments()[0];
    }
}
