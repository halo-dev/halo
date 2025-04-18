package run.halo.app.security.switchuser;

import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.SwitchUserWebFilter;
import org.springframework.stereotype.Component;
import run.halo.app.security.HaloRedirectAuthenticationSuccessHandler;
import run.halo.app.security.authentication.SecurityConfigurer;

/**
 * Switch user configurer.
 *
 * @author johnniang
 */
@Component
class SwitchUserConfigurer implements SecurityConfigurer {

    private final ReactiveUserDetailsService userDetailsService;

    SwitchUserConfigurer(ReactiveUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void configure(ServerHttpSecurity http) {
        var successHandler = new HaloRedirectAuthenticationSuccessHandler("/console");
        var failureHandler =
            new RedirectServerAuthenticationFailureHandler("/login?error=impersonate");
        var filter = new SwitchUserWebFilter(userDetailsService, successHandler, failureHandler);
        http.addFilterAfter(filter, SecurityWebFiltersOrder.AUTHORIZATION);
    }

}
