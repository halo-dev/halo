package run.halo.app.security.authentication.twofactor;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public interface TwoFactorAuthResponseHandler {

    Mono<Void> handle(ServerWebExchange exchange);

}
