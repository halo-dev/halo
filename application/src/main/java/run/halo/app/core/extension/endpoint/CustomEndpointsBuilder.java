package run.halo.app.core.extension.endpoint;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.extension.GroupVersion;

public class CustomEndpointsBuilder {

    private final Map<GroupVersion, List<RouterFunction<ServerResponse>>> routerFunctionsMap;

    public CustomEndpointsBuilder() {
        routerFunctionsMap = new HashMap<>();
    }

    public CustomEndpointsBuilder add(CustomEndpoint customEndpoint) {
        routerFunctionsMap
            .computeIfAbsent(customEndpoint.groupVersion(), gv -> new LinkedList<>())
            .add(customEndpoint.endpoint());
        return this;
    }

    public RouterFunction<ServerResponse> build() {
        SpringdocRouteBuilder routeBuilder = SpringdocRouteBuilder.route();
        routerFunctionsMap.forEach((gv, routerFunctions) -> {
            routeBuilder.nest(RequestPredicates.path("/apis/" + gv),
                () -> routerFunctions.stream().reduce(RouterFunction::and).orElse(null),
                builder -> builder.operationId("CustomEndpoints")
                    .description("Custom Endpoint")
                    .tag(gv + "/CustomEndpoint")
            );
        });
        if (routerFunctionsMap.isEmpty()) {
            // return empty route.
            return request -> Mono.empty();
        }
        routerFunctionsMap.clear();
        return routeBuilder.build();
    }
}
