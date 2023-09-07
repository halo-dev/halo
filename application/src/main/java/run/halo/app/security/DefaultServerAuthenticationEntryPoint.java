package run.halo.app.security;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Default authentication entry point.
 * See <a href="https://datatracker.ietf.org/doc/html/rfc7235#section-4.1">
 * https://datatracker.ietf.org/doc/html/rfc7235#section-4.1</a>
 * for more.
 *
 * @author johnniang
 */
public class DefaultServerAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        return Mono.defer(() -> {
            var response = exchange.getResponse();
            var wwwAuthenticate = "FormLogin realm=\"console\"";
            response.getHeaders().set(HttpHeaders.WWW_AUTHENTICATE, wwwAuthenticate);
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        });
    }

}
