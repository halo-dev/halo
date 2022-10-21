package run.halo.app.theme.router.strategy;

import java.util.List;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.theme.DefaultTemplateEnum;

/**
 * The {@link ListPageRouteHandlerStrategy} for generate {@link HandlerFunction} specific to the
 * template.
 *
 * @author guqing
 * @since 2.0.0
 */
public interface ListPageRouteHandlerStrategy {

    HandlerFunction<ServerResponse> getHandler();

    List<String> getRouterPaths(String pattern);

    boolean supports(DefaultTemplateEnum template);
}
