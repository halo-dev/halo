package run.halo.app.theme.router.factories;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static run.halo.app.theme.router.PageUrlUtils.totalPage;

import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Category;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.exception.NotFoundException;
import run.halo.app.infra.utils.PathUtils;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.finders.vo.CategoryVo;
import run.halo.app.theme.finders.vo.ListedPostVo;
import run.halo.app.theme.router.PageUrlUtils;
import run.halo.app.theme.router.UrlContextListResult;
import run.halo.app.theme.router.ViewNameResolver;

/**
 * The {@link CategoryPostRouteFactory} for generate {@link RouterFunction} specific to the template
 * <code>category.html</code>.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
@AllArgsConstructor
public class CategoryPostRouteFactory implements RouteFactory {

    private final PostFinder postFinder;

    private final SystemConfigurableEnvironmentFetcher environmentFetcher;
    private final ReactiveExtensionClient client;
    private final ViewNameResolver viewNameResolver;

    @Override
    public RouterFunction<ServerResponse> create(String prefix) {
        return RouterFunctions.route(GET(PathUtils.combinePath(prefix, "/{slug}"))
            .or(GET(PathUtils.combinePath(prefix, "/{slug}/page/{page:\\d+}")))
            .and(accept(MediaType.TEXT_HTML)), handlerFunction());
    }

    HandlerFunction<ServerResponse> handlerFunction() {
        return request -> {
            String slug = request.pathVariable("slug");
            return fetchBySlug(slug)
                .flatMap(categoryVo -> {
                    Map<String, Object> model = new HashMap<>();
                    model.put(ModelConst.TEMPLATE_ID, DefaultTemplateEnum.CATEGORY.getValue());
                    model.put("posts",
                        postListByCategoryName(categoryVo.getMetadata().getName(), request));
                    model.put("category", categoryVo);
                    String template = categoryVo.getSpec().getTemplate();
                    return viewNameResolver.resolveViewNameOrDefault(request, template,
                            DefaultTemplateEnum.CATEGORY.getValue())
                        .flatMap(viewName -> ServerResponse.ok().render(viewName, model));
                })
                .switchIfEmpty(
                    Mono.error(new NotFoundException("Category not found with slug: " + slug)));
        };
    }

    Mono<CategoryVo> fetchBySlug(String slug) {
        return client.list(Category.class, category -> category.getSpec().getSlug().equals(slug)
                && category.getMetadata().getDeletionTimestamp() == null, null)
            .next()
            .map(CategoryVo::from);
    }

    private Mono<UrlContextListResult<ListedPostVo>> postListByCategoryName(String name,
        ServerRequest request) {
        String path = request.path();
        int pageNum = pageNumInPathVariable(request);
        return configuredPageSize(environmentFetcher, SystemSetting.Post::getCategoryPageSize)
            .flatMap(pageSize -> postFinder.listByCategory(pageNum, pageSize, name))
            .map(list -> new UrlContextListResult.Builder<ListedPostVo>()
                .listResult(list)
                .nextUrl(PageUrlUtils.nextPageUrl(path, totalPage(list)))
                .prevUrl(PageUrlUtils.prevPageUrl(path))
                .build()
            );
    }
}
