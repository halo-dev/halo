package run.halo.app.theme.router.factories;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static run.halo.app.theme.router.PageUrlUtils.totalPage;

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
import run.halo.app.infra.SystemSetting;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.finders.vo.ListedPostVo;
import run.halo.app.theme.router.PageUrlUtils;
import run.halo.app.theme.router.UrlContextListResult;

/**
 * The {@link IndexRouteFactory} for generate {@link RouterFunction} specific to the template
 * <code>index.html</code>.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
@AllArgsConstructor
public class IndexRouteFactory implements RouteFactory {

    private final PostFinder postFinder;
    private final SystemConfigurableEnvironmentFetcher environmentFetcher;

    @Override
    public RouterFunction<ServerResponse> create(String pattern) {
        return RouterFunctions
            .route(GET("/").or(GET("/page/{page}")
                .or(GET("/index")).or(GET("/index/page/{page}"))
                .and(accept(MediaType.TEXT_HTML))), handlerFunction());
    }

    HandlerFunction<ServerResponse> handlerFunction() {
        return request -> ServerResponse.ok()
            .render(DefaultTemplateEnum.INDEX.getValue(),
                Map.of("posts", postList(request),
                    ModelConst.TEMPLATE_ID, DefaultTemplateEnum.INDEX.getValue()));
    }

    private Mono<UrlContextListResult<ListedPostVo>> postList(ServerRequest request) {
        String path = request.path();
        return configuredPageSize(environmentFetcher, SystemSetting.Post::getPostPageSize)
            .flatMap(pageSize -> postFinder.list(pageNumInPathVariable(request), pageSize))
            .map(list -> new UrlContextListResult.Builder<ListedPostVo>()
                .listResult(list)
                .nextUrl(PageUrlUtils.nextPageUrl(path, totalPage(list)))
                .prevUrl(PageUrlUtils.prevPageUrl(path))
                .build()
            );
    }
}
