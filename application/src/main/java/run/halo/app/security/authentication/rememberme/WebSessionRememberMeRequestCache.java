package run.halo.app.security.authentication.rememberme;

import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

/**
 * An implementation of {@link RememberMeRequestCache} that stores remember-me parameter in
 * {@link WebSession}.
 *
 * @author johnniang
 * @since 2.20.0
 */
public class WebSessionRememberMeRequestCache implements RememberMeRequestCache {

    private static final String SESSION_ATTRIBUTE_NAME =
        RememberMeRequestCache.class + ".REMEMBER_ME";

    private static final String DEFAULT_PARAMETER = "remember-me";

    @Override
    public Mono<Void> saveRememberMe(ServerWebExchange exchange) {
        return resolveFromQuery(exchange)
            .switchIfEmpty(resolveFromForm(exchange))
            .flatMap(rememberMe -> exchange.getSession().doOnNext(
                session -> session.getAttributes().put(SESSION_ATTRIBUTE_NAME, rememberMe))
            )
            .then();
    }

    @Override
    public Mono<Boolean> isRememberMe(ServerWebExchange exchange) {
        return resolveFromQuery(exchange)
            .switchIfEmpty(resolveFromForm(exchange))
            .switchIfEmpty(resolveFromSession(exchange))
            .defaultIfEmpty(false);
    }

    @Override
    public Mono<Void> removeRememberMe(ServerWebExchange exchange) {
        return exchange.getSession()
            .doOnNext(session -> session.getAttributes().remove(SESSION_ATTRIBUTE_NAME))
            .then();
    }

    private Mono<Boolean> resolveFromQuery(ServerWebExchange exchange) {
        return Mono.justOrEmpty(exchange.getRequest().getQueryParams().getFirst(DEFAULT_PARAMETER))
            .map(Boolean::parseBoolean);
    }

    private Mono<Boolean> resolveFromForm(ServerWebExchange exchange) {
        return exchange.getFormData()
            .mapNotNull(form -> form.getFirst(DEFAULT_PARAMETER))
            .map(Boolean::parseBoolean);
    }

    private Mono<Boolean> resolveFromSession(ServerWebExchange exchange) {
        return exchange.getSession()
            .mapNotNull(session -> session.getAttribute(SESSION_ATTRIBUTE_NAME))
            .filter(Boolean.class::isInstance)
            .cast(Boolean.class);
    }
}
