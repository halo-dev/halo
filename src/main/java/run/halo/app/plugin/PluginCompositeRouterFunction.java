package run.halo.app.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.pf4j.PluginWrapper;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.event.HaloPluginStartedEvent;
import run.halo.app.plugin.event.HaloPluginStoppedEvent;
import run.halo.app.plugin.resources.ReverseProxyRouterFunctionFactory;

/**
 * A composite {@link RouterFunction} implementation for plugin.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class PluginCompositeRouterFunction implements RouterFunction<ServerResponse> {

    private final Map<String, RouterFunction<ServerResponse>> routerFunctionRegistry =
        new ConcurrentHashMap<>();

    private final ReverseProxyRouterFunctionFactory reverseProxyRouterFunctionFactory;

    public PluginCompositeRouterFunction(
        ReverseProxyRouterFunctionFactory reverseProxyRouterFunctionFactory) {
        this.reverseProxyRouterFunctionFactory = reverseProxyRouterFunctionFactory;
    }

    public RouterFunction<ServerResponse> getRouterFunction(String pluginId) {
        return routerFunctionRegistry.get(pluginId);
    }

    @Override
    @NonNull
    public Mono<HandlerFunction<ServerResponse>> route(@NonNull ServerRequest request) {
        return Flux.fromIterable(routerFunctionRegistry.values())
            .concatMap(routerFunction -> routerFunction.route(request))
            .next();
    }

    @Override
    public void accept(@NonNull RouterFunctions.Visitor visitor) {
        routerFunctionRegistry.values().forEach(routerFunction -> routerFunction.accept(visitor));
    }

    /**
     * Obtains the user-defined {@link RouterFunction} from the plugin
     * {@link PluginApplicationContext} and create {@link RouterFunction} according to the
     * reverse proxy configuration file then register them to {@link #routerFunctionRegistry}.
     *
     * @param haloPluginStartedEvent event for plugin started
     */
    @EventListener(HaloPluginStartedEvent.class)
    public void onPluginStarted(HaloPluginStartedEvent haloPluginStartedEvent) {
        PluginWrapper plugin = haloPluginStartedEvent.getPlugin();
        // Obtain plugin application context
        PluginApplicationContext pluginApplicationContext =
            ExtensionContextRegistry.getInstance().getByPluginId(plugin.getPluginId());

        // create reverse proxy router function for plugin
        RouterFunction<ServerResponse> reverseProxyRouterFunction =
            reverseProxyRouterFunctionFactory.create(pluginApplicationContext);

        List<RouterFunction<ServerResponse>> routerFunctions =
            routerFunctions(pluginApplicationContext);

        List<RouterFunction<ServerResponse>> combinedRouterFunctions =
            new ArrayList<>(routerFunctions);
        if (reverseProxyRouterFunction != null) {
            combinedRouterFunctions.add(reverseProxyRouterFunction);
        }

        combinedRouterFunctions.stream()
            .reduce(RouterFunction::and)
            .ifPresent(compositeRouterFunction -> {
                routerFunctionRegistry.put(plugin.getPluginId(), compositeRouterFunction);
            });
    }

    @EventListener(HaloPluginStoppedEvent.class)
    public void onPluginStopped(HaloPluginStoppedEvent haloPluginStoppedEvent) {
        PluginWrapper plugin = haloPluginStoppedEvent.getPlugin();
        routerFunctionRegistry.remove(plugin.getPluginId());
    }

    @SuppressWarnings("unchecked")
    private List<RouterFunction<ServerResponse>> routerFunctions(
        PluginApplicationContext applicationContext) {
        List<RouterFunction<ServerResponse>> functions =
            applicationContext.getBeanProvider(RouterFunction.class)
                .orderedStream()
                .map(router -> (RouterFunction<ServerResponse>) router)
                .collect(Collectors.toList());
        return (!CollectionUtils.isEmpty(functions) ? functions : Collections.emptyList());
    }
}
