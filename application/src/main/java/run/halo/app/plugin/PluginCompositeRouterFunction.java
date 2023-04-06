package run.halo.app.plugin;

import static run.halo.app.plugin.ExtensionContextRegistry.getInstance;

import java.util.ArrayList;
import java.util.List;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.core.extension.endpoint.CustomEndpointsBuilder;
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
    private List<RouterFunction<ServerResponse>> routerFunctions() {
        var rawRouterFunctions = getInstance().getPluginApplicationContexts()
            .stream()
            .flatMap(applicationContext -> applicationContext
                .getBeanProvider(RouterFunction.class)
                .orderedStream())
            .map(router -> (RouterFunction<ServerResponse>) router)
            .toList();
        var reverseProxies = reverseProxyRouterFunctionFactory.getRouterFunctions();

        var endpointBuilder = new CustomEndpointsBuilder();
        getInstance().getPluginApplicationContexts()
            .forEach(context -> context.getBeanProvider(CustomEndpoint.class)
                .orderedStream()
                .forEach(endpointBuilder::add));
        var customEndpoint = endpointBuilder.build();

        List<RouterFunction<ServerResponse>> routerFunctions =
            new ArrayList<>(rawRouterFunctions.size() + reverseProxies.size() + 1);
        routerFunctions.addAll(rawRouterFunctions);
        routerFunctions.addAll(reverseProxies);
        routerFunctions.add(customEndpoint);
        return routerFunctions;
    }
}
