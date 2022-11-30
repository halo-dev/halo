package run.halo.app.theme.router.strategy;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.extension.GVK;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.infra.SystemSetting;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.SinglePageFinder;
import run.halo.app.theme.router.ViewNameResolver;

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
    private final ViewNameResolver viewNameResolver;

    public SinglePageRouteStrategy(SinglePageFinder singlePageFinder,
        ViewNameResolver viewNameResolver) {
        this.singlePageFinder = singlePageFinder;
        this.viewNameResolver = viewNameResolver;
    }

    private String getPlural() {
        GVK annotation = SinglePage.class.getAnnotation(GVK.class);
        return annotation.plural();
    }

    @Override
    public HandlerFunction<ServerResponse> getHandler(SystemSetting.ThemeRouteRules routeRules,
        String name) {
        return request -> {
            Map<String, Object> model = new HashMap<>();
            model.put("groupVersionKind", gvk);
            model.put("plural", getPlural());
            model.put(ModelConst.TEMPLATE_ID, DefaultTemplateEnum.SINGLE_PAGE.getValue());

            return singlePageFinder.getByName(name).flatMap(singlePageVo -> {
                model.put("singlePage", singlePageVo);
                String template = singlePageVo.getSpec().getTemplate();
                return viewNameResolver.resolveViewNameOrDefault(request, template,
                        DefaultTemplateEnum.SINGLE_PAGE.getValue())
                    .flatMap(viewName -> ServerResponse.ok().render(viewName, model));
            });
        };
    }

    @Override
    public boolean supports(GroupVersionKind gvk) {
        return this.gvk.equals(gvk);
    }
}
