package run.halo.app.security.authentication.oauth2;

import org.springframework.security.core.Authentication;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * OAuth2 login handler enhancer.
 *
 * @author johnniang
 * @since 2.20.0
 */
public interface OAuth2LoginHandlerEnhancer {

    Mono<Void> loginSuccess(ServerWebExchange exchange, Authentication authentication);

}
