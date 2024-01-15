package run.halo.app.security.authentication.twofactor;

import java.net.URI;
import org.springframework.context.MessageSource;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.ServerRedirectStrategy;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.infra.exception.Exceptions;

@Component
public class DefaultTwoFactorAuthResponseHandler implements TwoFactorAuthResponseHandler {

    private final ServerRedirectStrategy redirectStrategy = new DefaultServerRedirectStrategy();

    private static final String REDIRECT_LOCATION = "/console/login/mfa";

    private final MessageSource messageSource;

    private final ServerResponse.Context context;

    private static final ServerWebExchangeMatcher XHR_MATCHER = exchange -> {
        if (exchange.getRequest().getHeaders().getOrEmpty("X-Requested-With")
            .contains("XMLHttpRequest")) {
            return ServerWebExchangeMatcher.MatchResult.match();
        }
        return ServerWebExchangeMatcher.MatchResult.notMatch();
    };

    public DefaultTwoFactorAuthResponseHandler(MessageSource messageSource,
        ServerResponse.Context context) {
        this.messageSource = messageSource;
        this.context = context;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange) {
        return XHR_MATCHER.matches(exchange)
            .filter(ServerWebExchangeMatcher.MatchResult::isMatch)
            .switchIfEmpty(Mono.defer(
                () -> redirectStrategy.sendRedirect(exchange, URI.create(REDIRECT_LOCATION))
                    .then(Mono.empty())))
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
