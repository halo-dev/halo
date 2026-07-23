package run.halo.app.security.authentication.login;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
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

/**
 * A {@link ReactiveAuthenticationManager} that authenticates by trying multiple login strategies in order: username
 * first, then verified email. Each strategy only executes when the login identifier matches its expected format (e.g.,
 * email lookup only runs for identifiers containing {@code @}).
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

        return Flux.concat(lookupByUsername(loginId), lookupByEmail(loginId))
                .publishOn(Schedulers.boundedElastic())
                .filter(userDetails -> password != null && passwordEncoder.matches(password, userDetails.getPassword()))
                .next()
                .switchIfEmpty(Mono.error(() -> new BadCredentialsException("Invalid Credentials")))
                .delayUntil(this::checkAccountStatus)
                .flatMap(userDetails -> upgradePasswordIfNeeded(userDetails, password))
                .map(userDetails -> UsernamePasswordAuthenticationToken.authenticated(
                        userDetails, userDetails.getPassword(), userDetails.getAuthorities()));
    }

    private Mono<Void> checkAccountStatus(UserDetails user) {
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

    /** Looks up the user by username. Skips email-like identifiers since usernames never contain {@code @}. */
    Mono<UserDetails> lookupByUsername(String loginId) {
        if (loginId.contains("@")) {
            return Mono.empty();
        }
        return userDetailsService
                .findByUsername(loginId)
                .onErrorResume(BadCredentialsException.class, e -> Mono.empty());
    }

    /** Looks up the user by verified email. Skips identifiers that don't look like email addresses. */
    Mono<UserDetails> lookupByEmail(String loginId) {
        if (!loginId.contains("@")) {
            return Mono.empty();
        }
        return userService
                .findUserByVerifiedEmail(loginId)
                .flatMap(user -> userDetailsService
                        .findByUsername(user.getMetadata().getName())
                        .onErrorResume(BadCredentialsException.class, e -> Mono.empty()));
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
