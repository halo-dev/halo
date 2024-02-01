package run.halo.app.security;

import java.util.Set;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.lang.NonNull;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
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

    private final Set<AdditionalWebFilter.Scope> matchScopes;

    /**
     * Creates an aggregated {@link SecurityWebFilterChain} using the provided original and
     * additional filters.
     *
     * @param matchScopes Only matched the given scopes will be added to the filter chain
     */
    public DynamicMatcherSecurityWebFilterChain(ExtensionGetter extensionGetter,
        SecurityWebFilterChain delegate,
        Set<AdditionalWebFilter.Scope> matchScopes) {
        Assert.isTrue(!CollectionUtils.isEmpty(matchScopes), "Match scopes must not be empty");
        Assert.notNull(extensionGetter, "Extension getter must not be null");
        Assert.notNull(delegate, "Delegate must not be null");
        this.delegate = delegate;
        this.extensionGetter = extensionGetter;
        this.matchScopes = matchScopes;
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
            .filter(additionalWebFilter -> {
                var scope = additionalWebFilter.getScope();
                return scope == AdditionalWebFilter.Scope.ALL || matchScopes.contains(scope);
            })
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
