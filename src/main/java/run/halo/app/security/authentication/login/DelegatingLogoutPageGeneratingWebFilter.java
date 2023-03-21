package run.halo.app.security.authentication.login;

import org.springframework.lang.NonNull;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.web.server.ui.LogoutPageGeneratingWebFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import run.halo.app.security.AdditionalWebFilter;

/**
 * Generates a default log out page.
 *
 * @author guqing
 * @since 2.4.0
 */
@Component
public class DelegatingLogoutPageGeneratingWebFilter implements AdditionalWebFilter {
    private final LogoutPageGeneratingWebFilter logoutPageGeneratingWebFilter =
        new LogoutPageGeneratingWebFilter();

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        return logoutPageGeneratingWebFilter.filter(exchange, chain);
    }

    @Override
    public int getOrder() {
        return SecurityWebFiltersOrder.LOGOUT_PAGE_GENERATING.getOrder();
    }
}
