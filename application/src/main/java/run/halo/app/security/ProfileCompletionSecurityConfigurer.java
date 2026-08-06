package run.halo.app.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.security.authentication.SecurityConfigurer;
import run.halo.app.security.profile.ProfileCompletionFlow;

@Component
@RequiredArgsConstructor
class ProfileCompletionSecurityConfigurer implements SecurityConfigurer {

    private final ProfileCompletionFlow profileCompletionFlow;

    private final ServerRequestCache requestCache;

    private final ServerResponse.Context responseContext;

    @Override
    public void configure(ServerHttpSecurity http) {
        var profileCompletionFilter = new ProfileCompletionFilter(profileCompletionFlow, requestCache, responseContext);
        http.addFilterAfter(profileCompletionFilter, SecurityWebFiltersOrder.ANONYMOUS_AUTHENTICATION);
    }
}
