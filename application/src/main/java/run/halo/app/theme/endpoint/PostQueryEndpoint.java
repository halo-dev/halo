package run.halo.app.theme.endpoint;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.RequiredArgsConstructor;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.router.QueryParamBuildUtil;
import run.halo.app.infra.exception.NotFoundException;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.finders.PostPublicQueryService;
import run.halo.app.theme.finders.vo.ListedPostVo;
import run.halo.app.theme.finders.vo.NavigationPostVo;
import run.halo.app.theme.finders.vo.PostVo;

/**
 * Endpoint for post query.
 *
 * @author guqing
 * @since 2.5.0
 */
@Component
@RequiredArgsConstructor
public class PostQueryEndpoint implements CustomEndpoint {

    private final PostFinder postFinder;
    private final PostPublicQueryService postPublicQueryService;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final var tag = groupVersion().toString() + "/Post";
        return SpringdocRouteBuilder.route()
            .GET("posts", this::listPosts,
                builder -> {
                    builder.operationId("queryPosts")
                        .description("Lists posts.")
                        .tag(tag)
                        .response(responseBuilder()
                            .implementation(ListResult.generateGenericClass(ListedPostVo.class))
                        );
                    QueryParamBuildUtil.buildParametersFromType(builder, PostPublicQuery.class);
                }
            )
            .GET("posts/{name}", this::getPostByName,
                builder -> builder.operationId("queryPostByName")
                    .description("Gets a post by name.")
                    .tag(tag)
                    .parameter(parameterBuilder()
                        .in(ParameterIn.PATH)
                        .name("name")
                        .description("Post name")
                        .required(true)
                    )
                    .response(responseBuilder()
                        .implementation(PostVo.class)
                    )
            )
            .GET("posts/{name}/navigation", this::getPostNavigationByName,
                builder -> builder.operationId("queryPostNavigationByName")
                    .description("Gets a post navigation by name.")
                    .tag(tag)
                    .parameter(parameterBuilder()
                        .in(ParameterIn.PATH)
                        .name("name")
                        .description("Post name")
                        .required(true)
                    )
                    .response(responseBuilder()
                        .implementation(NavigationPostVo.class)
                    )
            )
            .build();
    }

    private Mono<ServerResponse> getPostNavigationByName(ServerRequest request) {
        final var name = request.pathVariable("name");
        return postFinder.cursor(name)
            .doOnNext(result -> {
                if (result.getCurrent() == null) {
                    throw new NotFoundException("Post not found");
                }
            })
            .flatMap(result -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .bodyValue(result)
            );
    }

    private Mono<ServerResponse> getPostByName(ServerRequest request) {
        final var name = request.pathVariable("name");
        return postFinder.getByName(name)
            .switchIfEmpty(Mono.error(() -> new NotFoundException("Post not found")))
            .flatMap(post -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .bodyValue(post)
            );
    }

    private Mono<ServerResponse> listPosts(ServerRequest request) {
        PostPublicQuery query = new PostPublicQuery(request.exchange());
        return postPublicQueryService.list(query.getPage(), query.getSize(), query.toPredicate(),
                query.toComparator())
            .flatMap(result -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .bodyValue(result)
            );
    }

    @Override
    public GroupVersion groupVersion() {
        return PublicApiUtils.groupVersion(new Post());
    }
}