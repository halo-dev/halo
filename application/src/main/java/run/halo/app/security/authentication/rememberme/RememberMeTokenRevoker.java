package run.halo.app.security.authentication.rememberme;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import run.halo.app.event.user.PasswordChangedEvent;

/**
 * Remember me token revoker.
 * <p>
 * Listen to password changed event and revoke remember me token.
 * </p>
 * Maybe you should consider revoke remember me token when user logout or username changed.
 *
 * @author guqing
 * @since 2.17.0
 */
@Component
@RequiredArgsConstructor
public class RememberMeTokenRevoker {
    private final PersistentRememberMeTokenRepository tokenRepository;

    @Async
    @EventListener(PasswordChangedEvent.class)
    public void onPasswordChanged(PasswordChangedEvent event) {
        tokenRepository.removeUserTokens(event.getUsername())
            .block();
    }
}
