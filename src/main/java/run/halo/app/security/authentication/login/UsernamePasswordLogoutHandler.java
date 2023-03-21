package run.halo.app.security.authentication.login;

import static run.halo.app.security.authentication.WebExchangeMatchers.ignoringMediaTypeAll;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.logout.LogoutWebFilter;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import org.springframework.security.web.server.authentication.logout.SecurityContextServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import run.halo.app.security.AdditionalWebFilter;

/**
 * Logout handler for username password authentication.
 *
 * @author guqing
 * @since 2.4.0
 */
@Component
public class UsernamePasswordLogoutHandler implements AdditionalWebFilter {
    private final ServerSecurityContextRepository securityContextRepository;
    private final LogoutWebFilter logoutWebFilter;

    /**
     * Constructs a {@link UsernamePasswordLogoutHandler} with the given
     * {@link ServerSecurityContextRepository}.
     * It will create a {@link LogoutWebFilter} instance and configure it.
     *
     * @param securityContextRepository a {@link ServerSecurityContextRepository} instance
     */
    public UsernamePasswordLogoutHandler(
        ServerSecurityContextRepository securityContextRepository) {
        this.securityContextRepository = securityContextRepository;

        this.logoutWebFilter = new LogoutWebFilter();
        configureLogoutWebFilter(logoutWebFilter);
    }

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        return logoutWebFilter.filter(exchange, chain);
    }

    @Override
    public int getOrder() {
        return SecurityWebFiltersOrder.LOGOUT.getOrder();
    }

    void configureLogoutWebFilter(LogoutWebFilter filter) {
        var securityContextServerLogoutHandler = new SecurityContextServerLogoutHandler();
        securityContextServerLogoutHandler.setSecurityContextRepository(securityContextRepository);
        filter.setLogoutHandler(securityContextServerLogoutHandler);
        filter.setLogoutSuccessHandler(new LogoutSuccessHandler());
    }

    /**
     * Success handler for logout.
     *
     * @author johnniang
     */
    public static class LogoutSuccessHandler implements ServerLogoutSuccessHandler {

        private final ServerLogoutSuccessHandler defaultHandler;

        public LogoutSuccessHandler() {
            var defaultHandler = new RedirectServerLogoutSuccessHandler();
            defaultHandler.setLogoutSuccessUrl(URI.create("/console/?logout"));
            this.defaultHandler = defaultHandler;
        }

        @Override
        public Mono<Void> onLogoutSuccess(WebFilterExchange exchange,
            Authentication authentication) {
            return ignoringMediaTypeAll(MediaType.APPLICATION_JSON).matches(exchange.getExchange())
                .flatMap(matchResult -> {
                    if (matchResult.isMatch()) {
                        exchange.getExchange().getResponse().setStatusCode(HttpStatus.OK);
                        return Mono.empty();
                    }
                    return defaultHandler.onLogoutSuccess(exchange, authentication);
                });
        }
    }
}
