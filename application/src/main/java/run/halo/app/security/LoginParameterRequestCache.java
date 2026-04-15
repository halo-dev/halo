package run.halo.app.security;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Request cache for login parameter.
 *
 * @author johnniang
 * @since 2.24.0
 */
public interface LoginParameterRequestCache {

    /**
     * Saves the parameter into request cache.
     *
     * @param exchange the server web exchange
     * @param parameterName the parameter name
     * @return empty mono
     */
    Mono<Void> saveParameter(ServerWebExchange exchange, String parameterName);

    /**
     * Removes the parameter from request cache.
     *
     * @param exchange the server web exchange
     * @param parameterName the parameter name
     * @return empty mono
     */
    Mono<Void> removeParameter(ServerWebExchange exchange, String parameterName);

    /**
     * Gets the parameter from request cache.
     *
     * @param exchange the server web exchange
     * @param parameterName the parameter name
     * @return a mono string or empty mono
     */
    Mono<String> getParameter(ServerWebExchange exchange, String parameterName);

}
