package run.halo.app.security.authentication.rememberme;

import lombok.RequiredArgsConstructor;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import run.halo.app.security.authentication.SecurityConfigurer;

@Component
@RequiredArgsConstructor
public class RememberMeConfigurer implements SecurityConfigurer {
    private final RememberMeServices rememberMeServices;
    private final ServerSecurityContextRepository securityContextRepository;
    private final CookieSignatureKeyResolver cookieSignatureKeyResolver;

    @Override
    public void configure(ServerHttpSecurity http) {
        http.addFilterAt(
            new RememberMeAuthenticationFilter(securityContextRepository,
                rememberMeServices, authenticationManager()),
            SecurityWebFiltersOrder.AUTHENTICATION);
    }

    RememberMeAuthenticationManager authenticationManager() {
        return new RememberMeAuthenticationManager(cookieSignatureKeyResolver);
    }
}
