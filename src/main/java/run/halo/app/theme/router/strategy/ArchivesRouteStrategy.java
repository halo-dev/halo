package run.halo.app.theme.router.strategy;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.infra.utils.PathUtils;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.router.TemplateRouterStrategy;

/**
 * The {@link ArchivesRouteStrategy} for generate {@link RouterFunction} specific to the template
 * <code>posts.html</code>.
 *
 * @author guqing
 * @since 2.0.0
 */
public class ArchivesRouteStrategy implements TemplateRouterStrategy {

    @Override
    public RouterFunction<ServerResponse> getRouteFunction(String template, String prefix) {
        return RouterFunctions
            .route(GET(prefix)
                    .or(GET(PathUtils.combinePath(prefix, "/page/{page:\\d+}")))
                    .or(GET(PathUtils.combinePath(prefix, "/{year:\\d{4}}/{month:\\d{2}}")))
                    .or(GET(PathUtils.combinePath(prefix,
                        "/{year:\\d{4}}/{month:\\d{2}}/page/{page:\\d+}")))
                    .and(accept(MediaType.TEXT_HTML)),
                request -> ServerResponse.ok()
                    .render(DefaultTemplateEnum.ARCHIVES.getValue()));
    }
}
