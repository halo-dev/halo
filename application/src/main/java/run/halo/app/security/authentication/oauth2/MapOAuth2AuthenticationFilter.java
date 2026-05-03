package run.halo.app.security.authentication.oauth2;

import static run.halo.app.security.authentication.oauth2.HaloOAuth2AuthenticationToken.authenticated;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.user.service.UserConnectionService;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.Metadata;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.security.LoginHandlerEnhancer;

/**
 * A filter to map OAuth2 authentication to authenticated user.
 *
 * @author johnniang
 * @since 2.20.0
 */
@Slf4j
class MapOAuth2AuthenticationFilter implements WebFilter {

    private static final String PRE_AUTHENTICATION =
        MapOAuth2AuthenticationFilter.class.getName() + ".PRE_AUTHENTICATION";

    private final UserConnectionService connectionService;

    private final ServerSecurityContextRepository securityContextRepository;

    @Setter
    private OAuth2AuthenticationTokenCache authenticationCache =
        new WebSessionOAuth2AuthenticationTokenCache();

    private final ReactiveUserDetailsService userDetailsService;

    private final LoginHandlerEnhancer loginHandlerEnhancer;

    private final UserService userService;

    private final SystemConfigFetcher systemConfigFetcher;

    @Setter
    private AuthenticationTrustResolver authenticationTrustResolver
        = new AuthenticationTrustResolverImpl();

    public MapOAuth2AuthenticationFilter(
        ServerSecurityContextRepository securityContextRepository,
        UserConnectionService connectionService,
        ReactiveUserDetailsService userDetailsService,
        LoginHandlerEnhancer loginHandlerEnhancer,
        UserService userService,
        SystemConfigFetcher systemConfigFetcher) {
        this.connectionService = connectionService;
        this.securityContextRepository = securityContextRepository;
        this.userDetailsService = userDetailsService;
        this.loginHandlerEnhancer = loginHandlerEnhancer;
        this.userService = userService;
        this.systemConfigFetcher = systemConfigFetcher;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
            .mapNotNull(SecurityContext::getAuthentication)
            .filter(authenticationTrustResolver::isAuthenticated)
            .doOnNext(
                // cache the pre-authentication
                authentication -> exchange.getAttributes().put(PRE_AUTHENTICATION, authentication)
            )
            .then(chain.filter(exchange))
            .then(Mono.defer(() -> ReactiveSecurityContextHolder.getContext()
                .mapNotNull(SecurityContext::getAuthentication)
                .filter(OAuth2AuthenticationToken.class::isInstance)
                .cast(OAuth2AuthenticationToken.class)
                .flatMap(oauth2Token -> {
                    var registrationId = oauth2Token.getAuthorizedClientRegistrationId();
                    var oauth2User = oauth2Token.getPrincipal();
                    return connectionService.updateUserConnectionIfPresent(
                            registrationId, oauth2User
                        )
                        .switchIfEmpty(Mono.defer(() -> {
                            var preAuthObj = exchange.getAttribute(PRE_AUTHENTICATION);
                            if (preAuthObj instanceof Authentication preAuth
                                && !(preAuth instanceof OAuth2AuthenticationToken)
                                && authenticationTrustResolver.isAuthenticated(preAuth)) {
                                return connectionService.createUserConnection(
                                    preAuth.getName(), registrationId, oauth2User
                                );
                            }
                            return Mono.empty();
                        }))
                        .switchIfEmpty(Mono.defer(
                            () -> autoRegisterUser(registrationId, oauth2User)
                        ))
                        // user bound and remap the authentication
                        .flatMap(connection ->
                            userDetailsService.findByUsername(connection.getSpec().getUsername())
                        )
                        .map(userDetails -> authenticated(userDetails, oauth2Token))
                        .flatMap(haloOAuth2Token -> {
                            var securityContext = new SecurityContextImpl(haloOAuth2Token);
                            return securityContextRepository.save(exchange, securityContext)
                                .then(
                                    loginHandlerEnhancer.onLoginSuccess(exchange, haloOAuth2Token)
                                );
                            // because this happens after the filter, there is no need to
                            // write SecurityContext to the context
                        });
                })
                .then())
            );
    }

    Mono<run.halo.app.core.extension.UserConnection> autoRegisterUser(
        String registrationId, OAuth2User oauth2User) {
        return systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class)
            .flatMap(setting -> {
                var defaultRole = setting.getDefaultRole();
                if (!StringUtils.hasText(defaultRole)) {
                    return Mono.error(new IllegalStateException(
                        "No default role configured for OAuth2 auto-registration"
                    ));
                }
                var attrs = oauth2User.getAttributes();

                var user = new User();
                user.setMetadata(new Metadata());
                user.getMetadata().setName("user-" + UUID.randomUUID().toString().substring(0, 8));
                user.setSpec(new User.UserSpec());
                user.getSpec().setDisplayName(extractDisplayName(attrs));
                user.getSpec().setEmail(extractEmail(attrs));
                user.getSpec().setEmailVerified(StringUtils.hasText(extractEmail(attrs)));
                user.getSpec().setRegisteredAt(Instant.now());

                return userService.createUser(user, Set.of(defaultRole))
                    .flatMap(createdUser -> {
                        var username = createdUser.getMetadata().getName();
                        return connectionService.createUserConnection(
                            username, registrationId, oauth2User
                        );
                    });
            });
    }

    private static String extractDisplayName(Map<String, Object> attrs) {
        for (var key : new String[]{"name", "login", "preferred_username"}) {
            var value = attrs.get(key);
            if (value instanceof String s && StringUtils.hasText(s)) {
                return s;
            }
        }
        return null;
    }

    private static String extractEmail(Map<String, Object> attrs) {
        var value = attrs.get("email");
        if (value instanceof String s && StringUtils.hasText(s)) {
            return s.toLowerCase();
        }
        return null;
    }

}
