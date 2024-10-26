package run.halo.app.security.authentication.oauth2;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * OAuth2 authentication token cache. Saving OAuth2AuthenticationToken is mainly for further binding
 * to Halo user.
 *
 * @author johnniang
 * @since 2.20.0
 */
public interface OAuth2AuthenticationTokenCache {

    /**
     * Save OAuth2AuthenticationToken into cache.
     *
     * @param exchange Server web exchange
     * @param oauth2Token OAuth2AuthenticationToken
     * @return empty
     */
    Mono<Void> saveToken(ServerWebExchange exchange, OAuth2AuthenticationToken oauth2Token);

    /**
     * Get OAuth2AuthenticationToken from cache.
     *
     * @param exchange Server web exchange
     * @return an {@link OAuth2AuthenticationToken} if present, empty otherwise
     */
    Mono<OAuth2AuthenticationToken> getToken(ServerWebExchange exchange);

    /**
     * Remove OAuth2AuthenticationToken from cache.
     *
     * @param exchange Server web exchange
     * @return empty
     */
    Mono<Void> removeToken(ServerWebExchange exchange);

}
