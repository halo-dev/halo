package run.halo.app.security.authentication.twofactor;

import org.springframework.context.MessageSource;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.security.authentication.SecurityConfigurer;
import run.halo.app.security.authentication.twofactor.totp.TotpAuthService;
import run.halo.app.security.authentication.twofactor.totp.TotpAuthenticationFilter;

@Component
public class TwoFactorAuthSecurityConfigurer implements SecurityConfigurer {

    private final ServerSecurityContextRepository securityContextRepository;
    private final TotpAuthService totpAuthService;

    private final ServerResponse.Context context;

    private final MessageSource messageSource;

    public TwoFactorAuthSecurityConfigurer(
        ServerSecurityContextRepository securityContextRepository,
        TotpAuthService totpAuthService, ServerResponse.Context context,
        MessageSource messageSource) {
        this.securityContextRepository = securityContextRepository;
        this.totpAuthService = totpAuthService;
        this.context = context;
        this.messageSource = messageSource;
    }

    @Override
    public void configure(ServerHttpSecurity http) {
        var filter = new TotpAuthenticationFilter(securityContextRepository, totpAuthService,
            context, messageSource);
        http.addFilterAfter(filter, SecurityWebFiltersOrder.AUTHENTICATION);
    }

}
