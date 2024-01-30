package run.halo.app.plugin;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.core.extension.endpoint.CustomEndpointsBuilder;

/**
 * Aggregated router function built from all custom endpoints.
 *
 * @author johnniang
 */
public class AggregatedRouterFunction implements RouterFunction<ServerResponse> {

    private final RouterFunction<ServerResponse> aggregated;

    public AggregatedRouterFunction(ObjectProvider<CustomEndpoint> customEndpoints) {
        var builder = new CustomEndpointsBuilder();
        customEndpoints.orderedStream()
            .forEach(builder::add);
        this.aggregated = builder.build();
    }

    @Override
    public Mono<HandlerFunction<ServerResponse>> route(ServerRequest request) {
        return aggregated.route(request);
    }

    @Override
    public void accept(RouterFunctions.Visitor visitor) {
        this.aggregated.accept(visitor);
    }
}
