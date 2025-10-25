package run.halo.app.security;

import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers;

import java.net.URI;
import java.util.Collections;
import lombok.Getter;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.ServerRedirectStrategy;
import org.springframework.security.web.server.util.matcher.AndServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.MediaTypeServerWebExchangeMatcher;
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
class InitializeRedirectionWebFilter implements WebFilter {

    private final URI location = URI.create("/system/setup");

    private final ServerWebExchangeMatcher redirectMatcher;

    private final InitializationStateGetter initializationStateGetter;

    @Getter
    private ServerRedirectStrategy redirectStrategy = new DefaultServerRedirectStrategy();

    public InitializeRedirectionWebFilter(InitializationStateGetter initializationStateGetter) {
        this.initializationStateGetter = initializationStateGetter;
        var textHtmlOnly = new MediaTypeServerWebExchangeMatcher(MediaType.TEXT_HTML);
        textHtmlOnly.setIgnoredMediaTypes(Collections.singleton(MediaType.ALL));
        this.redirectMatcher = new AndServerWebExchangeMatcher(
            pathMatchers(HttpMethod.GET, "/", "/console/**", "/uc/**", "/login", "/signup"),
            textHtmlOnly
        );
    }

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
