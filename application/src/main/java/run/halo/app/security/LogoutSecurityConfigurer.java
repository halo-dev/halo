package run.halo.app.security;

import static org.springframework.security.config.web.server.SecurityWebFiltersOrder.LOGOUT_PAGE_GENERATING;
import static run.halo.app.security.authentication.WebExchangeMatchers.ignoringMediaTypeAll;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.ui.LogoutPageGeneratingWebFilter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.security.authentication.SecurityConfigurer;

@Component
public class LogoutSecurityConfigurer implements SecurityConfigurer {

    @Override
    public void configure(ServerHttpSecurity http) {
        http.logout(logout -> logout.logoutSuccessHandler(new LogoutSuccessHandler()));
        http.addFilterAt(new LogoutPageGeneratingWebFilter(), LOGOUT_PAGE_GENERATING);
    }

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
                        var response = exchange.getExchange().getResponse();
                        response.setStatusCode(HttpStatus.NO_CONTENT);
                        return response.setComplete();
                    }
                    return defaultHandler.onLogoutSuccess(exchange, authentication);
                });
        }
    }
}
