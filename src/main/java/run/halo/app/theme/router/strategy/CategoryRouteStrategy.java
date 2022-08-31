package run.halo.app.theme.router.strategy;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.core.extension.Category;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.infra.utils.PathUtils;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.router.PermalinkIndexer;
import run.halo.app.theme.router.TemplateRouterStrategy;

/**
 * The {@link CategoryRouteStrategy} for generate {@link RouterFunction} specific to the template
 * <code>category.html</code>.
 *
 * @author guqing
 * @since 2.0.0
 */
public class CategoryRouteStrategy implements TemplateRouterStrategy {

    private final PermalinkIndexer permalinkIndexer;

    public CategoryRouteStrategy(PermalinkIndexer permalinkIndexer) {
        this.permalinkIndexer = permalinkIndexer;
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
                            Map.of("name", categoryName));
                });
    }
}
