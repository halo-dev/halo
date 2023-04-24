package run.halo.app.theme.endpoint;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static run.halo.app.theme.endpoint.PublicApiUtils.containsElement;
import static run.halo.app.theme.endpoint.PublicApiUtils.toAnotherListResult;

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
import run.halo.app.core.extension.content.Category;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.router.QueryParamBuildUtil;
import run.halo.app.theme.finders.PostPublicQueryService;
import run.halo.app.theme.finders.vo.CategoryVo;
import run.halo.app.theme.finders.vo.ListedPostVo;

/**
 * Endpoint for category query APIs.
 *
 * @author guqing
 * @since 2.5.0
 */
@Component
@RequiredArgsConstructor
public class CategoryQueryEndpoint implements CustomEndpoint {

    private final ReactiveExtensionClient client;
    private final PostPublicQueryService postPublicQueryService;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final var tag = groupVersion().toString() + "/Category";
        return SpringdocRouteBuilder.route()
            .GET("categories", this::listCategories,
                builder -> {
                    builder.operationId("queryCategories")
                        .description("Lists categories.")
                        .tag(tag)
                        .response(responseBuilder()
                            .implementation(ListResult.generateGenericClass(CategoryVo.class))
                        );
                    QueryParamBuildUtil.buildParametersFromType(builder, CategoryPublicQuery.class);
                }
            )
            .GET("categories/{name}", this::getByName,
                builder -> builder.operationId("queryCategoryByName")
                    .description("Gets category by name.")
                    .tag(tag)
                    .parameter(parameterBuilder()
                        .in(ParameterIn.PATH)
                        .name("name")
                        .description("Category name")
                        .required(true)
                    )
                    .response(responseBuilder()
                        .implementation(CategoryVo.class)
                    )
            )
            .GET("categories/{name}/posts", this::listPostsByCategoryName,
                builder -> {
                    builder.operationId("queryPostsByCategoryName")
                        .description("Lists posts by category name.")
                        .tag(tag)
                        .parameter(parameterBuilder()
                            .in(ParameterIn.PATH)
                            .name("name")
                            .description("Category name")
                            .required(true)
                        )
                        .response(responseBuilder()
                            .implementation(ListResult.generateGenericClass(ListedPostVo.class))
                        );
                    QueryParamBuildUtil.buildParametersFromType(builder, PostPublicQuery.class);
                }
            )
            .build();
    }

    private Mono<ServerResponse> listPostsByCategoryName(ServerRequest request) {
        final var name = request.pathVariable("name");
        final var query = new PostPublicQuery(request.exchange());
        Predicate<Post> categoryContainsPredicate =
            post -> containsElement(post.getSpec().getCategories(), name);
        return postPublicQueryService.list(query.getPage(),
                query.getSize(),
                categoryContainsPredicate.and(query.toPredicate()),
                query.toComparator()
            )
            .flatMap(result -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(result)
            );
    }

    private Mono<ServerResponse> getByName(ServerRequest request) {
        String name = request.pathVariable("name");
        return client.get(Category.class, name)
            .map(CategoryVo::from)
            .flatMap(categoryVo -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(categoryVo)
            );
    }

    private Mono<ServerResponse> listCategories(ServerRequest request) {
        CategoryPublicQuery query = new CategoryPublicQuery(request.exchange());
        return client.list(Category.class,
                query.toPredicate(),
                query.toComparator(),
                query.getPage(),
                query.getSize()
            )
            .map(listResult -> toAnotherListResult(listResult, CategoryVo::from))
            .flatMap(result -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(result)
            );
    }

    public static class CategoryPublicQuery extends SortableRequest {
        public CategoryPublicQuery(ServerWebExchange exchange) {
            super(exchange);
        }
    }

    @Override
    public GroupVersion groupVersion() {
        return PublicApiUtils.groupVersion(new Category());
    }
}
