package run.halo.app.localization;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import run.halo.app.infra.utils.PublicApiPaths;

/**
 * Applies an explicitly requested language to public API reads.
 *
 * <p>The filter is deliberately limited to safe GET and HEAD requests on the public API surface. Non-GET read
 * endpoints, such as search, can opt in with {@link PublicApiLocalizationFilter}. Console and user-center APIs are
 * excluded by {@link PublicApiPaths}.
 *
 * @since 2.28.0
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
@RequiredArgsConstructor
public class PublicApiLocalizationWebFilter implements WebFilter {
    private final PublicApiLocalizationResolver resolver;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (!isLocalizableRead(exchange)) {
            return chain.filter(exchange);
        }
        var languageTag = exchange.getRequest().getQueryParams().getFirst("locale");
        if (languageTag == null || languageTag.isBlank()) {
            return chain.filter(exchange);
        }
        return Mono.deferContextual(view -> {
            if (view.hasKey(LocalizationContext.class)) {
                return chain.filter(exchange);
            }
            return resolver.resolve(languageTag)
                    .map(Optional::of)
                    .defaultIfEmpty(Optional.empty())
                    .flatMap(context -> context.map(value -> chain.filter(exchange)
                                    .contextWrite(current -> current.put(LocalizationContext.class, value)))
                            .orElseGet(() -> chain.filter(exchange)));
        });
    }

    private static boolean isLocalizableRead(ServerWebExchange exchange) {
        var method = exchange.getRequest().getMethod();
        if (method != HttpMethod.GET && method != HttpMethod.HEAD) {
            return false;
        }
        return PublicApiPaths.matches(exchange.getRequest().getPath().pathWithinApplication());
    }
}
