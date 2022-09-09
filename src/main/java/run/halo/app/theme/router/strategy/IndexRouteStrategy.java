package run.halo.app.theme.router.strategy;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static run.halo.app.theme.router.TemplateRouterStrategy.PageUrlUtils.pageNum;
import static run.halo.app.theme.router.TemplateRouterStrategy.PageUrlUtils.totalPage;

import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.finders.vo.PostVo;
import run.halo.app.theme.router.TemplateRouterStrategy;
import run.halo.app.theme.router.UrlContextListResult;

/**
 * The {@link IndexRouteStrategy} for generate {@link RouterFunction} specific to the template
 * <code>index.html</code>.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class IndexRouteStrategy implements TemplateRouterStrategy {

    private final PostFinder postFinder;

    public IndexRouteStrategy(PostFinder postFinder) {
        this.postFinder = postFinder;
    }

    @Override
    public RouterFunction<ServerResponse> getRouteFunction(String template, String pattern) {
        return RouterFunctions
            .route(GET("/").or(GET("/page/{page}"))
                    .and(accept(MediaType.TEXT_HTML)),
                request -> ServerResponse.ok()
                    .render(DefaultTemplateEnum.INDEX.getValue(),
                        Map.of("posts", postList(request))));
    }

    private Mono<UrlContextListResult<PostVo>> postList(ServerRequest request) {
        String path = request.path();
        return Mono.defer(() -> Mono.just(postFinder.list(pageNum(request), 10)))
            .publishOn(Schedulers.boundedElastic())
            .map(list -> new UrlContextListResult.Builder<PostVo>()
                .listResult(list)
                .nextUrl(PageUrlUtils.nextPageUrl(path, totalPage(list)))
                .prevUrl(PageUrlUtils.prevPageUrl(path))
                .build());
    }
}
