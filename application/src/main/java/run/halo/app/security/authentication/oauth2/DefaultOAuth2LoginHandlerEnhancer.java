package run.halo.app.security.authentication.oauth2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.core.user.service.UserConnectionService;

/**
 * Default implementation of {@link OAuth2LoginHandlerEnhancer}.
 *
 * @author johnniang
 * @since 2.20.0
 */
@Slf4j
@Component
public class DefaultOAuth2LoginHandlerEnhancer implements OAuth2LoginHandlerEnhancer {

    private final UserConnectionService connectionService;

    private final OAuth2AuthenticationTokenCache oauth2TokenCache;

    private final OAuth2BindIntent bindIntent;

    private final AuthenticationTrustResolver authenticationTrustResolver = new AuthenticationTrustResolverImpl();

    public DefaultOAuth2LoginHandlerEnhancer(
            UserConnectionService connectionService,
            OAuth2AuthenticationTokenCache oauth2TokenCache,
            OAuth2BindIntent bindIntent) {
        this.connectionService = connectionService;
        this.oauth2TokenCache = oauth2TokenCache;
        this.bindIntent = bindIntent;
    }

    @Override
    public Mono<Void> loginSuccess(ServerWebExchange exchange, Authentication authentication) {
        if (!authenticationTrustResolver.isFullyAuthenticated(authentication)) {
            // Should never happen
            // Remove token directly if not fully authenticated
            return Mono.when(oauth2TokenCache.removeToken(exchange), bindIntent.clear(exchange));
        }
        return bindIntent
                .consume(exchange)
                .flatMap(oauth2Identity ->
                        bind(exchange, authentication, oauth2Identity).thenReturn(true))
                .defaultIfEmpty(false)
                .flatMap(bindAttempted -> bindAttempted ? Mono.empty() : oauth2TokenCache.removeToken(exchange));
    }

    private Mono<Void> bind(ServerWebExchange exchange, Authentication authentication, String expectedOAuth2Identity) {
        var binding = oauth2TokenCache.getToken(exchange).flatMap(oauth2Token -> {
            if (!OAuth2IdentityFingerprint.from(oauth2Token).equals(expectedOAuth2Identity)) {
                log.warn("Refused to bind an OAuth2 identity that changed during local login");
                return Mono.empty();
            }
            var oauth2User = oauth2Token.getPrincipal();
            var username = authentication.getName();
            var registrationId = oauth2Token.getAuthorizedClientRegistrationId();
            return connectionService
                    .updateUserConnectionIfPresent(registrationId, oauth2User)
                    .doOnNext(connection -> {
                        if (log.isDebugEnabled()) {
                            log.debug("User connection already exists, skip creating. connection: [{}]", connection);
                        }
                    })
                    .switchIfEmpty(Mono.defer(
                            () -> connectionService.createUserConnection(username, registrationId, oauth2User)))
                    .then();
        });
        return binding.then(Mono.defer(() -> oauth2TokenCache.removeToken(exchange)))
                .onErrorResume(error ->
                        Mono.defer(() -> oauth2TokenCache.removeToken(exchange)).then(Mono.error(error)));
    }
}
