package run.halo.app.search;

import lombok.extern.slf4j.Slf4j;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;

@Component
@Slf4j
public class IndicesEndpoint implements CustomEndpoint {

    private final IndicesService indicesService;

    private static final String API_VERSION = "api.console.halo.run/v1alpha1";

    public IndicesEndpoint(IndicesService indicesService) {
        this.indicesService = indicesService;
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final var tag = API_VERSION + "/Indices";
        return SpringdocRouteBuilder.route()
            .POST("indices/post", this::rebuildPostIndices,
                builder -> builder.operationId("BuildPostIndices")
                    .tag(tag)
                    .description("Build or rebuild post indices for full text search"))
            .build();
    }

    private Mono<ServerResponse> rebuildPostIndices(ServerRequest request) {
        return indicesService.rebuildPostIndices()
            .then(Mono.defer(() -> ServerResponse.ok().bodyValue("Rebuild post indices")));
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion(API_VERSION);
    }

}
