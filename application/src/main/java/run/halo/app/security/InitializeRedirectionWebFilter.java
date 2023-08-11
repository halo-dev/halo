package run.halo.app.security;

import java.net.URI;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.ServerRedirectStrategy;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import run.halo.app.infra.InitializationStateGetter;

/**
 * A web filter that will redirect user to set up page if system is not initialized.
 *
 * @author guqing
 * @since 2.5.2
 */
@Component
@RequiredArgsConstructor
public class InitializeRedirectionWebFilter implements WebFilter {
    private final URI location = URI.create("/console");
    private final ServerWebExchangeMatcher redirectMatcher =
        new PathPatternParserServerWebExchangeMatcher("/", HttpMethod.GET);

    private final InitializationStateGetter initializationStateGetter;

    @Getter
    private ServerRedirectStrategy redirectStrategy = new DefaultServerRedirectStrategy();

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        return redirectMatcher.matches(exchange)
            .flatMap(matched -> {
                if (!matched.isMatch()) {
                    return chain.filter(exchange);
                }
                return initializationStateGetter.userInitialized()
                    .defaultIfEmpty(false)
                    .flatMap(initialized -> {
                        if (initialized) {
                            return chain.filter(exchange);
                        }
                        // Redirect to set up page if system is not initialized.
                        return redirectStrategy.sendRedirect(exchange, location);
                    });
            });
    }

    public void setRedirectStrategy(ServerRedirectStrategy redirectStrategy) {
        Assert.notNull(redirectStrategy, "redirectStrategy cannot be null");
        this.redirectStrategy = redirectStrategy;
    }
}
