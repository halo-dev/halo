package run.halo.app.security.authentication.twofactor;

import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationFailureHandler;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.stereotype.Component;
import run.halo.app.security.LoginHandlerEnhancer;
import run.halo.app.security.authentication.SecurityConfigurer;
import run.halo.app.security.authentication.twofactor.totp.TotpAuthService;
import run.halo.app.security.authentication.twofactor.totp.TotpAuthenticationManager;
import run.halo.app.security.authentication.twofactor.totp.TotpCodeAuthenticationConverter;

@Component
@Order(0)
public class TwoFactorAuthSecurityConfigurer implements SecurityConfigurer {

    private final ServerSecurityContextRepository securityContextRepository;

    private final TotpAuthService totpAuthService;

    private final LoginHandlerEnhancer loginHandlerEnhancer;

    private final ServerRequestCache serverRequestCache;

    public TwoFactorAuthSecurityConfigurer(
        ServerSecurityContextRepository securityContextRepository,
        TotpAuthService totpAuthService, LoginHandlerEnhancer loginHandlerEnhancer,
        ServerRequestCache serverRequestCache
    ) {
        this.securityContextRepository = securityContextRepository;
        this.totpAuthService = totpAuthService;
        this.loginHandlerEnhancer = loginHandlerEnhancer;
        this.serverRequestCache = serverRequestCache;
    }

    @Override
    public void configure(ServerHttpSecurity http) {
        var authManager = new TotpAuthenticationManager(totpAuthService);
        var filter = new AuthenticationWebFilter(authManager);
        filter.setRequiresAuthenticationMatcher(
            pathMatchers(HttpMethod.POST, "/challenges/two-factor/totp")
        );
        filter.setSecurityContextRepository(securityContextRepository);
        filter.setServerAuthenticationConverter(new TotpCodeAuthenticationConverter());
        filter.setAuthenticationSuccessHandler(
            new TotpAuthenticationSuccessHandler(loginHandlerEnhancer, serverRequestCache)
        );
        filter.setAuthenticationFailureHandler(
            new RedirectServerAuthenticationFailureHandler("/challenges/two-factor/totp?error")
        );
        http.addFilterAt(filter, SecurityWebFiltersOrder.AUTHENTICATION);
    }

}
