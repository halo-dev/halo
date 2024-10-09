package run.halo.app.security;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher.MatchResult;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.infra.utils.HaloUtils;

/**
 * Default authentication entry point.
 * See <a href="https://datatracker.ietf.org/doc/html/rfc7235#section-4.1">
 * https://datatracker.ietf.org/doc/html/rfc7235#section-4.1</a>
 * for more.
 *
 * @author johnniang
 */
public class DefaultServerAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    private final ServerWebExchangeMatcher xhrMatcher = exchange -> {
        if (HaloUtils.isXhr(exchange.getRequest().getHeaders())) {
            return MatchResult.match();
        }
        return MatchResult.notMatch();
    };

    private final RedirectServerAuthenticationEntryPoint redirectEntryPoint;

    public DefaultServerAuthenticationEntryPoint(ServerRequestCache serverRequestCache) {
        var entryPoint =
            new RedirectServerAuthenticationEntryPoint("/login?authentication_required");
        entryPoint.setRequestCache(serverRequestCache);
        this.redirectEntryPoint = entryPoint;
    }

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        return xhrMatcher.matches(exchange)
            .filter(MatchResult::isMatch)
            .switchIfEmpty(
                Mono.defer(() -> this.redirectEntryPoint.commence(exchange, ex).then(Mono.empty()))
            )
            .flatMap(match -> Mono.defer(
                () -> {
                    var response = exchange.getResponse();
                    var wwwAuthenticate = "FormLogin realm=\"console\"";
                    response.getHeaders().set(HttpHeaders.WWW_AUTHENTICATE, wwwAuthenticate);
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    return response.setComplete();
                }).then(Mono.empty())
            );
    }

}
