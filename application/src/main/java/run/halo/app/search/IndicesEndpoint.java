package run.halo.app.search;

import lombok.extern.slf4j.Slf4j;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.search.event.HaloDocumentRebuildRequestEvent;

@Component
@Slf4j
public class IndicesEndpoint implements CustomEndpoint {

    private static final String API_VERSION = "api.console.halo.run/v1alpha1";

    private final ApplicationEventPublisher eventPublisher;

    public IndicesEndpoint(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final var tag = "IndicesV1alpha1Console";
        return SpringdocRouteBuilder.route()
            .POST("indices/post", this::rebuildIndices,
                builder -> builder.operationId("BuildPostIndices")
                    .tag(tag)
                    .deprecated(true)
                    .description("""
                        Build or rebuild post indices for full text search. \
                        This method is deprecated, please use POST /indices/-/rebuild instead.\
                        """)
            )
            .POST("/indices/-/rebuild", this::rebuildIndices,
                builder -> builder.operationId("RebuildAllIndices")
                    .tag(tag)
                    .description("Rebuild all indices")
            )
            .build();
    }

    private Mono<ServerResponse> rebuildIndices(ServerRequest serverRequest) {
        return Mono.fromRunnable(
            () -> eventPublisher.publishEvent(new HaloDocumentRebuildRequestEvent(this))
        ).then(ServerResponse.accepted().build());
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion(API_VERSION);
    }

}
