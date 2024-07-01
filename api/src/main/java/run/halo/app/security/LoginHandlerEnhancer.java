package run.halo.app.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * <p>Halo uses this interface to enhance the processing of login success, such as device management
 * and remember me, etc. The login method of the plugin extension needs to call this interface in
 * the processing method of login success to ensure the normal operation of some enhanced
 * functions.</p>
 *
 * @author guqing
 * @since 2.17.0
 */
public interface LoginHandlerEnhancer {

    /**
     * Invoked when login success.
     *
     * @param exchange The exchange.
     * @param successfulAuthentication The successful authentication.
     */
    Mono<Void> onLoginSuccess(ServerWebExchange exchange, Authentication successfulAuthentication);

    /**
     * Invoked when login fails.
     *
     * @param exchange The exchange.
     * @param exception the reason authentication failed
     */
    Mono<Void> onLoginFailure(ServerWebExchange exchange, AuthenticationException exception);
}
