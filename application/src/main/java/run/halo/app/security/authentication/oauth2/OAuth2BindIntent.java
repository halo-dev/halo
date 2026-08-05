package run.halo.app.security.authentication.oauth2;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** A short-lived, one-shot indication that the user explicitly chose to bind an OAuth2 identity. */
public interface OAuth2BindIntent {

    Mono<Void> save(ServerWebExchange exchange);

    Mono<Boolean> consume(ServerWebExchange exchange);

    Mono<Void> clear(ServerWebExchange exchange);
}
