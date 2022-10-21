package run.halo.app.theme.router.strategy;

import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.infra.SystemSetting;

/**
 * The {@link DetailsPageRouteHandlerStrategy} for generate {@link HandlerFunction} specific to the
 * template.
 *
 * @author guqing
 * @since 2.0.0
 */
public interface DetailsPageRouteHandlerStrategy {

    HandlerFunction<ServerResponse> getHandler(SystemSetting.ThemeRouteRules routeRules,
        String name);

    boolean supports(GroupVersionKind gvk);
}
