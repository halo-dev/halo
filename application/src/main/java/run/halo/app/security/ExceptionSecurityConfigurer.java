package run.halo.app.security;

import java.util.ArrayList;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.web.access.server.BearerTokenServerAccessDeniedHandler;
import org.springframework.security.oauth2.server.resource.web.server.authentication.ServerBearerTokenAuthenticationConverter;
import org.springframework.security.web.server.DelegatingServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.AuthenticationConverterServerWebExchangeMatcher;
import org.springframework.security.web.server.authorization.HttpStatusServerAccessDeniedHandler;
import org.springframework.security.web.server.authorization.ServerWebExchangeDelegatingServerAccessDeniedHandler;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.security.authentication.SecurityConfigurer;
import run.halo.app.security.authentication.twofactor.TwoFactorAuthenticationEntryPoint;

@Component
public class ExceptionSecurityConfigurer implements SecurityConfigurer {

    private final MessageSource messageSource;

    private final ServerResponse.Context context;

    public ExceptionSecurityConfigurer(MessageSource messageSource,
        ServerResponse.Context context) {
        this.messageSource = messageSource;
        this.context = context;
    }

    @Override
    public void configure(ServerHttpSecurity http) {
        http.exceptionHandling(exception -> {
            var accessDeniedHandlers =
                new ArrayList<ServerWebExchangeDelegatingServerAccessDeniedHandler.DelegateEntry>(
                    2
                );
            accessDeniedHandlers.add(
                new ServerWebExchangeDelegatingServerAccessDeniedHandler.DelegateEntry(
                    new AuthenticationConverterServerWebExchangeMatcher(
                        new ServerBearerTokenAuthenticationConverter()
                    ),
                    new BearerTokenServerAccessDeniedHandler()
                ));
            accessDeniedHandlers.add(
                new ServerWebExchangeDelegatingServerAccessDeniedHandler.DelegateEntry(
                    ServerWebExchangeMatchers.anyExchange(),
                    new HttpStatusServerAccessDeniedHandler(HttpStatus.FORBIDDEN)
                )
            );

            var entryPoints =
                new ArrayList<DelegatingServerAuthenticationEntryPoint.DelegateEntry>(3);
            entryPoints.add(new DelegatingServerAuthenticationEntryPoint.DelegateEntry(
                TwoFactorAuthenticationEntryPoint.MATCHER,
                new TwoFactorAuthenticationEntryPoint(messageSource, context)
            ));
            entryPoints.add(new DelegatingServerAuthenticationEntryPoint.DelegateEntry(
                exchange -> ServerWebExchangeMatcher.MatchResult.match(),
                new DefaultServerAuthenticationEntryPoint()
            ));

            exception.authenticationEntryPoint(
                    new DelegatingServerAuthenticationEntryPoint(entryPoints)
                )
                .accessDeniedHandler(
                    new ServerWebExchangeDelegatingServerAccessDeniedHandler(accessDeniedHandlers)
                );
        });
    }

}
