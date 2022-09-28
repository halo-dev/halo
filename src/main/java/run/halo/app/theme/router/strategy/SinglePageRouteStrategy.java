package run.halo.app.theme.router.strategy;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static run.halo.app.theme.router.strategy.PermalinkPredicates.get;

import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.core.extension.SinglePage;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.SinglePageFinder;
import run.halo.app.theme.finders.vo.SinglePageVo;
import run.halo.app.theme.router.PermalinkIndexer;
import run.halo.app.theme.router.TemplateRouterStrategy;

/**
 * The {@link SinglePageRouteStrategy} for generate {@link RouterFunction} specific to the template
 * <code>page.html</code>.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class SinglePageRouteStrategy implements TemplateRouterStrategy {

    private final PermalinkIndexer permalinkIndexer;
    private final SinglePageFinder singlePageFinder;

    public SinglePageRouteStrategy(PermalinkIndexer permalinkIndexer,
        SinglePageFinder singlePageFinder) {
        this.permalinkIndexer = permalinkIndexer;
        this.singlePageFinder = singlePageFinder;
    }

    @Override
    public RouterFunction<ServerResponse> getRouteFunction(String template, String pattern) {
        GroupVersionKind gvk = GroupVersionKind.fromExtension(SinglePage.class);

        RequestPredicate requestPredicate = request -> false;

        List<String> permalinks = permalinkIndexer.getPermalinks(gvk);
        for (String permalink : permalinks) {
            requestPredicate = requestPredicate.or(get(permalink));
        }

        return RouterFunctions
            .route(requestPredicate.and(accept(MediaType.TEXT_HTML)), request -> {
                var name = permalinkIndexer.getNameByPermalink(gvk, request.path());
                if (name == null) {
                    return ServerResponse.notFound().build();
                }
                return ServerResponse.ok()
                    .render(DefaultTemplateEnum.SINGLE_PAGE.getValue(),
                        Map.of("name", name,
                            "singlePage", singlePageByName(name)));
            });
    }

    private Mono<SinglePageVo> singlePageByName(String name) {
        return Mono.defer(() -> Mono.just(singlePageFinder.getByName(name)))
            .publishOn(Schedulers.boundedElastic());
    }
}
