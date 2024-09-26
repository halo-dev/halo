package run.halo.app.plugin;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.infra.SecureServerRequest;

/**
 * A composite {@link RouterFunction} implementation for plugin.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class DefaultPluginRouterFunctionRegistry
    implements RouterFunction<ServerResponse>, PluginRouterFunctionRegistry {

    private final Collection<RouterFunction<ServerResponse>> routerFunctions;

    public DefaultPluginRouterFunctionRegistry() {
        this.routerFunctions = new CopyOnWriteArraySet<>();
    }

    @Override
    @NonNull
    public Mono<HandlerFunction<ServerResponse>> route(@NonNull ServerRequest request) {
        var secureRequest = new SecureServerRequest(request);
        return Flux.fromIterable(this.routerFunctions)
            .concatMap(routerFunction -> routerFunction.route(secureRequest))
            .next();
    }

    @Override
    public void accept(@NonNull RouterFunctions.Visitor visitor) {
        this.routerFunctions.forEach(routerFunction -> routerFunction.accept(visitor));
    }

    @Override
    public void register(Collection<RouterFunction<ServerResponse>> routerFunctions) {
        this.routerFunctions.addAll(routerFunctions);
    }

    @Override
    public void unregister(Collection<RouterFunction<ServerResponse>> routerFunctions) {
        this.routerFunctions.removeAll(routerFunctions);
    }

    /**
     * Only for testing.
     *
     * @return maintained router functions.
     */
    Collection<RouterFunction<ServerResponse>> getRouterFunctions() {
        return routerFunctions;
    }
}
