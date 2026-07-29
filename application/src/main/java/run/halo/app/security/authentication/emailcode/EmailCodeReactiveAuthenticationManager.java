package run.halo.app.security.authentication.emailcode;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import reactor.core.publisher.Mono;
import run.halo.app.security.authentication.UserAccountStatusChecker;

/**
 * Authenticates by validating the email verification code and looking up the user by verified email.
 *
 * <p>On success returns a standard {@link UsernamePasswordAuthenticationToken} so that downstream handlers (success,
 * remember-me, device tracking) work identically to password login.
 *
 * @author johnniang
 * @since 2.26.0
 */
@RequiredArgsConstructor
public class EmailCodeReactiveAuthenticationManager implements ReactiveAuthenticationManager {

    private final EmailCodeService emailCodeService;

    private final ReactiveUserDetailsService userDetailsService;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        if (!(authentication instanceof EmailCodeAuthenticationToken token)) {
            return Mono.error(() ->
                    new IllegalArgumentException("Unsupported authentication type: " + authentication.getClass()));
        }

        var email = token.getPrincipal().toString();
        var code = token.getCredentials().toString();

        return emailCodeService
                .verifyLoginCode(email, code)
                .flatMap(user -> userDetailsService
                        .findByUsername(user.getMetadata().getName())
                        .onErrorMap(
                                BadCredentialsException.class,
                                e -> new BadCredentialsException("Invalid email or code")))
                .delayUntil(UserAccountStatusChecker::check)
                .map(userDetails -> UsernamePasswordAuthenticationToken.authenticated(
                        userDetails, null, userDetails.getAuthorities()));
    }
}
