package run.halo.app.theme.endpoint;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;

import lombok.RequiredArgsConstructor;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.theme.finders.SiteStatsFinder;
import run.halo.app.theme.finders.vo.SiteStatsVo;

/**
 * Endpoint for site stats query APIs.
 *
 * @author guqing
 * @since 2.5.0
 */
@Component
@RequiredArgsConstructor
public class SiteStatsQueryEndpoint implements CustomEndpoint {

    private final SiteStatsFinder siteStatsFinder;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final var tag = groupVersion().toString() + "/Stats";
        return SpringdocRouteBuilder.route()
            .GET("stats/-", this::getStats,
                builder -> builder.operationId("queryStats")
                    .description("Gets site stats")
                    .tag(tag)
                    .response(responseBuilder()
                        .implementation(SiteStatsVo.class)
                    )
            )
            .build();
    }

    private Mono<ServerResponse> getStats(ServerRequest request) {
        return siteStatsFinder.getStats()
            .flatMap(result -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(result)
            );
    }

    @Override
    public GroupVersion groupVersion() {
        return new GroupVersion("api.halo.run", "v1alpha1");
    }
}
