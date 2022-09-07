package run.halo.app.theme.router.strategy;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

import org.springframework.http.MediaType;
import org.springframework.http.server.PathContainer;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.pattern.PathPatternParser;
import run.halo.app.extension.ListResult;
import run.halo.app.infra.utils.PathUtils;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.finders.vo.PostVo;
import run.halo.app.theme.router.TemplateRouterStrategy;

/**
 * The {@link ArchivesRouteStrategy} for generate {@link RouterFunction} specific to the template
 * <code>posts.html</code>.
 *
 * @author guqing
 * @since 2.0.0
 */
public class ArchivesRouteStrategy implements TemplateRouterStrategy {
    private PostFinder postFinder;

    @Override
    public RouterFunction<ServerResponse> getRouteFunction(String template, String prefix) {
        return RouterFunctions
            .route(GET(prefix)
                    .or(GET(PathUtils.combinePath(prefix, "/page/{page}")))
                    .or(GET(PathUtils.combinePath(prefix, "/{year}/{month}")))
                    .or(GET(PathUtils.combinePath(prefix, "/{year}/{month}/page/{page}")))
                    .and(accept(MediaType.TEXT_HTML)),
                request -> {
                    return ServerResponse.ok()
                        .render(DefaultTemplateEnum.ARCHIVES.getValue());
                });
    }

    private void test(Integer page, String path) {
        PathPatternParser.defaultInstance.parse(path).matchAndExtract(PathContainer.parsePath(""));
        ListResult<PostVo> list = postFinder.list(1, 10);
        // new PageResult.Builder<PostVo>()
        //     .page(list.getPage())
        //     .size(list.getSize())
        //     .items(list.getItems())
        //     .nextUrl()
        //     .prevUrl();
    }
}
