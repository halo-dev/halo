package run.halo.app.security.authentication.twofactor;

import java.net.URI;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.ServerRedirectStrategy;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.security.authentication.exception.TooManyRequestsException;

@Component
public class TotpAuthenticationFailureHandler implements ServerAuthenticationFailureHandler {

    private static final URI DEFAULT_REDIRECT = URI.create("/challenges/two-factor/totp?error=invalid-totp-code");

    private static final URI RATE_LIMIT_REDIRECT = URI.create("/challenges/two-factor/totp?error=rate-limit-exceeded");

    private final ServerRedirectStrategy redirectStrategy = new DefaultServerRedirectStrategy();

    @Override
    public Mono<Void> onAuthenticationFailure(WebFilterExchange webFilterExchange, AuthenticationException exception) {
        var location = exception instanceof TooManyRequestsException ? RATE_LIMIT_REDIRECT : DEFAULT_REDIRECT;
        return redirectStrategy.sendRedirect(webFilterExchange.getExchange(), location);
    }
}
