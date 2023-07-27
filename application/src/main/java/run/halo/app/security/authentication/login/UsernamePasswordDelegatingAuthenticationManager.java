package run.halo.app.security.authentication.login;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;

@Slf4j
public class UsernamePasswordDelegatingAuthenticationManager
    implements ReactiveAuthenticationManager {

    private final ExtensionGetter extensionGetter;

    private final ReactiveAuthenticationManager defaultAuthenticationManager;

    public UsernamePasswordDelegatingAuthenticationManager(ExtensionGetter extensionGetter,
        ReactiveAuthenticationManager defaultAuthenticationManager) {
        this.extensionGetter = extensionGetter;
        this.defaultAuthenticationManager = defaultAuthenticationManager;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return extensionGetter
            .getEnabledExtensionByDefinition(UsernamePasswordAuthenticationManager.class)
            .next()
            .flatMap(authenticationManager -> authenticationManager.authenticate(authentication)
                .doOnError(t -> log.error(
                    "failed to authenticate with {}, fallback to default username password "
                        + "authentication.", authenticationManager.getClass(), t)
                )
                .onErrorResume(
                    t -> !(t instanceof AuthenticationException),
                    t -> Mono.empty()
                )
            )
            .switchIfEmpty(
                Mono.defer(() -> defaultAuthenticationManager.authenticate(authentication))
            );
    }
}
