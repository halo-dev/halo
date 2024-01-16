package run.halo.app.security.authentication.twofactor;

import java.net.URI;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import reactor.core.publisher.Mono;

public class TwoFactorAuthorizationManager
    implements ReactiveAuthorizationManager<AuthorizationContext> {

    private final ReactiveAuthorizationManager<AuthorizationContext> delegate;

    private static final URI REDIRECT_LOCATION = URI.create("/console/login?2fa=totp");

    public TwoFactorAuthorizationManager(
        ReactiveAuthorizationManager<AuthorizationContext> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> authentication,
        AuthorizationContext context) {
        return authentication.flatMap(a -> {
            Mono<AuthorizationDecision> checked = delegate.check(Mono.just(a), context);
            if (a instanceof TwoFactorAuthentication) {
                checked = checked.filter(AuthorizationDecision::isGranted)
                    .switchIfEmpty(
                        Mono.error(() -> new TwoFactorAuthRequiredException(REDIRECT_LOCATION)));
            }
            return checked;
        });
    }

}
