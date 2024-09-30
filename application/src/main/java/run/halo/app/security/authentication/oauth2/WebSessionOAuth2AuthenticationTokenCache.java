package run.halo.app.security.authentication.oauth2;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * WebSession cache implementation of {@link OAuth2AuthenticationTokenCache}.
 *
 * @author johnniang
 * @since 2.20.0
 */
public class WebSessionOAuth2AuthenticationTokenCache implements OAuth2AuthenticationTokenCache {

    private static final String SESSION_ATTRIBUTE_KEY =
        OAuth2AuthenticationTokenCache.class + ".OAUTH2_TOKEN";

    @Override
    public Mono<Void> saveToken(ServerWebExchange exchange, OAuth2AuthenticationToken oauth2Token) {
        return exchange.getSession()
            .doOnNext(session -> {
                session.getAttributes().put(SESSION_ATTRIBUTE_KEY, oauth2Token);
            })
            .then();
    }

    @Override
    public Mono<OAuth2AuthenticationToken> getToken(ServerWebExchange exchange) {
        return exchange.getSession()
            .mapNotNull(session -> session.getAttribute(SESSION_ATTRIBUTE_KEY))
            .filter(OAuth2AuthenticationToken.class::isInstance)
            .cast(OAuth2AuthenticationToken.class);
    }

    @Override
    public Mono<Void> removeToken(ServerWebExchange exchange) {
        return exchange.getSession()
            .doOnNext(session -> session.getAttributes().remove(SESSION_ATTRIBUTE_KEY))
            .then();
    }

}
