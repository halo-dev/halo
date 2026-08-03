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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.core.user.service.UserService;
import run.halo.app.security.authentication.UserAccountStatusChecker;

/**
 * A {@link ReactiveAuthenticationManager} that authenticates by trying multiple login strategies in order: verified
 * email first, then username. Each strategy only executes when the login identifier matches its expected format (e.g.,
 * email lookup only runs for identifiers containing {@code @}, while username lookup acts as a generic fallback for any
 * identifier). The password is checked after each lookup, and only a strategy that both resolves the identifier and
 * matches the password wins; otherwise the chain falls through to the next strategy.
 */
@RequiredArgsConstructor
class LoginReactiveAuthenticationManager implements ReactiveAuthenticationManager {

    private final ReactiveUserDetailsService userDetailsService;

    private final UserService userService;

    private final PasswordEncoder passwordEncoder;

    private final ReactiveUserDetailsPasswordService passwordService;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        var loginId = authentication.getName();
        var credentials = authentication.getCredentials();
        var password = credentials != null ? credentials.toString() : null;

        return Flux.concat(Mono.defer(() -> lookupByEmail(loginId)), Mono.defer(() -> lookupByUsername(loginId)))
                .publishOn(Schedulers.boundedElastic())
                .filter(userDetails -> password != null && passwordEncoder.matches(password, userDetails.getPassword()))
                .next()
                .switchIfEmpty(Mono.error(() -> new BadCredentialsException("Invalid Credentials")))
                .delayUntil(UserAccountStatusChecker::check)
                .flatMap(userDetails -> upgradePasswordIfNeeded(userDetails, password))
                .map(userDetails -> UsernamePasswordAuthenticationToken.authenticated(
                        userDetails, userDetails.getPassword(), userDetails.getAuthorities()));
    }

    /** Looks up the user by username. This is the generic fallback strategy for any identifier. */
    Mono<UserDetails> lookupByUsername(String loginId) {
        return userDetailsService.findByUsername(loginId);
    }

    /** Looks up the user by verified email. Skips identifiers that don't look like email addresses. */
    Mono<UserDetails> lookupByEmail(String loginId) {
        if (!loginId.contains("@")) {
            return Mono.empty();
        }
        return userService
                .findUserByVerifiedEmail(loginId)
                .flatMap(user ->
                        userDetailsService.findByUsername(user.getMetadata().getName()));
    }

    private Mono<UserDetails> upgradePasswordIfNeeded(UserDetails userDetails, String password) {
        if (passwordService != null) {
            var upgradeEncoding =
                    userDetails.getPassword() != null && passwordEncoder.upgradeEncoding(userDetails.getPassword());
            if (upgradeEncoding) {
                var newPassword = passwordEncoder.encode(password);
                return passwordService.updatePassword(userDetails, newPassword);
            }
        }
        return Mono.just(userDetails);
    }
}
