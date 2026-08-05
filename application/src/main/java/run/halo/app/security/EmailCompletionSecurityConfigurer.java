package run.halo.app.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.core.user.service.UserService;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.security.authentication.SecurityConfigurer;

@Component
@RequiredArgsConstructor
class EmailCompletionSecurityConfigurer implements SecurityConfigurer {

    private final SystemConfigFetcher systemConfigFetcher;

    private final UserService userService;

    private final ServerRequestCache requestCache;

    private final ServerResponse.Context responseContext;

    @Override
    public void configure(ServerHttpSecurity http) {
        var emailCompletionFilter =
                new EmailCompletionFilter(systemConfigFetcher, userService, requestCache, responseContext);
        http.addFilterAfter(emailCompletionFilter, SecurityWebFiltersOrder.ANONYMOUS_AUTHENTICATION);
    }
}
