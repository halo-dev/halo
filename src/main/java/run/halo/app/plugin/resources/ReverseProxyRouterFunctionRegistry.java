package run.halo.app.plugin.resources;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.StampedLock;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.ReverseProxy;
import run.halo.app.plugin.ExtensionContextRegistry;
import run.halo.app.plugin.PluginApplicationContext;

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
    private final MultiValueMap<String, String> pluginIdReverseProxyMap =
        new LinkedMultiValueMap<>();

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
            List<String> reverseProxyNames = pluginIdReverseProxyMap.get(pluginId);
            if (reverseProxyNames != null && reverseProxyNames.contains(proxyName)) {
                return Mono.empty();
            }
            pluginIdReverseProxyMap.add(pluginId, proxyName);

            // Obtain plugin application context
            PluginApplicationContext pluginApplicationContext =
                ExtensionContextRegistry.getInstance().getByPluginId(pluginId);
            return reverseProxyRouterFunctionFactory.create(reverseProxy, pluginApplicationContext)
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
        List<String> names = pluginIdReverseProxyMap.get(pluginId);
        return names == null ? 0 : names.size();
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
            List<String> proxyNames = pluginIdReverseProxyMap.remove(pluginId);
            if (proxyNames == null) {
                return Mono.empty();
            }
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
            List<String> proxyNames = pluginIdReverseProxyMap.get(pluginId);
            if (proxyNames == null) {
                return Mono.empty();
            }
            proxyNames.remove(reverseProxyName);
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
