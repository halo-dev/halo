package run.halo.app.security.authentication;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static run.halo.app.infra.exception.Exceptions.createErrorResponse;
import static run.halo.app.security.authentication.WebExchangeMatchers.ignoringMediaTypeAll;

import java.net.URI;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.ServerRedirectStrategy;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.web.ErrorResponse;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.security.LoginHandlerEnhancer;
import run.halo.app.security.authentication.exception.TooManyRequestsException;

/**
 * Shared authentication failure handler for all login methods.
 *
 * <p>Redirects form submissions to the login page with the configured {@code loginMethod} query parameter (e.g.
 * {@code /login?error=invalid-credential&method=email-code}), or returns a JSON error body for XHR requests.
 *
 * @author johnniang
 * @since 2.26.0
 */
public class LoginFailureHandler implements ServerAuthenticationFailureHandler {

    private final String loginMethod;

    private final ServerResponse.Context context;

    private final MessageSource messageSource;

    private final LoginHandlerEnhancer loginHandlerEnhancer;

    private final ServerRedirectStrategy redirectStrategy = new DefaultServerRedirectStrategy();

    public LoginFailureHandler(
            String loginMethod,
            ServerResponse.Context context,
            MessageSource messageSource,
            LoginHandlerEnhancer loginHandlerEnhancer) {
        this.loginMethod = loginMethod;
        this.context = context;
        this.messageSource = messageSource;
        this.loginHandlerEnhancer = loginHandlerEnhancer;
    }

    @Override
    public Mono<Void> onAuthenticationFailure(WebFilterExchange webFilterExchange, AuthenticationException exception) {
        var exchange = webFilterExchange.getExchange();
        return loginHandlerEnhancer
                .onLoginFailure(exchange, exception)
                .then(ignoringMediaTypeAll(APPLICATION_JSON).matches(exchange).flatMap(matchResult -> {
                    if (matchResult.isMatch()) {
                        return handleAuthenticationException(exception, exchange);
                    }
                    var location = buildRedirectLocation(exception);
                    return redirectStrategy.sendRedirect(exchange, location);
                }));
    }

    private URI buildRedirectLocation(AuthenticationException exception) {
        if (exception instanceof DisabledException) {
            return URI.create("/login?error=account-disabled&method=" + loginMethod);
        }
        if (exception instanceof BadCredentialsException) {
            return URI.create("/login?error=invalid-credential&method=" + loginMethod);
        }
        if (exception instanceof TooManyRequestsException) {
            return URI.create("/login?error=rate-limit-exceeded&method=" + loginMethod);
        }
        return URI.create("/login?error&method=" + loginMethod);
    }

    private Mono<Void> handleAuthenticationException(Throwable exception, ServerWebExchange exchange) {
        var errorResponse = createErrorResponse(exception, UNAUTHORIZED, exchange, messageSource);
        return writeErrorResponse(errorResponse, exchange);
    }

    private Mono<Void> writeErrorResponse(ErrorResponse errorResponse, ServerWebExchange exchange) {
        return ServerResponse.status(errorResponse.getStatusCode())
                .contentType(APPLICATION_JSON)
                .bodyValue(errorResponse.getBody())
                .flatMap(response -> response.writeTo(exchange, context));
    }
}
