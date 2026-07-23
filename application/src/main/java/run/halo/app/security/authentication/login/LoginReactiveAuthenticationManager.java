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
        var loginId = authentication.getName();
        var credentials = authentication.getCredentials();
        var password = credentials != null ? credentials.toString() : null;

        return Flux.concat(tryByUsername(loginId, password), tryByEmail(loginId, password))
                .next()
                .switchIfEmpty(Mono.error(() -> new BadCredentialsException("Invalid Credentials")))
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(this::preAuthenticationChecks)
                .flatMap(userDetails -> upgradePasswordIfNeeded(userDetails, password))
                .doOnNext(this::postAuthenticationChecks)
                .map(userDetails -> UsernamePasswordAuthenticationToken.authenticated(
                        userDetails, userDetails.getPassword(), userDetails.getAuthorities()));
    }

    /**
     * Default pre-authentication checks: account locked, disabled, and expired. Mirrors
     * {@code AbstractUserDetailsReactiveAuthenticationManager.defaultPreAuthenticationChecks}.
     */
    private void preAuthenticationChecks(UserDetails user) {
        if (!user.isAccountNonLocked()) {
            throw new LockedException("User account is locked");
        }
        if (!user.isEnabled()) {
            throw new DisabledException("User is disabled");
        }
        if (!user.isAccountNonExpired()) {
            throw new AccountExpiredException("User account has expired");
        }
    }

    /**
     * Default post-authentication checks: credentials expired. Mirrors
     * {@code AbstractUserDetailsReactiveAuthenticationManager.defaultPostAuthenticationChecks}.
     */
    private void postAuthenticationChecks(UserDetails user) {
        if (!user.isCredentialsNonExpired()) {
            throw new CredentialsExpiredException("User credentials have expired");
        }
    }

    /**
     * Attempts to authenticate by username. Since usernames do not contain {@code @}, this method immediately returns
     * empty for email-like login identifiers.
     */
    Mono<UserDetails> tryByUsername(String loginId, String password) {
        if (loginId.contains("@")) {
            return Mono.empty();
        }
        return userDetailsService
                .findByUsername(loginId)
                .publishOn(Schedulers.boundedElastic())
                .onErrorResume(BadCredentialsException.class, e -> Mono.empty())
                .filter(userDetails ->
                        password != null && passwordEncoder.matches(password, userDetails.getPassword()));
    }

    /**
     * Attempts to authenticate by verified email. Only executes when the login identifier looks like an email address.
     */
    Mono<UserDetails> tryByEmail(String loginId, String password) {
        if (!loginId.contains("@")) {
            return Mono.empty();
        }
        return userService
                .findUserByVerifiedEmail(loginId)
                .flatMap(user -> tryByUsername(user.getMetadata().getName(), password));
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
