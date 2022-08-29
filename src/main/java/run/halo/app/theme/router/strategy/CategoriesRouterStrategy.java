package run.halo.app.theme.router.strategy;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.router.TemplateRouterStrategy;

/**
 * Categories router strategy for generate {@link RouterFunction} specific to the template
 * <code>categories.html</code>.
 *
 * @author guqing
 * @since 2.0.0
 */
public class CategoriesRouterStrategy implements TemplateRouterStrategy {

    @Override
    public RouterFunction<ServerResponse> getRouteFunction(String template, String prefix) {
        return RouterFunctions
            .route(GET("/categories")
                    .and(accept(MediaType.TEXT_HTML)),
                request -> ServerResponse.ok()
                    .render(DefaultTemplateEnum.CATEGORIES.getValue()));
    }
}
