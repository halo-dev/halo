package run.halo.app.core.extension.endpoint;

import java.util.LinkedList;
import java.util.List;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

public class CustomEndpointsBuilder {

    private final List<RouterFunction<ServerResponse>> routerFunctions;

    public CustomEndpointsBuilder() {
        routerFunctions = new LinkedList<>();
    }

    public CustomEndpointsBuilder add(RouterFunction<ServerResponse> routerFunction) {
        routerFunctions.add(routerFunction);
        return this;
    }

    public RouterFunction<ServerResponse> build() {
        return SpringdocRouteBuilder.route()
            .nest(RequestPredicates.path("/apis/api.console.halo.run/v1alpha1"),
                () -> routerFunctions.stream().reduce(RouterFunction::and).orElse(null),
                builder -> builder
                    .operationId("CustomEndpoints")
                    .description("Custom endpoints")
                    .tag("api.console.halo.run/v1alpha1/CustomEndpoint"))
            .build();
    }
}
