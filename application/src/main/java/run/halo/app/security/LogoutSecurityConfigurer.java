package run.halo.app.security;

import static run.halo.app.security.authentication.WebExchangeMatchers.ignoringMediaTypeAll;

import java.net.URI;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.logout.DelegatingServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.user.service.UserService;
import run.halo.app.infra.actuator.GlobalInfoService;
import run.halo.app.security.authentication.SecurityConfigurer;
import run.halo.app.security.authentication.rememberme.RememberMeServices;
import run.halo.app.theme.router.ModelConst;

@Component
@RequiredArgsConstructor
@Order(0)
public class LogoutSecurityConfigurer implements SecurityConfigurer {
    private final RememberMeServices rememberMeServices;
    private final ApplicationContext applicationContext;

    @Override
    public void configure(ServerHttpSecurity http) {
        var serverLogoutHandlers = getLogoutHandlers();
        http.logout(
            logout -> logout.logoutSuccessHandler(new LogoutSuccessHandler(serverLogoutHandlers))
        );
    }

    private class LogoutSuccessHandler implements ServerLogoutSuccessHandler {

        private final ServerLogoutSuccessHandler defaultHandler;
        private final ServerLogoutHandler logoutHandler;

        public LogoutSuccessHandler(ServerLogoutHandler... logoutHandler) {
            var defaultHandler = new RedirectServerLogoutSuccessHandler();
            defaultHandler.setLogoutSuccessUrl(URI.create("/login?logout"));
            this.defaultHandler = defaultHandler;
            if (logoutHandler.length == 1) {
                this.logoutHandler = logoutHandler[0];
            } else {
                this.logoutHandler = new DelegatingServerLogoutHandler(logoutHandler);
            }
        }

        @Bean
        RouterFunction<ServerResponse> logoutPage(
            UserService userService,
            GlobalInfoService globalInfoService
        ) {
            return RouterFunctions.route()
                .GET("/logout", request -> {
                    var user = ReactiveSecurityContextHolder.getContext()
                        .map(SecurityContext::getAuthentication)
                        .map(Authentication::getName)
                        .flatMap(userService::getUser);
                    var exchange = request.exchange();
                    var contextPath = exchange.getRequest().getPath().contextPath().value();
                    return ServerResponse.ok().render("logout", Map.of(
                        "globalInfo", globalInfoService.getGlobalInfo(),
                        "action", contextPath + "/logout",
                        "user", user
                    ));
                })
                .before(request -> {
                    request.exchange().getAttributes().put(ModelConst.NO_CACHE, true);
                    return request;
                })
                .build();
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
