package run.halo.app.security.authentication.oauth2;

import lombok.Setter;
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

    @Setter
    private OAuth2AuthenticationTokenCache oauth2TokenCache =
        new WebSessionOAuth2AuthenticationTokenCache();

    private final AuthenticationTrustResolver authenticationTrustResolver =
        new AuthenticationTrustResolverImpl();

    public DefaultOAuth2LoginHandlerEnhancer(UserConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    @Override
    public Mono<Void> loginSuccess(ServerWebExchange exchange, Authentication authentication) {
        if (!authenticationTrustResolver.isFullyAuthenticated(authentication)) {
            // Should never happen
            // Remove token directly if not fully authenticated
            return oauth2TokenCache.removeToken(exchange).then();
        }
        return oauth2TokenCache.getToken(exchange)
            .flatMap(oauth2Token -> {
                var oauth2User = oauth2Token.getPrincipal();
                var username = authentication.getName();
                var registrationId = oauth2Token.getAuthorizedClientRegistrationId();
                return connectionService.updateUserConnectionIfPresent(registrationId, oauth2User)
                    .doOnNext(connection -> {
                        if (log.isDebugEnabled()) {
                            log.debug(
                                "User connection already exists, skip creating. connection: [{}]",
                                connection
                            );
                        }
                    })
                    .switchIfEmpty(Mono.defer(() -> connectionService.createUserConnection(
                        username,
                        registrationId,
                        oauth2User
                    )))
                    .then(oauth2TokenCache.removeToken(exchange));
            });
    }

}
