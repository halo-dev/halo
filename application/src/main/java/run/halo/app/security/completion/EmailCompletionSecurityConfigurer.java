package run.halo.app.security.completion;

import org.springframework.core.annotation.Order;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.core.user.service.UserService;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.security.authentication.SecurityConfigurer;

/** Registers {@link EmailCompletionFilter} after authentication. */
@Component
@Order(10)
class EmailCompletionSecurityConfigurer implements SecurityConfigurer {

    private final SystemConfigFetcher systemConfigFetcher;
    private final UserService userService;
    private final ServerRequestCache serverRequestCache;
    private final ServerResponse.Context context;

    public EmailCompletionSecurityConfigurer(
            SystemConfigFetcher systemConfigFetcher,
            UserService userService,
            ServerRequestCache serverRequestCache,
            ServerResponse.Context context) {
        this.systemConfigFetcher = systemConfigFetcher;
        this.userService = userService;
        this.serverRequestCache = serverRequestCache;
        this.context = context;
    }

    @Override
    public void configure(ServerHttpSecurity http) {
        var filter = new EmailCompletionFilter(systemConfigFetcher, userService, serverRequestCache, context);
        http.addFilterAfter(filter, SecurityWebFiltersOrder.AUTHENTICATION);
    }
}
