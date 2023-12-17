package run.halo.app.plugin.resources;

import com.google.common.collect.LinkedHashMultimap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.StampedLock;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.ReverseProxy;

/**
 * A registry for {@link RouterFunction} of plugin.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class ReverseProxyRouterFunctionRegistry {
    private final ReverseProxyRouterFunctionFactory reverseProxyRouterFunctionFactory;
    private final StampedLock lock = new StampedLock();
    private final Map<String, RouterFunction<ServerResponse>> proxyNameRouterFunctionRegistry =
        new HashMap<>();
    private final LinkedHashMultimap<String, String> pluginIdReverseProxyMap =
        LinkedHashMultimap.create();

    public ReverseProxyRouterFunctionRegistry(
        ReverseProxyRouterFunctionFactory reverseProxyRouterFunctionFactory) {
        this.reverseProxyRouterFunctionFactory = reverseProxyRouterFunctionFactory;
    }

    /**
     * Register reverse proxy router function.
     *
     * @param pluginId plugin id
     * @param reverseProxy reverse proxy
     * @return a mono
     */
    public Mono<Void> register(String pluginId, ReverseProxy reverseProxy) {
        Assert.notNull(pluginId, "The plugin id must not be null.");
        final String proxyName = reverseProxy.getMetadata().getName();
        long stamp = lock.writeLock();
        try {
            pluginIdReverseProxyMap.put(pluginId, proxyName);
            return reverseProxyRouterFunctionFactory.create(reverseProxy, pluginId)
                .map(routerFunction -> {
                    proxyNameRouterFunctionRegistry.put(proxyName, routerFunction);
                    return routerFunction;
                })
                .then();
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    /**
     * Only for test.
     */
    protected int reverseProxySize(String pluginId) {
        Set<String> names = pluginIdReverseProxyMap.get(pluginId);
        return names.size();
    }

    /**
     * Remove reverse proxy router function by plugin id.
     *
     * @param pluginId plugin id
     */
    public Mono<Void> remove(String pluginId) {
        Assert.notNull(pluginId, "The plugin id must not be null.");
        long stamp = lock.writeLock();
        try {
            Set<String> proxyNames = pluginIdReverseProxyMap.removeAll(pluginId);
            for (String proxyName : proxyNames) {
                proxyNameRouterFunctionRegistry.remove(proxyName);
            }
            return Mono.empty();
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    /**
     * Remove reverse proxy router function by pluginId and reverse proxy name.
     */
    public Mono<Void> remove(String pluginId, String reverseProxyName) {
        long stamp = lock.writeLock();
        try {
            pluginIdReverseProxyMap.remove(pluginId, reverseProxyName);
            proxyNameRouterFunctionRegistry.remove(reverseProxyName);
            return Mono.empty();
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    /**
     * Gets reverse proxy {@link RouterFunction} by reverse proxy name.
     */
    public RouterFunction<ServerResponse> getRouterFunction(String proxyName) {
        long stamp = lock.readLock();
        try {
            return proxyNameRouterFunctionRegistry.get(proxyName);
        } finally {
            lock.unlockRead(stamp);
        }
    }

    /**
     * Gets all reverse proxy {@link RouterFunction}.
     */
    public List<RouterFunction<ServerResponse>> getRouterFunctions() {
        long stamp = lock.readLock();
        try {
            return List.copyOf(proxyNameRouterFunctionRegistry.values());
        } finally {
            lock.unlockRead(stamp);
        }
    }
}
