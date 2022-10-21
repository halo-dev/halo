package run.halo.app.theme.router.strategy;

import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.CategoryFinder;
import run.halo.app.theme.finders.vo.CategoryTreeVo;

/**
 * Categories router strategy for generate {@link HandlerFunction} specific to the template
 * <code>categories.html</code>.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class CategoriesRouteStrategy implements ListPageRouteHandlerStrategy {
    private final CategoryFinder categoryFinder;

    public CategoriesRouteStrategy(CategoryFinder categoryFinder) {
        this.categoryFinder = categoryFinder;
    }

    private Mono<List<CategoryTreeVo>> categories() {
        return Mono.defer(() -> Mono.just(categoryFinder.listAsTree()))
            .publishOn(Schedulers.boundedElastic());
    }

    @Override
    public HandlerFunction<ServerResponse> getHandler() {
        return request -> ServerResponse.ok()
            .render(DefaultTemplateEnum.CATEGORIES.getValue(),
                Map.of("categories", categories()));
    }

    @Override
    public List<String> getRouterPaths(String prefix) {
        return List.of(StringUtils.prependIfMissing(prefix, "/"));
    }

    @Override
    public boolean supports(DefaultTemplateEnum template) {
        return DefaultTemplateEnum.CATEGORIES.equals(template);
    }
}
