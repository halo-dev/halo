package run.halo.app.plugin.resources;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.pf4j.PluginRuntimeException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultSingletonBeanRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.function.server.support.RouterFunctionMapping;

/**
 * Provides methods for registering {@link RouterFunction} and destroying them.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class RouterFunctionBeanFactory implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext)
        throws BeansException {
        this.applicationContext = applicationContext;
    }

    public DefaultSingletonBeanRegistry getBeanRegistry() {
        AbstractApplicationContext context = (AbstractApplicationContext) applicationContext;
        return (DefaultSingletonBeanRegistry) context.getBeanFactory();
    }

    private void registerRouterFunctionBean(String name, Object existingBean) {
        DefaultSingletonBeanRegistry beanRegistry = getBeanRegistry();
        if (beanRegistry.containsSingleton(name)) {
            beanRegistry.destroySingleton(name);
        }
        beanRegistry.registerSingleton(name, existingBean);
    }

    public void create(String beanName, RouterFunction<ServerResponse> routerFunction) {
        if (routerFunction == null) {
            return;
        }
        registerRouterFunctionBean(beanName, routerFunction);
        refreshRouterFunctionMapping();
    }

    public void destroyRouterFunction(String beanName) {
        getBeanRegistry().destroySingleton(beanName);
        refreshRouterFunctionMapping();
    }

    // TODO Use the RouterFunctionMapping unique to the plugin instead of reflection
    private void refreshRouterFunctionMapping() {
        RouterFunctionMapping routerFunctionMapping =
            applicationContext.getBean(RouterFunctionMapping.class);
        try {
            Method initRouterFunctions =
                routerFunctionMapping.getClass()
                    .getDeclaredMethod("initRouterFunctions");
            initRouterFunctions.setAccessible(true);
            initRouterFunctions.invoke(routerFunctionMapping);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new PluginRuntimeException(e);
        }
    }
}
