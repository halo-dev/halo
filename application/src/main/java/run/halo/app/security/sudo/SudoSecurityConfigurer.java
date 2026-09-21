package run.halo.app.security.sudo;

import lombok.RequiredArgsConstructor;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.security.authentication.SecurityConfigurer;

/**
 * Registers {@link SudoModeWebFilter} after authentication is populated.
 *
 * @author johnniang
 * @since 2.27.0
 */
@Component
@RequiredArgsConstructor
class SudoSecurityConfigurer implements SecurityConfigurer {

    private final SudoService sudoService;

    private final ServerResponse.Context responseContext;

    @Override
    public void configure(ServerHttpSecurity http) {
        http.addFilterAfter(
                new SudoModeWebFilter(sudoService, responseContext), SecurityWebFiltersOrder.ANONYMOUS_AUTHENTICATION);
    }
}
