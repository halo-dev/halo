package run.halo.app.theme;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * The {@link ViewNameResolver} is used to resolve view name if the view name cannot be resolved
 * to the view, the default view name is returned.
 *
 * @author guqing
 * @since 2.10.2
 */
public interface ViewNameResolver {
    Mono<String> resolveViewNameOrDefault(ServerWebExchange exchange, String name,
        String defaultName);

    Mono<String> resolveViewNameOrDefault(ServerRequest request, String name,
        String defaultName);
}
