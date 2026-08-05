package run.halo.app.security.authentication.oauth2;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.security.LoginHandlerEnhancer;

/** Establishes an authenticated session for a user mapped from an OAuth2 token. */
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSession {

    private final ReactiveUserDetailsService userDetailsService;
    private final ServerSecurityContextRepository securityContextRepository;
    private final LoginHandlerEnhancer loginHandlerEnhancer;

    /**
     * Loads the user by username, remaps the OAuth2 token to a {@link HaloOAuth2AuthenticationToken}, saves the
     * security context, and triggers the login success handlers.
     *
     * @param exchange the current exchange
     * @param username the username to authenticate as
     * @param token the OAuth2 token to remap from
     * @return an empty mono on success
     */
    public Mono<Void> establish(ServerWebExchange exchange, String username, OAuth2AuthenticationToken token) {
        return userDetailsService
                .findByUsername(username)
                .map(userDetails -> HaloOAuth2AuthenticationToken.authenticated(userDetails, token))
                .flatMap(haloToken -> {
                    var securityContext = new SecurityContextImpl(haloToken);
                    return securityContextRepository
                            .save(exchange, securityContext)
                            .then(loginHandlerEnhancer.onLoginSuccess(exchange, haloToken));
                });
    }
}
