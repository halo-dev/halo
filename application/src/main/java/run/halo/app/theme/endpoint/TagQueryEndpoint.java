package run.halo.app.theme.endpoint;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static run.halo.app.theme.endpoint.PublicApiUtils.containsElement;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.Tag;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.router.QueryParamBuildUtil;
import run.halo.app.theme.finders.PostPublicQueryService;
import run.halo.app.theme.finders.TagFinder;
import run.halo.app.theme.finders.vo.ListedPostVo;
import run.halo.app.theme.finders.vo.TagVo;

/**
 * Endpoint for tag query APIs.
 *
 * @author guqing
 * @since 2.5.0
 */
@Component
@RequiredArgsConstructor
public class TagQueryEndpoint implements CustomEndpoint {

    private final TagFinder tagFinder;
    private final PostPublicQueryService postPublicQueryService;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final var tag = groupVersion().toString() + "/Tag";
        return SpringdocRouteBuilder.route()
            .GET("tags", this::listTags,
                builder -> {
                    builder.operationId("queryTags")
                        .description("Lists tags")
                        .tag(tag)
                        .response(responseBuilder()
                            .implementation(
                                ListResult.generateGenericClass(TagVo.class))
                        );
                    QueryParamBuildUtil.buildParametersFromType(builder, TagPublicQuery.class);
                }
            )
            .GET("tags/{name}", this::getTagByName,
                builder -> builder.operationId("queryTagByName")
                    .description("Gets tag by name")
                    .tag(tag)
                    .parameter(parameterBuilder()
                        .in(ParameterIn.PATH)
                        .name("name")
                        .description("Tag name")
                        .required(true)
                    )
                    .response(responseBuilder()
                        .implementation(TagVo.class)
                    )
            )
            .GET("tags/{name}/posts", this::listPostsByTagName,
                builder -> {
                    builder.operationId("queryPostsByTagName")
                        .description("Lists posts by tag name")
                        .tag(tag)
                        .parameter(parameterBuilder()
                            .in(ParameterIn.PATH)
                            .name("name")
                            .description("Tag name")
                            .required(true)
                        )
                        .response(responseBuilder()
                            .implementation(ListedPostVo.class)
                        );
                    QueryParamBuildUtil.buildParametersFromType(builder, PostPublicQuery.class);
                }
            )
            .build();
    }

    private Mono<ServerResponse> getTagByName(ServerRequest request) {
        String name = request.pathVariable("name");
        return tagFinder.getByName(name)
            .flatMap(tag -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(tag)
            );
    }

    private Mono<ServerResponse> listPostsByTagName(ServerRequest request) {
        final var name = request.pathVariable("name");
        final var query = new PostPublicQuery(request.exchange());
        final Predicate<Post> containsTagPredicate =
            post -> containsElement(post.getSpec().getTags(), name);
        return postPublicQueryService.list(query.getPage(),
                query.getSize(),
                containsTagPredicate.and(query.toPredicate()),
                query.toComparator()
            )
            .flatMap(result -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(result)
            );
    }

    private Mono<ServerResponse> listTags(ServerRequest request) {
        var query = new TagPublicQuery(request.exchange());
        return tagFinder.list(query.getPage(),
                query.getSize(),
                query.toPredicate(),
                query.toComparator()
            )
            .flatMap(result -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(result)
            );
    }

    static class TagPublicQuery extends SortableRequest {
        public TagPublicQuery(ServerWebExchange exchange) {
            super(exchange);
        }
    }

    @Override
    public GroupVersion groupVersion() {
        return PublicApiUtils.groupVersion(new Tag());
    }
}
