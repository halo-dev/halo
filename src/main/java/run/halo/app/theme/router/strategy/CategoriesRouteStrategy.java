package run.halo.app.theme.router.strategy;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.infra.utils.PathUtils;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.CategoryFinder;
import run.halo.app.theme.finders.vo.CategoryTreeVo;
import run.halo.app.theme.router.TemplateRouterStrategy;

/**
 * Categories router strategy for generate {@link RouterFunction} specific to the template
 * <code>categories.html</code>.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class CategoriesRouteStrategy implements TemplateRouterStrategy {
    private final CategoryFinder categoryFinder;

    public CategoriesRouteStrategy(CategoryFinder categoryFinder) {
        this.categoryFinder = categoryFinder;
    }

    @Override
    public RouterFunction<ServerResponse> getRouteFunction(String template, String prefix) {
        return RouterFunctions
            .route(GET(PathUtils.combinePath(prefix))
                    .and(accept(MediaType.TEXT_HTML)),
                request -> ServerResponse.ok()
                    .render(DefaultTemplateEnum.CATEGORIES.getValue(),
                        Map.of("categories", categories())));
    }

    private Mono<List<CategoryTreeVo>> categories() {
        return Mono.defer(() -> Mono.just(categoryFinder.listAsTree()))
            .publishOn(Schedulers.boundedElastic());
    }
}
