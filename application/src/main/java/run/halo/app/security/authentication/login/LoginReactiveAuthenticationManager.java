package run.halo.app.security.authentication.login;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.ReactiveUserDetailsPasswordService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import run.halo.app.core.user.service.UserService;

/**
 * A {@link ReactiveAuthenticationManager} that authenticates by trying username first, then falling back to verified
 * email lookup if the username is not found or the password does not match.
 *
 * <p>This prevents ambiguous credential matching when a login identifier could be either a username or an email
 * address.
 */
@RequiredArgsConstructor
class LoginReactiveAuthenticationManager implements ReactiveAuthenticationManager {

    private final ReactiveUserDetailsService userDetailsService;

    private final UserService userService;

    private final PasswordEncoder passwordEncoder;

    private final ReactiveUserDetailsPasswordService passwordService;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        var username = authentication.getName();
        var password = authentication.getCredentials().toString();

        return tryByUsername(username, password)
                .switchIfEmpty(Mono.defer(() -> tryByEmail(username, password)))
                .switchIfEmpty(Mono.error(() -> new BadCredentialsException("Invalid Credentials")))
                .flatMap(userDetails -> upgradePasswordIfNeeded(userDetails, password))
                .map(userDetails -> {
                    var result = new UsernamePasswordAuthenticationToken(
                            userDetails, authentication.getCredentials(), userDetails.getAuthorities());
                    result.setDetails(authentication.getDetails());
                    return result;
                });
    }

    /**
     * Attempts to authenticate by username: loads the user and checks the password. Returns empty Mono if the user is
     * not found or the password does not match.
     */
    Mono<UserDetails> tryByUsername(String username, String password) {
        return userDetailsService
                .findByUsername(username)
                .onErrorResume(BadCredentialsException.class, e -> Mono.empty())
                .filter(userDetails -> passwordEncoder.matches(password, userDetails.getPassword()));
    }

    /**
     * Attempts to authenticate by email: looks up the verified email to find the actual username, then delegates to
     * {@link #tryByUsername(String, String)}.
     */
    Mono<UserDetails> tryByEmail(String email, String password) {
        return userService
                .findUserByVerifiedEmail(email)
                .flatMap(user -> tryByUsername(user.getMetadata().getName(), password));
    }

    private Mono<UserDetails> upgradePasswordIfNeeded(UserDetails userDetails, String password) {
        if (passwordService != null) {
            return passwordService.updatePassword(userDetails, password);
        }
        return Mono.just(userDetails);
    }
}
