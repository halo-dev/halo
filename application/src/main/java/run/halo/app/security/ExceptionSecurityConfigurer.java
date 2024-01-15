package run.halo.app.security;

import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.web.access.server.BearerTokenServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import run.halo.app.security.authentication.SecurityConfigurer;

@Component
public class ExceptionSecurityConfigurer implements SecurityConfigurer {

    @Override
    public void configure(ServerHttpSecurity http) {
        http.exceptionHandling(exception -> {
            var accessDeniedHandler = new BearerTokenServerAccessDeniedHandler();
            var entryPoint = new DefaultServerAuthenticationEntryPoint();
            exception
                .authenticationEntryPoint(entryPoint)
                .accessDeniedHandler(accessDeniedHandler);
        });
    }

}
