package run.halo.app.theme.router;

import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * The {@link TemplateRouterStrategy} for generate {@link RouterFunction} specific to the template.
 *
 * @author guqing
 * @since 2.0.0
 */
@FunctionalInterface
public interface TemplateRouterStrategy {

    RouterFunction<ServerResponse> getRouteFunction(String template, String pattern);
}
