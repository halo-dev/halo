package run.halo.app.theme.router.strategy;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.core.extension.SinglePage;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.router.PermalinkIndexer;
import run.halo.app.theme.router.TemplateRouterStrategy;

/**
 * The {@link SinglePageRouteStrategy} for generate {@link RouterFunction} specific to the template
 * <code>page.html</code>.
 *
 * @author guqing
 * @since 2.0.0
 */
public class SinglePageRouteStrategy implements TemplateRouterStrategy {

    private final PermalinkIndexer permalinkIndexer;

    public SinglePageRouteStrategy(PermalinkIndexer permalinkIndexer) {
        this.permalinkIndexer = permalinkIndexer;
    }

    @Override
    public RouterFunction<ServerResponse> getRouteFunction(String template, String pattern) {
        GroupVersionKind gvk = GroupVersionKind.fromExtension(SinglePage.class);

        RequestPredicate requestPredicate = request -> false;

        List<String> permalinks =
            Objects.requireNonNullElse(permalinkIndexer.getPermalinks(gvk), List.of());
        for (String permalink : permalinks) {
            requestPredicate = requestPredicate.or(RequestPredicates.GET(permalink));
        }

        return RouterFunctions
            .route(requestPredicate.and(accept(MediaType.TEXT_HTML)), request -> {
                String slug = StringUtils.removeStart(request.path(), "/");
                String name = permalinkIndexer.getNameBySlug(gvk, slug);
                if (name == null) {
                    return ServerResponse.notFound().build();
                }
                return ServerResponse.ok()
                    .render(DefaultTemplateEnum.SINGLE_PAGE.getValue(), Map.of("name", name));
            });
    }
}
