package run.halo.app.security.authentication.twofactor;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import reactor.core.publisher.Mono;

public class TwoFactorAuthorizationManager
    implements ReactiveAuthorizationManager<AuthorizationContext> {

    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> authentication,
        AuthorizationContext context) {
        return authentication.map(TwoFactorAuthentication.class::isInstance)
            .defaultIfEmpty(false)
            .map(AuthorizationDecision::new);
    }

}
