package run.halo.app.security.authentication.twofactor;

import java.net.URI;
import org.springframework.context.MessageSource;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.ServerRedirectStrategy;
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

    private static final URI REDIRECT_LOCATION = URI.create("/challenges/two-factor/totp");

    /**
     * Because we don't want to cache the request before redirecting to the 2FA page,
     * ServerRedirectStrategy is used to redirect the request.
     */
    private final ServerRedirectStrategy redirectStrategy = new DefaultServerRedirectStrategy();

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
            .switchIfEmpty(
                redirectStrategy.sendRedirect(exchange, REDIRECT_LOCATION).then(Mono.empty())
            )
            .flatMap(isXhr -> {
                var errorResponse = Exceptions.createErrorResponse(
                    new TwoFactorAuthRequiredException(REDIRECT_LOCATION),
                    null, exchange, messageSource);
                return ServerResponse.status(errorResponse.getStatusCode())
                    .bodyValue(errorResponse.getBody())
                    .flatMap(response -> response.writeTo(exchange, context));
            });
    }
}
