package run.halo.app.plugin;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;
import run.halo.app.core.endpoint.console.CustomEndpointsBuilder;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.infra.SecureServerRequest;

/**
 * Aggregated router function built from all custom endpoints.
 *
 * @author johnniang
 */
public class AggregatedRouterFunction implements RouterFunction<ServerResponse> {

    private final RouterFunction<ServerResponse> aggregated;

    public AggregatedRouterFunction(ObjectProvider<CustomEndpoint> customEndpoints) {
        var builder = new CustomEndpointsBuilder();
        customEndpoints.orderedStream().forEach(builder::add);
        this.aggregated = builder.build();
    }

    @Override
    public Mono<HandlerFunction<ServerResponse>> route(ServerRequest request) {
        return aggregated.route(new SecureServerRequest(request));
    }

    @Override
    public void accept(RouterFunctions.Visitor visitor) {
        this.aggregated.accept(visitor);
    }
}
