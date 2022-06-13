package run.halo.app.plugin.resources;

import org.pf4j.PluginWrapper;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.plugin.ExtensionContextRegistry;
import run.halo.app.plugin.PluginApplicationContext;

/**
 * Router registry for plugin reverse proxy rules.
 * Provide registration and deletion methods.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class ReverseProxyRouterRegistry {
    private final RouterFunctionBeanFactory routerBeanFactory;
    private final ReverseProxyRouterFunctionFactory reverseProxyRouterFunctionFactory;

    public ReverseProxyRouterRegistry(RouterFunctionBeanFactory routerBeanFactory,
        ReverseProxyRouterFunctionFactory reverseProxyRouterFunctionFactory) {
        this.routerBeanFactory = routerBeanFactory;
        this.reverseProxyRouterFunctionFactory = reverseProxyRouterFunctionFactory;
    }

    public void register(PluginWrapper plugin) {
        PluginApplicationContext pluginApplicationContext =
            ExtensionContextRegistry.getInstance().getByPluginId(plugin.getPluginId());
        RouterFunction<ServerResponse> routerFunction =
            reverseProxyRouterFunctionFactory.create(pluginApplicationContext);
        routerBeanFactory.create(buildRouteFunctionBeanName(plugin.getPluginId()), routerFunction);
    }

    public void remove(PluginWrapper plugin) {
        routerBeanFactory.destroyRouterFunction(buildRouteFunctionBeanName(plugin.getPluginId()));
    }

    private String buildRouteFunctionBeanName(String pluginName) {
        return pluginName + "ReverseProxyRouteFunction";
    }
}
