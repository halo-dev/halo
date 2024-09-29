package run.halo.app.security.authentication.oauth2;

import static run.halo.app.security.authentication.oauth2.HaloOAuth2AuthenticationToken.authenticated;

import java.net.URI;
import lombok.Setter;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.ServerRedirectStrategy;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.logout.SecurityContextServerLogoutHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutHandler;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import run.halo.app.core.user.service.UserConnectionService;

/**
 * A filter to map OAuth2 authentication to authenticated user.
 *
 * @author johnniang
 * @since 2.20.0
 */
class MapOAuth2AuthenticationFilter implements WebFilter {

    private static final String PRE_AUTHENTICATION =
        MapOAuth2AuthenticationFilter.class.getName() + ".PRE_AUTHENTICATION";

    private final UserConnectionService connectionService;

    private final ServerSecurityContextRepository securityContextRepository;

    @Setter
    private OAuth2AuthenticationTokenCache authenticationCache =
        new WebSessionOAuth2AuthenticationTokenCache();

    private final ReactiveUserDetailsService userDetailsService;

    private final ServerLogoutHandler logoutHandler;

    private final ServerRedirectStrategy redirectStrategy = new DefaultServerRedirectStrategy();

    @Setter
    private AuthenticationTrustResolver authenticationTrustResolver
        = new AuthenticationTrustResolverImpl();

    public MapOAuth2AuthenticationFilter(
        ServerSecurityContextRepository securityContextRepository,
        UserConnectionService connectionService,
        ReactiveUserDetailsService userDetailsService) {
        this.connectionService = connectionService;
        this.securityContextRepository = securityContextRepository;
        this.userDetailsService = userDetailsService;
        var logoutHandler = new SecurityContextServerLogoutHandler();
        logoutHandler.setSecurityContextRepository(securityContextRepository);
        this.logoutHandler = logoutHandler;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .filter(authenticationTrustResolver::isAuthenticated)
            .doOnNext(
                // cache the pre-authentication
                authentication -> exchange.getAttributes().put(PRE_AUTHENTICATION, authentication)
            )
            .then(chain.filter(exchange))
            .then(Mono.defer(() -> ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(OAuth2AuthenticationToken.class::isInstance)
                .cast(OAuth2AuthenticationToken.class)
                .flatMap(oauth2Token -> {
                    var registrationId = oauth2Token.getAuthorizedClientRegistrationId();
                    var oauth2User = oauth2Token.getPrincipal();
                    // check the connection
                    return connectionService.updateUserConnectionIfPresent(
                            registrationId, oauth2User
                        )
                        .switchIfEmpty(Mono.defer(() -> {
                            var preAuthenticationObject = exchange.getAttribute(PRE_AUTHENTICATION);
                            if (preAuthenticationObject instanceof Authentication preAuth
                                && authenticationTrustResolver.isAuthenticated(preAuth)) {
                                // check the authentication again
                                // try to bind the user automatically
                                return connectionService.createUserConnection(
                                    preAuth.getName(), registrationId, oauth2User
                                );
                            }
                            // save the OAuth2Authentication into session
                            return authenticationCache.saveToken(exchange, oauth2Token)
                                .then(Mono.defer(() -> {
                                    var webFilterExchange = new WebFilterExchange(exchange, chain);
                                    // clear the security context
                                    return logoutHandler.logout(webFilterExchange, oauth2Token);
                                }))
                                .then(Mono.defer(() -> redirectStrategy.sendRedirect(exchange,
                                    URI.create("/login?oauth2_bind")
                                )))
                                // skip handling
                                .then(Mono.empty());
                        }))
                        // user bound and remap the authentication
                        .flatMap(connection ->
                            userDetailsService.findByUsername(connection.getSpec().getUsername())
                        )
                        .map(userDetails -> authenticated(userDetails, oauth2Token))
                        .flatMap(haloOAuth2Token -> {
                            var securityContext = new SecurityContextImpl(haloOAuth2Token);
                            return securityContextRepository.save(exchange, securityContext);
                            // because this happens after the filter, there is no need to
                            // write SecurityContext to the context
                        });
                })
                .then())
            );
    }

}
