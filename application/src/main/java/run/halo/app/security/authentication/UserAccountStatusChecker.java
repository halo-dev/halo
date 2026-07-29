package run.halo.app.security.authentication;

import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;

/**
 * Shared utility for checking user account status before completing authentication.
 *
 * @author johnniang
 * @since 2.26.0
 */
public final class UserAccountStatusChecker {

    private UserAccountStatusChecker() {}

    public static Mono<Void> check(UserDetails user) {
        if (!user.isAccountNonLocked()) {
            return Mono.error(new LockedException("User account is locked"));
        }
        if (!user.isEnabled()) {
            return Mono.error(new DisabledException("User is disabled"));
        }
        if (!user.isAccountNonExpired()) {
            return Mono.error(new AccountExpiredException("User account has expired"));
        }
        if (!user.isCredentialsNonExpired()) {
            return Mono.error(new CredentialsExpiredException("User credentials have expired"));
        }
        return Mono.empty();
    }
}
