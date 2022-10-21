package run.halo.app.theme.router.strategy;

import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.core.extension.SinglePage;
import run.halo.app.extension.GVK;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.infra.SystemSetting;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.SinglePageFinder;
import run.halo.app.theme.finders.vo.SinglePageVo;

/**
 * The {@link SinglePageRouteStrategy} for generate {@link HandlerFunction} specific to the template
 * <code>page.html</code>.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class SinglePageRouteStrategy implements DetailsPageRouteHandlerStrategy {
    private final GroupVersionKind gvk = GroupVersionKind.fromExtension(SinglePage.class);
    private final SinglePageFinder singlePageFinder;

    public SinglePageRouteStrategy(SinglePageFinder singlePageFinder) {
        this.singlePageFinder = singlePageFinder;
    }

    private String getPlural() {
        GVK annotation = SinglePage.class.getAnnotation(GVK.class);
        return annotation.plural();
    }

    private Mono<SinglePageVo> singlePageByName(String name) {
        return Mono.defer(() -> Mono.just(singlePageFinder.getByName(name)))
            .publishOn(Schedulers.boundedElastic());
    }

    @Override
    public HandlerFunction<ServerResponse> getHandler(SystemSetting.ThemeRouteRules routeRules,
        String name) {
        return request -> ServerResponse.ok()
            .render(DefaultTemplateEnum.SINGLE_PAGE.getValue(),
                Map.of("name", name,
                    "groupVersionKind", gvk,
                    "plural", getPlural(),
                    "singlePage", singlePageByName(name)));
    }

    @Override
    public boolean supports(GroupVersionKind gvk) {
        return this.gvk.equals(gvk);
    }
}
