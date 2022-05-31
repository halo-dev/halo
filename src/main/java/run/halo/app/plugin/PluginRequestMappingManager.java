package run.halo.app.plugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginWrapper;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Plugin mapping manager.
 *
 * @author guqing
 * @see RequestMappingHandlerMapping
 */
@Slf4j
public class PluginRequestMappingManager {

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    public PluginRequestMappingManager(
        RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    }

    public void registerControllers(PluginWrapper pluginWrapper) {
        String pluginId = pluginWrapper.getPluginId();
        getControllerBeans(pluginId)
            .forEach(this::registerController);
    }

    private void registerController(Object controller) {
        log.debug("Registering plugin request mapping for bean: [{}]", controller);
        Method detectHandlerMethods = ReflectionUtils.findMethod(RequestMappingHandlerMapping.class,
            "detectHandlerMethods", Object.class);
        if (detectHandlerMethods == null) {
            return;
        }
        try {
            detectHandlerMethods.setAccessible(true);
            detectHandlerMethods.invoke(requestMappingHandlerMapping, controller);
        } catch (IllegalStateException ise) {
            // ignore this
            log.warn(ise.getMessage());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            log.warn("invocation target exception: [{}]", e.getMessage(), e);
        }
    }

    private void unregisterControllerMappingInternal(Object controller) {
        requestMappingHandlerMapping.getHandlerMethods()
            .forEach((mapping, handlerMethod) -> {
                if (controller == handlerMethod.getBean()) {
                    log.debug("Removed plugin request mapping [{}] from bean [{}]", mapping,
                        controller);
                    requestMappingHandlerMapping.unregisterMapping(mapping);
                }
            });
    }

    public void removeControllerMapping(String pluginId) {
        getControllerBeans(pluginId)
            .forEach(this::unregisterControllerMappingInternal);
    }

    public Collection<Object> getControllerBeans(String pluginId) {
        GenericApplicationContext pluginContext =
            ExtensionContextRegistry.getInstance().getByPluginId(pluginId);
        return pluginContext.getBeansWithAnnotation(Controller.class).values();
    }
}
