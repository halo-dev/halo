package run.halo.app.security;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.lang.NonNull;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;

/**
 * A {@link SecurityWebFilterChain} that leverages a {@link ServerWebExchangeMatcher} to
 * determine which {@link WebFilter} to execute.
 *
 * @author guqing
 * @since 2.4.0
 */
public class DynamicMatcherSecurityWebFilterChain implements SecurityWebFilterChain {

    private final SecurityWebFilterChain delegate;

    private final ExtensionGetter extensionGetter;

    public DynamicMatcherSecurityWebFilterChain(ExtensionGetter extensionGetter,
        SecurityWebFilterChain delegate) {
        this.delegate = delegate;
        this.extensionGetter = extensionGetter;
    }

    @Override
    public Mono<Boolean> matches(ServerWebExchange exchange) {
        return delegate.matches(exchange);
    }

    @Override
    public Flux<WebFilter> getWebFilters() {
        return Flux.merge(delegate.getWebFilters(), getAdditionalFilters())
            .sort(new AnnotationAwareOrderComparator());
    }

    private Flux<WebFilter> getAdditionalFilters() {
        return extensionGetter.getEnabledExtensionByDefinition(AdditionalWebFilter.class)
            .map(additionalWebFilter -> new OrderedWebFilter(additionalWebFilter,
                additionalWebFilter.getOrder())
            );
    }

    private record OrderedWebFilter(WebFilter webFilter, int order) implements WebFilter, Ordered {

        @Override
        @NonNull
        public Mono<Void> filter(@NonNull ServerWebExchange exchange,
            @NonNull WebFilterChain chain) {
            return this.webFilter.filter(exchange, chain);
        }

        @Override
        public int getOrder() {
            return this.order;
        }
    }
}
