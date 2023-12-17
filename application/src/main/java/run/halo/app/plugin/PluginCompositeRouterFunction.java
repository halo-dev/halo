package run.halo.app.plugin;

import static run.halo.app.plugin.ExtensionContextRegistry.getInstance;

import com.google.common.collect.Iterables;
import java.util.List;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.resources.ReverseProxyRouterFunctionRegistry;

/**
 * A composite {@link RouterFunction} implementation for plugin.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class PluginCompositeRouterFunction implements RouterFunction<ServerResponse> {

    private final ReverseProxyRouterFunctionRegistry reverseProxyRouterFunctionFactory;

    public PluginCompositeRouterFunction(
        ReverseProxyRouterFunctionRegistry reverseProxyRouterFunctionFactory) {
        this.reverseProxyRouterFunctionFactory = reverseProxyRouterFunctionFactory;
    }

    @Override
    @NonNull
    public Mono<HandlerFunction<ServerResponse>> route(@NonNull ServerRequest request) {
        return Flux.fromIterable(routerFunctions())
            .concatMap(routerFunction -> routerFunction.route(request))
            .next();
    }

    @Override
    public void accept(@NonNull RouterFunctions.Visitor visitor) {
        routerFunctions().forEach(routerFunction -> routerFunction.accept(visitor));
    }

    @SuppressWarnings("unchecked")
    private Iterable<RouterFunction<ServerResponse>> routerFunctions() {
        getInstance().acquireReadLock();
        try {
            List<PluginApplicationContext> contexts = getInstance().getPluginApplicationContexts()
                .stream()
                .filter(AbstractApplicationContext::isActive)
                .toList();
            var rawRouterFunctions = contexts
                .stream()
                .flatMap(applicationContext -> applicationContext
                    .getBeanProvider(RouterFunction.class)
                    .orderedStream())
                .map(router -> (RouterFunction<ServerResponse>) router)
                .toList();
            var reverseProxies = reverseProxyRouterFunctionFactory.getRouterFunctions();

            return Iterables.concat(rawRouterFunctions, reverseProxies);
        } finally {
            getInstance().releaseReadLock();
        }
    }
}
