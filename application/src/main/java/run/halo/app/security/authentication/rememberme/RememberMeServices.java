package run.halo.app.security.authentication.rememberme;

import org.springframework.security.core.Authentication;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public interface RememberMeServices {

    Mono<Authentication> autoLogin(ServerWebExchange exchange);

    Mono<Void> loginFail(ServerWebExchange exchange);

    Mono<Void> loginSuccess(ServerWebExchange exchange, Authentication successfulAuthentication);
}
