package run.halo.app.security;

import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.anyExchange;
import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers;

import java.util.ArrayList;
import org.springframework.context.MessageSource;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.web.access.server.BearerTokenServerAccessDeniedHandler;
import org.springframework.security.oauth2.server.resource.web.server.authentication.ServerBearerTokenAuthenticationConverter;
import org.springframework.security.web.server.DelegatingServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.AuthenticationConverterServerWebExchangeMatcher;
import org.springframework.security.web.server.authorization.HttpStatusServerAccessDeniedHandler;
import org.springframework.security.web.server.authorization.ServerWebExchangeDelegatingServerAccessDeniedHandler;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.security.authentication.SecurityConfigurer;
import run.halo.app.security.authentication.twofactor.TwoFactorAuthenticationEntryPoint;

@Component
@Order(0)
public class ExceptionSecurityConfigurer implements SecurityConfigurer {

    private final MessageSource messageSource;

    private final ServerResponse.Context context;

    private final ServerRequestCache serverRequestCache;

    public ExceptionSecurityConfigurer(MessageSource messageSource,
        ServerResponse.Context context,
        ServerRequestCache serverRequestCache) {
        this.messageSource = messageSource;
        this.context = context;
        this.serverRequestCache = serverRequestCache;
    }

    @Override
    public void configure(ServerHttpSecurity http) {
        http.exceptionHandling(exception -> {
            var accessDeniedHandlers =
                new ArrayList<ServerWebExchangeDelegatingServerAccessDeniedHandler.DelegateEntry>(
                    3
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
                    pathMatchers(HttpMethod.GET, "/login", "/signup"),
                    new RedirectAccessDeniedHandler("/uc")
                ));
            accessDeniedHandlers.add(
                new ServerWebExchangeDelegatingServerAccessDeniedHandler.DelegateEntry(
                    anyExchange(),
                    new HttpStatusServerAccessDeniedHandler(HttpStatus.FORBIDDEN)
                )
            );

            var entryPoints =
                new ArrayList<DelegatingServerAuthenticationEntryPoint.DelegateEntry>(2);
            entryPoints.add(new DelegatingServerAuthenticationEntryPoint.DelegateEntry(
                TwoFactorAuthenticationEntryPoint.MATCHER,
                new TwoFactorAuthenticationEntryPoint(messageSource, context)
            ));
            entryPoints.add(new DelegatingServerAuthenticationEntryPoint.DelegateEntry(
                anyExchange(),
                new DefaultServerAuthenticationEntryPoint(serverRequestCache)
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
