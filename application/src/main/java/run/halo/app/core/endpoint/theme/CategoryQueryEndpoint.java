package run.halo.app.core.endpoint.theme;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Category;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.index.query.Queries;
import run.halo.app.extension.router.SortableRequest;
import run.halo.app.localization.PublicLocalization;
import run.halo.app.localization.ResourceLocalizationService;
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
    private final ResourceLocalizationService localizationService;
    private final PostPublicQueryService postPublicQueryService;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final var tag = "CategoryV1alpha1Public";
        return SpringdocRouteBuilder.route()
                .GET("categories", this::listCategories, builder -> {
                    builder.operationId("queryCategories")
                            .description("Lists categories.")
                            .tag(tag)
                            .response(responseBuilder()
                                    .implementation(ListResult.generateGenericClass(CategoryVo.class)));
                    CategoryPublicQuery.buildParameters(builder);
                })
                .GET(
                        "categories/{name}",
                        this::getByName,
                        builder -> builder.operationId("queryCategoryByName")
                                .description("Gets category by metadata.name.")
                                .tag(tag)
                                .parameter(parameterBuilder()
                                        .in(ParameterIn.PATH)
                                        .name("name")
                                        .description("Category metadata.name")
                                        .required(true))
                                .response(responseBuilder().implementation(CategoryVo.class)))
                .GET("categories/{name}/posts", this::listPostsByCategoryName, builder -> {
                    builder.operationId("queryPostsByCategoryName")
                            .description("Lists posts by category metadata.name.")
                            .tag(tag)
                            .parameter(parameterBuilder()
                                    .in(ParameterIn.PATH)
                                    .name("name")
                                    .description("Category metadata.name")
                                    .required(true))
                            .response(responseBuilder()
                                    .implementation(ListResult.generateGenericClass(ListedPostVo.class)));
                    PostPublicQuery.buildParameters(builder);
                })
                .build();
    }

    private Mono<ServerResponse> listPostsByCategoryName(ServerRequest request) {
        final var name = request.pathVariable("name");
        final var query = new PostPublicQuery(request.exchange());
        var listOptions = query.toListOptions();
        var newFieldSelector = listOptions.getFieldSelector().andQuery(Queries.equal("spec.categories", name));
        listOptions.setFieldSelector(newFieldSelector);
        return postPublicQueryService
                .list(listOptions, query.toPageRequest())
                .flatMap(result -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(result));
    }

    private Mono<ServerResponse> getByName(ServerRequest request) {
        String name = request.pathVariable("name");
        return client.get(Category.class, name)
                .flatMap(category -> PublicLocalization.project(category, localizationService))
                .map(CategoryVo::from)
                .flatMap(categoryVo -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(categoryVo));
    }

    private Mono<ServerResponse> listCategories(ServerRequest request) {
        CategoryPublicQuery query = new CategoryPublicQuery(request.exchange());
        return client.listBy(Category.class, query.toListOptions(), query.toPageRequest())
                .flatMap(result -> PublicLocalization.project(result.getItems(), localizationService)
                        .map(items -> new ListResult<>(
                                result.getPage(),
                                result.getSize(),
                                result.getTotal(),
                                items.stream().map(CategoryVo::from).toList())))
                .flatMap(result -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(result));
    }

    public static class CategoryPublicQuery extends SortableRequest {
        public CategoryPublicQuery(ServerWebExchange exchange) {
            super(exchange);
        }

        public static void buildParameters(Builder builder) {
            SortableRequest.buildParameters(builder);
        }
    }

    @Override
    public GroupVersion groupVersion() {
        return PublicApiUtils.groupVersion(new Category());
    }
}
