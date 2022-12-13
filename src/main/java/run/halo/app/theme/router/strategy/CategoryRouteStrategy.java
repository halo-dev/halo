package run.halo.app.theme.router.strategy;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static run.halo.app.theme.router.PageUrlUtils.pageNum;
import static run.halo.app.theme.router.PageUrlUtils.totalPage;

import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Category;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.CategoryFinder;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.finders.vo.ListedPostVo;
import run.halo.app.theme.router.PageUrlUtils;
import run.halo.app.theme.router.UrlContextListResult;
import run.halo.app.theme.router.ViewNameResolver;

/**
 * The {@link CategoryRouteStrategy} for generate {@link HandlerFunction} specific to the template
 * <code>category.html</code>.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
@AllArgsConstructor
public class CategoryRouteStrategy implements DetailsPageRouteHandlerStrategy {
    private final GroupVersionKind gvk = GroupVersionKind.fromExtension(Category.class);
    private final PostFinder postFinder;

    private final CategoryFinder categoryFinder;

    private final ViewNameResolver viewNameResolver;

    private final SystemConfigurableEnvironmentFetcher environmentFetcher;

    private Mono<UrlContextListResult<ListedPostVo>> postListByCategoryName(String name,
        ServerRequest request) {
        String path = request.path();
        return environmentFetcher.fetchPost()
            .map(post -> defaultIfNull(post.getCategoryPageSize(), ModelConst.DEFAULT_PAGE_SIZE))
            .flatMap(
                pageSize -> postFinder.listByCategory(pageNum(request), pageSize, name))
            .map(list -> new UrlContextListResult.Builder<ListedPostVo>()
                .listResult(list)
                .nextUrl(PageUrlUtils.nextPageUrl(path, totalPage(list)))
                .prevUrl(PageUrlUtils.prevPageUrl(path))
                .build());
    }

    @Override
    public HandlerFunction<ServerResponse> getHandler(SystemSetting.ThemeRouteRules routeRules,
        String name) {
        return request -> {
            Map<String, Object> model = new HashMap<>();
            model.put("name", name);
            model.put("posts", postListByCategoryName(name, request));

            model.put(ModelConst.TEMPLATE_ID, DefaultTemplateEnum.CATEGORY.getValue());
            return categoryFinder.getByName(name).flatMap(categoryVo -> {
                model.put("category", categoryVo);
                String template = categoryVo.getSpec().getTemplate();
                return viewNameResolver.resolveViewNameOrDefault(request, template,
                        DefaultTemplateEnum.CATEGORY.getValue())
                    .flatMap(viewName -> ServerResponse.ok().render(viewName, model));
            });
        };
    }

    @Override
    public boolean supports(GroupVersionKind gvk) {
        return this.gvk.equals(gvk);
    }
}
