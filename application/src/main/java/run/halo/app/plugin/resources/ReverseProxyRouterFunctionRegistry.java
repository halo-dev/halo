package run.halo.app.plugin.resources;

import com.google.common.collect.LinkedHashMultimap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.StampedLock;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.core.extension.ReverseProxy;
import run.halo.app.plugin.PluginRouterFunctionRegistry;

/**
 * A registry for {@link RouterFunction} of plugin.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class ReverseProxyRouterFunctionRegistry {

    private final PluginRouterFunctionRegistry pluginRouterFunctionRegistry;

    private final ReverseProxyRouterFunctionFactory reverseProxyRouterFunctionFactory;
    private final StampedLock lock = new StampedLock();
    private final Map<String, RouterFunction<ServerResponse>> proxyNameRouterFunctionRegistry =
        new HashMap<>();
    private final LinkedHashMultimap<String, String> pluginIdReverseProxyMap =
        LinkedHashMultimap.create();

    public ReverseProxyRouterFunctionRegistry(
        PluginRouterFunctionRegistry pluginRouterFunctionRegistry,
        ReverseProxyRouterFunctionFactory reverseProxyRouterFunctionFactory) {
        this.pluginRouterFunctionRegistry = pluginRouterFunctionRegistry;
        this.reverseProxyRouterFunctionFactory = reverseProxyRouterFunctionFactory;
    }

    /**
     * Register reverse proxy router function.
     *
     * @param pluginId plugin id
     * @param reverseProxy reverse proxy
     */
    public void register(String pluginId, ReverseProxy reverseProxy) {
        Assert.notNull(pluginId, "The plugin id must not be null.");
        final String proxyName = reverseProxy.getMetadata().getName();
        long stamp = lock.writeLock();
        try {
            pluginIdReverseProxyMap.put(pluginId, proxyName);
            var routerFunction = reverseProxyRouterFunctionFactory.create(reverseProxy, pluginId);
            if (routerFunction != null) {
                proxyNameRouterFunctionRegistry.put(proxyName, routerFunction);
                pluginRouterFunctionRegistry.register(Set.of(routerFunction));
            }
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    /**
     * Only for test.
     */
    int reverseProxySize(String pluginId) {
        Set<String> names = pluginIdReverseProxyMap.get(pluginId);
        return names.size();
    }

    /**
     * Remove reverse proxy router function by pluginId and reverse proxy name.
     */
    public void remove(String pluginId, String reverseProxyName) {
        long stamp = lock.writeLock();
        try {
            pluginIdReverseProxyMap.remove(pluginId, reverseProxyName);
            var removedRouterFunction = proxyNameRouterFunctionRegistry.remove(reverseProxyName);
            if (removedRouterFunction != null) {
                pluginRouterFunctionRegistry.unregister(Set.of(removedRouterFunction));
            }
        } finally {
            lock.unlockWrite(stamp);
        }
    }

}
