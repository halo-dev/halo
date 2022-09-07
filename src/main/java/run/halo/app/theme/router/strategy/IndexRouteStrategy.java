package run.halo.app.theme.router.strategy;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

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
import run.halo.app.theme.router.PageResult;
import run.halo.app.theme.router.TemplateRouterStrategy;

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

    private Mono<PageResult<PostVo>> postList(ServerRequest request) {
        String path = request.path();
        return Mono.defer(() -> Mono.just(postFinder.list(pageNum(request), 10)))
            .publishOn(Schedulers.boundedElastic())
            .map(list -> new PageResult.Builder<PostVo>()
                .listResult(list)
                .nextUrl(PageUrlUtils.nextPageUrl(path))
                .prevUrl(PageUrlUtils.prevPageUrl(path))
                .build());
    }
}
