package run.halo.app.security.authentication.twofactor;

import java.net.URI;
import org.springframework.context.MessageSource;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.infra.exception.Exceptions;

public class TwoFactorAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    public static ServerWebExchangeMatcher MATCHER = exchange -> exchange.getPrincipal()
        .filter(TwoFactorAuthentication.class::isInstance)
        .flatMap(a -> ServerWebExchangeMatcher.MatchResult.match())
        .switchIfEmpty(ServerWebExchangeMatcher.MatchResult.notMatch());

    private static final String REDIRECT_LOCATION = "/challenges/two-factor/totp";

    private final RedirectServerAuthenticationEntryPoint redirectEntryPoint =
        new RedirectServerAuthenticationEntryPoint(REDIRECT_LOCATION);

    private final MessageSource messageSource;

    private final ServerResponse.Context context;

    private static final ServerWebExchangeMatcher XHR_MATCHER = exchange -> {
        if (exchange.getRequest().getHeaders().getOrEmpty("X-Requested-With")
            .contains("XMLHttpRequest")) {
            return ServerWebExchangeMatcher.MatchResult.match();
        }
        return ServerWebExchangeMatcher.MatchResult.notMatch();
    };

    public TwoFactorAuthenticationEntryPoint(MessageSource messageSource,
        ServerResponse.Context context) {
        this.messageSource = messageSource;
        this.context = context;
    }

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        return XHR_MATCHER.matches(exchange)
            .filter(ServerWebExchangeMatcher.MatchResult::isMatch)
            .switchIfEmpty(redirectEntryPoint.commence(exchange, ex).then(Mono.empty()))
            .flatMap(isXhr -> {
                var errorResponse = Exceptions.createErrorResponse(
                    new TwoFactorAuthRequiredException(URI.create(REDIRECT_LOCATION)),
                    null, exchange, messageSource);
                return ServerResponse.status(errorResponse.getStatusCode())
                    .bodyValue(errorResponse.getBody())
                    .flatMap(response -> response.writeTo(exchange, context));
            });
    }
}
