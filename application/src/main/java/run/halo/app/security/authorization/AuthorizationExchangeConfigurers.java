package run.halo.app.security.authorization;

import java.util.Collections;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.util.matcher.AndServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.MediaTypeServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.core.user.service.RoleService;
import run.halo.app.security.authentication.SecurityConfigurer;

/**
 * Authorization exchange configurers.
 *
 * @author johnniang
 * @since 2.20.0
 */
@Component
class AuthorizationExchangeConfigurers {

    private final AuthenticationTrustResolver authenticationTrustResolver =
        new AuthenticationTrustResolverImpl();

    @Bean
    @Order(0)
    SecurityConfigurer apiAuthorizationConfigurer(RoleService roleService) {
        return http -> http.authorizeExchange(
            spec -> spec.pathMatchers("/api/**", "/apis/**", "/actuator/**")
                .access(new RequestInfoAuthorizationManager(roleService)));
    }

    @Bean
    @Order(100)
    SecurityConfigurer unauthenticatedAuthorizationConfigurer() {
        return http -> http.authorizeExchange(spec -> {
            spec.pathMatchers(HttpMethod.GET, "/login", "/signup")
                .access((authentication, context) -> authentication.map(
                        a -> !authenticationTrustResolver.isAuthenticated(a)
                    )
                    .defaultIfEmpty(true)
                    .map(AuthorizationDecision::new));
        });
    }

    @Bean
    @Order(200)
    SecurityConfigurer preAuthenticationAuthorizationConfigurer() {
        return http -> http.authorizeExchange(spec -> spec.pathMatchers(
            "/login/**",
            "/challenges/**",
            "/password-reset/**",
            "/signup"
        ).permitAll());
    }

    @Bean
    @Order(300)
    SecurityConfigurer authenticatedAuthorizationConfigurer() {
        // Anonymous user is not allowed
        return http -> http.authorizeExchange(
            spec -> spec.pathMatchers(
                "/console/**",
                "/uc/**",
                "/logout"
            ).authenticated()
        );
    }

    @Bean
    @Order(400)
    SecurityConfigurer anonymousOrAuthenticatedAuthorizationConfigurer() {
        return http -> http.authorizeExchange(
            spec -> spec.matchers(createHtmlMatcher()).access((authentication, context) ->
                // we only need to check the authentication is authenticated
                // because we treat anonymous user as authenticated
                authentication.map(Authentication::isAuthenticated)
                    .map(AuthorizationDecision::new)
                    .switchIfEmpty(Mono.fromSupplier(() -> new AuthorizationDecision(false)))
            )
        );
    }

    @Bean
    @Order
    SecurityConfigurer permitAllAuthorizationConfigurer() {
        return http -> http.authorizeExchange(spec -> spec.anyExchange().permitAll());
    }

    private static ServerWebExchangeMatcher createHtmlMatcher() {
        ServerWebExchangeMatcher get =
            ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, "/**");
        ServerWebExchangeMatcher notFavicon = new NegatedServerWebExchangeMatcher(
            ServerWebExchangeMatchers.pathMatchers("/favicon.*"));
        MediaTypeServerWebExchangeMatcher html =
            new MediaTypeServerWebExchangeMatcher(MediaType.TEXT_HTML);
        html.setIgnoredMediaTypes(Collections.singleton(MediaType.ALL));
        return new AndServerWebExchangeMatcher(get, notFavicon, html);
    }

}
