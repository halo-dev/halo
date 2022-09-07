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
import run.halo.app.theme.finders.TagFinder;
import run.halo.app.theme.finders.vo.TagVo;
import run.halo.app.theme.router.TemplateRouterStrategy;

/**
 * The {@link TagsRouteStrategy} for generate {@link RouterFunction} specific to the template
 * <code>tags.html</code>.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class TagsRouteStrategy implements TemplateRouterStrategy {

    private final TagFinder tagFinder;

    public TagsRouteStrategy(TagFinder tagFinder) {
        this.tagFinder = tagFinder;
    }

    @Override
    public RouterFunction<ServerResponse> getRouteFunction(String template, String prefix) {
        String pattern = PathUtils.combinePath(prefix);
        return RouterFunctions
            .route(GET(pattern)
                    .and(accept(MediaType.TEXT_HTML)),
                request -> ServerResponse.ok()
                    .render(DefaultTemplateEnum.TAGS.getValue(),
                        Map.of("tags", tags()))
            );
    }

    private Mono<List<TagVo>> tags() {
        return Mono.defer(() -> Mono.just(tagFinder.listAll()))
            .publishOn(Schedulers.boundedElastic());
    }
}
