package run.halo.app.theme.router.strategy;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.core.extension.Tag;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.infra.utils.PathUtils;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.router.PermalinkIndexer;
import run.halo.app.theme.router.TemplateRouterStrategy;

/**
 * The {@link TagRouteStrategy} for generate {@link RouterFunction} specific to the template
 * <code>tag.html</code>.
 *
 * @author guqing
 * @since 2.0.0
 */
public class TagRouteStrategy implements TemplateRouterStrategy {

    private final PermalinkIndexer permalinkIndexer;

    public TagRouteStrategy(PermalinkIndexer permalinkIndexer) {
        this.permalinkIndexer = permalinkIndexer;
    }

    @Override
    public RouterFunction<ServerResponse> getRouteFunction(String template, String prefix) {
        return RouterFunctions
            .route(GET(PathUtils.combinePath(prefix, "/{slug}"))
                    .or(GET(PathUtils.combinePath(prefix, "/{slug}/page/{page}")))
                    .and(accept(MediaType.TEXT_HTML)),
                request -> {
                    GroupVersionKind gvk = GroupVersionKind.fromExtension(Tag.class);
                    List<String> slugs = permalinkIndexer.getSlugs(gvk);
                    String slug = request.pathVariable("slug");
                    if (!slugs.contains(slug)) {
                        return ServerResponse.notFound().build();
                    }
                    String name = permalinkIndexer.getNameBySlug(gvk, slug);
                    return ServerResponse.ok()
                        .render(DefaultTemplateEnum.TAG.getValue(), Map.of("name", name));
                });
    }
}
