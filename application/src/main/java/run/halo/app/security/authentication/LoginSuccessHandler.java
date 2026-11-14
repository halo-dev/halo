package run.halo.app.security.authentication;

import static run.halo.app.security.SecurityConstant.REMEMBER_ME_PARAMETER_NAME;

import java.net.URI;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.ServerRedirectStrategy;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.security.LoginHandlerEnhancer;
import run.halo.app.security.LoginParameterRequestCache;
import run.halo.app.security.authentication.twofactor.TwoFactorAuthentication;

/**
 * Shared authentication success handler for all login methods (password, email-code, phone-code, etc.).
 *
 * <p>Performs credential erasure, delegates to {@link LoginHandlerEnhancer}, then redirects form submissions to
 * {@code /uc} or returns the principal as JSON for XHR requests.
 *
 * @author johnniang
 * @since 2.26.0
 */
public class LoginSuccessHandler implements ServerAuthenticationSuccessHandler {

    private final ServerResponse.Context context;

    private final LoginHandlerEnhancer loginHandlerEnhancer;

    private final LoginParameterRequestCache parameterRequestCache;

    private final ServerRedirectStrategy redirectStrategy = new DefaultServerRedirectStrategy();

    private final ServerAuthenticationSuccessHandler defaultSuccessHandler =
            new RedirectServerAuthenticationSuccessHandler("/uc");

    public LoginSuccessHandler(
            ServerResponse.Context context,
            LoginHandlerEnhancer loginHandlerEnhancer,
            LoginParameterRequestCache parameterRequestCache) {
        this.context = context;
        this.loginHandlerEnhancer = loginHandlerEnhancer;
        this.parameterRequestCache = parameterRequestCache;
    }

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        if (authentication instanceof TwoFactorAuthentication) {
            var exchange = webFilterExchange.getExchange();
            return parameterRequestCache
                    .saveParameter(exchange, REMEMBER_ME_PARAMETER_NAME)
                    .then(redirectStrategy.sendRedirect(exchange, URI.create("/challenges/two-factor/totp")));
        }

        if (authentication instanceof CredentialsContainer container) {
            container.eraseCredentials();
        }

        ServerWebExchangeMatcher xhrMatcher = exchange -> {
            if (exchange.getRequest()
                    .getHeaders()
                    .getOrEmpty("X-Requested-With")
                    .contains("XMLHttpRequest")) {
                return ServerWebExchangeMatcher.MatchResult.match();
            }
            return ServerWebExchangeMatcher.MatchResult.notMatch();
        };

        var exchange = webFilterExchange.getExchange();
        return loginHandlerEnhancer
                .onLoginSuccess(exchange, authentication)
                .then(xhrMatcher
                        .matches(exchange)
                        .filter(ServerWebExchangeMatcher.MatchResult::isMatch)
                        .switchIfEmpty(Mono.defer(() -> defaultSuccessHandler
                                .onAuthenticationSuccess(webFilterExchange, authentication)
                                .then(Mono.empty())))
                        .flatMap(isXhr -> ServerResponse.ok()
                                .bodyValue(authentication.getPrincipal())
                                .flatMap(response -> response.writeTo(exchange, context))));
    }
}
