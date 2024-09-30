package run.halo.app.security.authentication.twofactor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import reactor.core.publisher.Mono;
import run.halo.app.security.LoginHandlerEnhancer;

@Slf4j
public class TotpAuthenticationSuccessHandler implements ServerAuthenticationSuccessHandler {

    private final LoginHandlerEnhancer loginEnhancer;

    private final ServerAuthenticationSuccessHandler successHandler =
        new RedirectServerAuthenticationSuccessHandler("/uc");

    public TotpAuthenticationSuccessHandler(LoginHandlerEnhancer loginEnhancer) {
        this.loginEnhancer = loginEnhancer;
    }

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange,
        Authentication authentication) {
        return loginEnhancer.onLoginSuccess(webFilterExchange.getExchange(), authentication)
            .then(successHandler.onAuthenticationSuccess(webFilterExchange, authentication));
    }
}
