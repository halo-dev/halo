package run.halo.app.security.authentication.rememberme;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * An interface for caching remember-me parameter in request for further handling. Especially
 * useful for two-factor authentication.
 *
 * @author johnniang
 * @since 2.20.0
 */
public interface RememberMeRequestCache {

    /**
     * Save remember-me parameter or form into cache.
     *
     * @param exchange exchange
     * @return empty to return
     */
    Mono<Void> saveRememberMe(ServerWebExchange exchange);

    /**
     * Check if remember-me parameter exists in cache.
     *
     * @param exchange exchange
     * @return true if remember-me exists, false otherwise
     */
    Mono<Boolean> isRememberMe(ServerWebExchange exchange);

    /**
     * Remove remember-me parameter from cache.
     *
     * @param exchange exchange
     * @return empty to return
     */
    Mono<Void> removeRememberMe(ServerWebExchange exchange);

}
