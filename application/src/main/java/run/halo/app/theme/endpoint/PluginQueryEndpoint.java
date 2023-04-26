package run.halo.app.theme.endpoint;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.RequiredArgsConstructor;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Plugin;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.theme.finders.PluginFinder;

/**
 * Endpoint for plugin query APIs.
 *
 * @author guqing
 * @since 2.5.0
 */
@Component
@RequiredArgsConstructor
public class PluginQueryEndpoint implements CustomEndpoint {

    private final PluginFinder pluginFinder;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final var tag = groupVersion().toString() + "/Plugin";
        return SpringdocRouteBuilder.route()
            .GET("plugins/{name}/available", this::availableByName,
                builder -> builder.operationId("queryPluginAvailableByName")
                    .description("Gets plugin available by name.")
                    .tag(tag)
                    .parameter(parameterBuilder()
                        .in(ParameterIn.PATH)
                        .name("name")
                        .description("Plugin name")
                        .required(true)
                    )
                    .response(responseBuilder()
                        .implementation(Boolean.class)
                    )
            )
            .build();
    }

    private Mono<ServerResponse> availableByName(ServerRequest request) {
        String name = request.pathVariable("name");
        boolean available = pluginFinder.available(name);
        return ServerResponse.ok().bodyValue(available);
    }

    @Override
    public GroupVersion groupVersion() {
        return PublicApiUtils.groupVersion(new Plugin());
    }
}