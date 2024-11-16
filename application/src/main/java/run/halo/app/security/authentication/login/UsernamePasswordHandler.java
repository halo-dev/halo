package run.halo.app.security.authentication.login;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static run.halo.app.infra.exception.Exceptions.createErrorResponse;
import static run.halo.app.security.authentication.WebExchangeMatchers.ignoringMediaTypeAll;

import java.net.URI;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.ServerRedirectStrategy;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.ErrorResponse;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.security.LoginHandlerEnhancer;
import run.halo.app.security.authentication.exception.TooManyRequestsException;
import run.halo.app.security.authentication.rememberme.RememberMeRequestCache;
import run.halo.app.security.authentication.rememberme.WebSessionRememberMeRequestCache;
import run.halo.app.security.authentication.twofactor.TwoFactorAuthentication;

@Slf4j
public class UsernamePasswordHandler implements ServerAuthenticationSuccessHandler,
    ServerAuthenticationFailureHandler {

    private final ServerResponse.Context context;

    private final MessageSource messageSource;

    private final LoginHandlerEnhancer loginHandlerEnhancer;

    private final ServerRedirectStrategy redirectStrategy = new DefaultServerRedirectStrategy();

    @Setter
    private RememberMeRequestCache rememberMeRequestCache = new WebSessionRememberMeRequestCache();

    private final ServerAuthenticationSuccessHandler defaultSuccessHandler =
        new RedirectServerAuthenticationSuccessHandler("/uc");

    public UsernamePasswordHandler(ServerResponse.Context context, MessageSource messageSource,
        LoginHandlerEnhancer loginHandlerEnhancer) {
        this.context = context;
        this.messageSource = messageSource;
        this.loginHandlerEnhancer = loginHandlerEnhancer;
    }

    @Override
    public Mono<Void> onAuthenticationFailure(WebFilterExchange webFilterExchange,
        AuthenticationException exception) {
        var exchange = webFilterExchange.getExchange();
        return loginHandlerEnhancer.onLoginFailure(exchange, exception)
            .then(ignoringMediaTypeAll(APPLICATION_JSON)
                .matches(exchange)
                .filter(ServerWebExchangeMatcher.MatchResult::isMatch)
                .switchIfEmpty(Mono.defer(
                    () -> {
                        URI location = URI.create("/login?error&method=local");
                        if (exception instanceof BadCredentialsException) {
                            location = URI.create("/login?error=invalid-credential&method=local");
                        }
                        if (exception instanceof TooManyRequestsException) {
                            location = URI.create("/login?error=rate-limit-exceeded&method=local");
                        }
                        return redirectStrategy.sendRedirect(exchange, location);
                    }).then(Mono.empty())
                )
                .flatMap(matchResult -> handleAuthenticationException(exception, exchange)));
    }

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange,
        Authentication authentication) {
        if (authentication instanceof TwoFactorAuthentication) {
            return rememberMeRequestCache.saveRememberMe(webFilterExchange.getExchange())
                // Do not use RedirectServerAuthenticationSuccessHandler to redirect
                // because it will use request cache to redirect
                .then(redirectStrategy.sendRedirect(webFilterExchange.getExchange(),
                    URI.create("/challenges/two-factor/totp"))
                );
        }

        if (authentication instanceof CredentialsContainer container) {
            container.eraseCredentials();
        }

        ServerWebExchangeMatcher xhrMatcher = exchange -> {
            if (exchange.getRequest().getHeaders().getOrEmpty("X-Requested-With")
                .contains("XMLHttpRequest")) {
                return ServerWebExchangeMatcher.MatchResult.match();
            }
            return ServerWebExchangeMatcher.MatchResult.notMatch();
        };

        var exchange = webFilterExchange.getExchange();
        return loginHandlerEnhancer.onLoginSuccess(webFilterExchange.getExchange(), authentication)
            .then(xhrMatcher.matches(exchange)
                .filter(ServerWebExchangeMatcher.MatchResult::isMatch)
                .switchIfEmpty(Mono.defer(
                    () -> defaultSuccessHandler.onAuthenticationSuccess(webFilterExchange,
                            authentication)
                        .then(Mono.empty())))
                .flatMap(isXhr -> ServerResponse.ok()
                    .bodyValue(authentication.getPrincipal())
                    .flatMap(response -> response.writeTo(exchange, context))));
    }

    private Mono<Void> handleAuthenticationException(Throwable exception,
        ServerWebExchange exchange) {
        var errorResponse = createErrorResponse(exception, UNAUTHORIZED, exchange, messageSource);
        return writeErrorResponse(errorResponse, exchange);
    }

    private Mono<Void> writeErrorResponse(ErrorResponse errorResponse,
        ServerWebExchange exchange) {
        return ServerResponse.status(errorResponse.getStatusCode())
            .contentType(APPLICATION_JSON)
            .bodyValue(errorResponse.getBody())
            .flatMap(response -> response.writeTo(exchange, context));
    }

}
