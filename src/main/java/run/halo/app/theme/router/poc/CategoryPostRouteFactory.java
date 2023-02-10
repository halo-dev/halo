package run.halo.app.theme.router.poc;

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
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.utils.PathUtils;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.CategoryFinder;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.finders.vo.ListedPostVo;
import run.halo.app.theme.router.PageUrlUtils;
import run.halo.app.theme.router.UrlContextListResult;
import run.halo.app.theme.router.ViewNameResolver;
import run.halo.app.theme.router.strategy.ModelConst;

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
    private CategoryFinder categoryFinder;
    private final ViewNameResolver viewNameResolver;

    @Override
    public RouterFunction<ServerResponse> create(String prefix) {
        return RouterFunctions.route(GET(PathUtils.combinePath(prefix, "/{name}"))
            .or(GET(PathUtils.combinePath(prefix, "/{name}/page/{page:\\d+}")))
            .and(accept(MediaType.TEXT_HTML)), handlerFunction());
    }

    HandlerFunction<ServerResponse> handlerFunction() {
        return request -> {
            Map<String, Object> model = new HashMap<>();
            model.put("posts", postListByCategoryName(request));
            model.put(ModelConst.TEMPLATE_ID, DefaultTemplateEnum.CATEGORY.getValue());
            return categoryFinder.getByName(request.pathVariable("name"))
                .flatMap(categoryVo -> {
                    model.put("category", categoryVo);
                    String template = categoryVo.getSpec().getTemplate();
                    return viewNameResolver.resolveViewNameOrDefault(request, template,
                            DefaultTemplateEnum.CATEGORY.getValue())
                        .flatMap(viewName -> ServerResponse.ok().render(viewName, model));
                });
        };
    }

    private Mono<UrlContextListResult<ListedPostVo>> postListByCategoryName(ServerRequest request) {
        String path = request.path();
        String name = request.pathVariable("name");
        int pageNum = pageNumInPathVariable(request);
        return configuredPageSize(environmentFetcher)
            .flatMap(pageSize -> postFinder.listByCategory(pageNum, pageSize, name))
            .map(list -> new UrlContextListResult.Builder<ListedPostVo>()
                .listResult(list)
                .nextUrl(PageUrlUtils.nextPageUrl(path, totalPage(list)))
                .prevUrl(PageUrlUtils.prevPageUrl(path))
                .build()
            );
    }
}
