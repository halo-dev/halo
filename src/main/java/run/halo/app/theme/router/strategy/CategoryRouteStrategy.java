package run.halo.app.theme.router.strategy;

import static run.halo.app.theme.router.PageUrlUtils.pageNum;
import static run.halo.app.theme.router.PageUrlUtils.totalPage;

import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.core.extension.Category;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.infra.SystemSetting;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.CategoryFinder;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.finders.vo.CategoryVo;
import run.halo.app.theme.finders.vo.PostVo;
import run.halo.app.theme.router.PageUrlUtils;
import run.halo.app.theme.router.UrlContextListResult;

/**
 * The {@link CategoryRouteStrategy} for generate {@link HandlerFunction} specific to the template
 * <code>category.html</code>.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class CategoryRouteStrategy implements DetailsPageRouteHandlerStrategy {
    private final GroupVersionKind gvk = GroupVersionKind.fromExtension(Category.class);
    private final PostFinder postFinder;

    private final CategoryFinder categoryFinder;

    public CategoryRouteStrategy(PostFinder postFinder,
        CategoryFinder categoryFinder) {
        this.postFinder = postFinder;
        this.categoryFinder = categoryFinder;
    }

    private Mono<UrlContextListResult<PostVo>> postListByCategoryName(String name,
        ServerRequest request) {
        String path = request.path();
        return Mono.defer(() -> Mono.just(postFinder.listByCategory(pageNum(request), 10, name)))
            .publishOn(Schedulers.boundedElastic())
            .map(list -> new UrlContextListResult.Builder<PostVo>()
                .listResult(list)
                .nextUrl(PageUrlUtils.nextPageUrl(path, totalPage(list)))
                .prevUrl(PageUrlUtils.prevPageUrl(path))
                .build());
    }

    private Mono<CategoryVo> categoryByName(String name) {
        return Mono.defer(() -> Mono.just(categoryFinder.getByName(name)))
            .publishOn(Schedulers.boundedElastic());
    }

    @Override
    public HandlerFunction<ServerResponse> getHandler(SystemSetting.ThemeRouteRules routeRules,
        String name) {
        return request -> ServerResponse.ok()
            .render(DefaultTemplateEnum.CATEGORY.getValue(),
                Map.of("name", name,
                    "posts", postListByCategoryName(name, request),
                    "category", categoryByName(name)));
    }

    @Override
    public boolean supports(GroupVersionKind gvk) {
        return this.gvk.equals(gvk);
    }
}
