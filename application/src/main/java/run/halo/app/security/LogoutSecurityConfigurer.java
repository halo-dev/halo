package run.halo.app.security;

import static org.springframework.security.config.web.server.SecurityWebFiltersOrder.LOGOUT_PAGE_GENERATING;
import static run.halo.app.security.authentication.WebExchangeMatchers.ignoringMediaTypeAll;

import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.logout.DelegatingServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.ui.LogoutPageGeneratingWebFilter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.security.authentication.SecurityConfigurer;
import run.halo.app.security.authentication.rememberme.RememberMeServices;

@Component
@RequiredArgsConstructor
public class LogoutSecurityConfigurer implements SecurityConfigurer {
    private final RememberMeServices rememberMeServices;
    private final ApplicationContext applicationContext;

    @Override
    public void configure(ServerHttpSecurity http) {
        var serverLogoutHandlers = getLogoutHandlers();
        http.logout(
            logout -> logout.logoutSuccessHandler(new LogoutSuccessHandler(serverLogoutHandlers)));
        http.addFilterAt(new LogoutPageGeneratingWebFilter(), LOGOUT_PAGE_GENERATING);
    }

    private class LogoutSuccessHandler implements ServerLogoutSuccessHandler {

        private final ServerLogoutSuccessHandler defaultHandler;
        private final ServerLogoutHandler logoutHandler;

        public LogoutSuccessHandler(ServerLogoutHandler... logoutHandler) {
            var defaultHandler = new RedirectServerLogoutSuccessHandler();
            defaultHandler.setLogoutSuccessUrl(URI.create("/console/?logout"));
            this.defaultHandler = defaultHandler;
            if (logoutHandler.length == 1) {
                this.logoutHandler = logoutHandler[0];
            } else {
                this.logoutHandler = new DelegatingServerLogoutHandler(logoutHandler);
            }
        }

        @Override
        public Mono<Void> onLogoutSuccess(WebFilterExchange exchange,
            Authentication authentication) {
            return logoutHandler.logout(exchange, authentication)
                .then(rememberMeServices.loginFail(exchange.getExchange()))
                .then(ignoringMediaTypeAll(MediaType.APPLICATION_JSON)
                    .matches(exchange.getExchange())
                    .flatMap(matchResult -> {
                        if (matchResult.isMatch()) {
                            var response = exchange.getExchange().getResponse();
                            response.setStatusCode(HttpStatus.NO_CONTENT);
                            return response.setComplete();
                        }
                        return defaultHandler.onLogoutSuccess(exchange, authentication);
                    })
                );
        }
    }

    private ServerLogoutHandler[] getLogoutHandlers() {
        return applicationContext.getBeansOfType(ServerLogoutHandler.class).values()
            .toArray(new ServerLogoutHandler[0]);
    }
}
