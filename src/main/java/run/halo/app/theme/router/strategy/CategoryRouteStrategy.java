package run.halo.app.theme.router.strategy;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.core.extension.Category;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.infra.utils.PathUtils;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.finders.vo.PostVo;
import run.halo.app.theme.router.PageResult;
import run.halo.app.theme.router.PermalinkIndexer;
import run.halo.app.theme.router.TemplateRouterStrategy;

/**
 * The {@link CategoryRouteStrategy} for generate {@link RouterFunction} specific to the template
 * <code>category.html</code>.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class CategoryRouteStrategy implements TemplateRouterStrategy {

    private final PermalinkIndexer permalinkIndexer;
    private final PostFinder postFinder;

    public CategoryRouteStrategy(PermalinkIndexer permalinkIndexer, PostFinder postFinder) {
        this.permalinkIndexer = permalinkIndexer;
        this.postFinder = postFinder;
    }

    @Override
    public RouterFunction<ServerResponse> getRouteFunction(String template, String prefix) {
        return RouterFunctions
            .route(GET(PathUtils.combinePath(prefix, "/{slug}"))
                    .or(GET(PathUtils.combinePath(prefix, "/{slug}/page/{page}")))
                    .and(accept(MediaType.TEXT_HTML)),
                request -> {
                    String slug = request.pathVariable("slug");
                    GroupVersionKind gvk = GroupVersionKind.fromExtension(Category.class);
                    List<String> slugs = permalinkIndexer.getSlugs(gvk);
                    if (!slugs.contains(slug)) {
                        return ServerResponse.notFound().build();
                    }
                    String categoryName = permalinkIndexer.getNameBySlug(gvk, slug);
                    return ServerResponse.ok()
                        .render(DefaultTemplateEnum.CATEGORY.getValue(),
                            Map.of("name", categoryName,
                                "posts", postListByCategoryName(categoryName, request)));
                });
    }

    private Mono<PageResult<PostVo>> postListByCategoryName(String name, ServerRequest request) {
        String path = request.path();
        return Mono.defer(() -> Mono.just(postFinder.listByCategory(pageNum(request), 10, name)))
            .publishOn(Schedulers.boundedElastic())
            .map(list -> new PageResult.Builder<PostVo>()
                .listResult(list)
                .nextUrl(PageUrlUtils.nextPageUrl(path, totalPage(list)))
                .prevUrl(PageUrlUtils.prevPageUrl(path))
                .build());
    }
}
