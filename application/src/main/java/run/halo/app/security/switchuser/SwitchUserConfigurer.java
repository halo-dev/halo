package run.halo.app.security.switchuser;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.SwitchUserWebFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import run.halo.app.core.user.service.UserService;
import run.halo.app.infra.actuator.GlobalInfoService;
import run.halo.app.security.HaloRedirectAuthenticationSuccessHandler;
import run.halo.app.security.authentication.SecurityConfigurer;
import run.halo.app.theme.router.ModelConst;

/**
 * Switch user configurer.
 *
 * @author johnniang
 */
@Component
class SwitchUserConfigurer implements SecurityConfigurer {

    private final ReactiveUserDetailsService userDetailsService;

    SwitchUserConfigurer(ReactiveUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void configure(ServerHttpSecurity http) {
        var successHandler = new HaloRedirectAuthenticationSuccessHandler("/console");
        var failureHandler = new RedirectServerAuthenticationFailureHandler("/login?error=impersonate");
        var filter = new SwitchUserWebFilter(userDetailsService, successHandler, failureHandler);
        http.addFilterAfter(filter, SecurityWebFiltersOrder.AUTHORIZATION);
    }

    @Bean
    RouterFunction<ServerResponse> switchUserPage(UserService userService, GlobalInfoService globalInfoService) {
        return RouterFunctions.route()
                .GET("/login/impersonate", request -> {
                    var username = request.queryParam("username")
                            .filter(name -> !name.isBlank())
                            .orElseThrow(() -> new ServerWebInputException("Query parameter username is required."));
                    var contextPath = request.exchange()
                            .getRequest()
                            .getPath()
                            .contextPath()
                            .value();
                    var action = contextPath + "/login/impersonate?username="
                            + URLEncoder.encode(username, StandardCharsets.UTF_8);
                    return userService
                            .getUser(username)
                            .flatMap(user -> ServerResponse.ok()
                                    .render(
                                            "switch-user",
                                            Map.of(
                                                    "globalInfo",
                                                    globalInfoService.getGlobalInfo(),
                                                    "action",
                                                    action,
                                                    "user",
                                                    user,
                                                    "exit",
                                                    false)));
                })
                .GET("/logout/impersonate", request -> {
                    var contextPath = request.exchange()
                            .getRequest()
                            .getPath()
                            .contextPath()
                            .value();
                    return ReactiveSecurityContextHolder.getContext()
                            .map(SecurityContext::getAuthentication)
                            .map(Authentication::getName)
                            .flatMap(userService::getUser)
                            .flatMap(user -> ServerResponse.ok()
                                    .render(
                                            "switch-user",
                                            Map.of(
                                                    "globalInfo",
                                                    globalInfoService.getGlobalInfo(),
                                                    "action",
                                                    contextPath + "/logout/impersonate",
                                                    "user",
                                                    user,
                                                    "exit",
                                                    true)));
                })
                .before(request -> {
                    request.exchange().getAttributes().put(ModelConst.NO_CACHE, true);
                    return request;
                })
                .build();
    }
}
