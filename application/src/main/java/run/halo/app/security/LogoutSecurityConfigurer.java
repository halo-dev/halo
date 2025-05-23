package run.halo.app.security;

import static run.halo.app.security.authentication.WebExchangeMatchers.ignoringMediaTypeAll;

import java.net.URI;
import java.util.ArrayList;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.ServerRedirectStrategy;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.logout.DelegatingServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.SecurityContextServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.security.web.server.savedrequest.WebSessionServerRequestCache;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.user.service.UserLoginOrLogoutProcessing;
import run.halo.app.core.user.service.UserService;
import run.halo.app.infra.actuator.GlobalInfoService;
import run.halo.app.security.authentication.SecurityConfigurer;
import run.halo.app.theme.router.ModelConst;

@Component
@RequiredArgsConstructor
@Order(0)
class LogoutSecurityConfigurer implements SecurityConfigurer {

    private final ApplicationContext applicationContext;

    private final UserLoginOrLogoutProcessing userLoginOrLogoutProcessing;

    private final ServerRequestCache serverRequestCache = new HaloServerRequestCache();

    private final ServerSecurityContextRepository securityContextRepository;

    @Override
    public void configure(ServerHttpSecurity http) {
        http.logout(logout -> logout
            .logoutHandler(getLogoutHandler())
            .logoutSuccessHandler(new LogoutSuccessHandler())
        );
    }

    private ServerLogoutHandler getLogoutHandler() {
        var defaultLogoutHandler = new SecurityContextServerLogoutHandler();
        defaultLogoutHandler.setSecurityContextRepository(securityContextRepository);
        var logoutHandlers = new ArrayList<ServerLogoutHandler>();
        logoutHandlers.add(defaultLogoutHandler);
        applicationContext.getBeanProvider(ServerLogoutHandler.class)
            .forEach(logoutHandlers::add);
        if (logoutHandlers.size() == 1) {
            return logoutHandlers.getFirst();
        }
        return new DelegatingServerLogoutHandler(logoutHandlers);
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
            .filter((request, next) ->
                // Save request before handling the logout
                serverRequestCache.saveRequest(request.exchange()).then(next.handle(request))
            )
            .build();
    }


    private class LogoutSuccessHandler implements ServerLogoutSuccessHandler {

        private final ServerLogoutSuccessHandler defaultHandler;

        public LogoutSuccessHandler() {
            var redirectHandler = new RequestCacheRedirectLogoutSuccessHandler();
            redirectHandler.setRequestCache(serverRequestCache);
            this.defaultHandler = redirectHandler;
        }

        @Override
        public Mono<Void> onLogoutSuccess(WebFilterExchange exchange,
            Authentication authentication) {
            return userLoginOrLogoutProcessing.logoutProcessing(authentication.getName())
                .then(ignoringMediaTypeAll(MediaType.APPLICATION_JSON)
                    .matches(exchange.getExchange())
                    .filter(ServerWebExchangeMatcher.MatchResult::isMatch)
                    .switchIfEmpty(Mono.defer(() ->
                        defaultHandler.onLogoutSuccess(exchange, authentication).then(Mono.empty())
                    ))
                    .flatMap(match -> {
                        var response = exchange.getExchange().getResponse();
                        response.setStatusCode(HttpStatus.NO_CONTENT);
                        return response.setComplete();
                    })
                );
        }
    }

    private static class RequestCacheRedirectLogoutSuccessHandler
        implements ServerLogoutSuccessHandler {

        private final ServerRedirectStrategy redirectStrategy = new DefaultServerRedirectStrategy();

        private URI location = URI.create("/login?logout");

        private ServerRequestCache requestCache = new WebSessionServerRequestCache();

        public RequestCacheRedirectLogoutSuccessHandler() {
        }

        public RequestCacheRedirectLogoutSuccessHandler(String location) {
            this.location = URI.create(location);
        }

        public void setRequestCache(@NonNull ServerRequestCache requestCache) {
            Assert.notNull(requestCache, "requestCache cannot be null");
            this.requestCache = requestCache;
        }

        @Override
        public Mono<Void> onLogoutSuccess(
            WebFilterExchange exchange, Authentication authentication
        ) {
            return this.requestCache.getRedirectUri(exchange.getExchange())
                .defaultIfEmpty(this.location)
                .flatMap(location ->
                    this.redirectStrategy.sendRedirect(exchange.getExchange(), location)
                );
        }

    }
}
