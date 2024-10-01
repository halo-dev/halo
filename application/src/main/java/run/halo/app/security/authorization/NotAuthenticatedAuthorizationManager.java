package run.halo.app.security.authorization;

import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import reactor.core.publisher.Mono;

/**
 * Authorization manager that checks if the user is not authenticated.
 *
 * @author johnniang
 * @since 2.20.0
 */
public class NotAuthenticatedAuthorizationManager
    implements ReactiveAuthorizationManager<AuthorizationContext> {

    private final AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();

    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> authentication,
        AuthorizationContext object) {
        return authentication.map(a -> !trustResolver.isAuthenticated(a))
            .defaultIfEmpty(true)
            .map(AuthorizationDecision::new);
    }

}
