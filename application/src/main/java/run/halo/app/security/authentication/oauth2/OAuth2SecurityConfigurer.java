package run.halo.app.security.authentication.oauth2;

import org.springframework.core.annotation.Order;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import run.halo.app.core.user.service.UserConnectionService;
import run.halo.app.security.authentication.SecurityConfigurer;

/**
 * OAuth2 security configurer.
 *
 * @author johnniang
 * @since 2.20.0
 */
@Component
@Order(0)
class OAuth2SecurityConfigurer implements SecurityConfigurer {

    private final ServerSecurityContextRepository securityContextRepository;

    private final UserConnectionService connectionService;

    private final ReactiveUserDetailsService userDetailsService;

    public OAuth2SecurityConfigurer(ServerSecurityContextRepository securityContextRepository,
        UserConnectionService connectionService, ReactiveUserDetailsService userDetailsService) {
        this.securityContextRepository = securityContextRepository;
        this.connectionService = connectionService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void configure(ServerHttpSecurity http) {
        var mapOAuth2Filter = new MapOAuth2AuthenticationFilter(
            securityContextRepository, connectionService, userDetailsService
        );
        http.addFilterBefore(mapOAuth2Filter, SecurityWebFiltersOrder.AUTHENTICATION);
    }
}
