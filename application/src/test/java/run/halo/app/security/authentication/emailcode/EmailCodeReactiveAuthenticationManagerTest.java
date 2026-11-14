package run.halo.app.security.authentication.emailcode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.User.UserSpec;
import run.halo.app.extension.Metadata;

/**
 * Tests for {@link EmailCodeReactiveAuthenticationManager}.
 *
 * @author johnniang
 * @since 2.26.0
 */
@ExtendWith(MockitoExtension.class)
class EmailCodeReactiveAuthenticationManagerTest {

    @Mock
    EmailCodeService emailCodeService;

    @Mock
    ReactiveUserDetailsService userDetailsService;

    @InjectMocks
    EmailCodeReactiveAuthenticationManager manager;

    // ── Happy path ──────────────────────────────────────────────────

    @Test
    void shouldAuthenticateWithValidCode() {
        var user = createUserExtension("johnniang");
        when(emailCodeService.verifyLoginCode("test@example.com", "123456")).thenReturn(Mono.just(user));

        var userDetails = createUserDetails("johnniang");
        when(userDetailsService.findByUsername("johnniang")).thenReturn(Mono.just(userDetails));

        var result = authenticate("test@example.com", "123456");

        StepVerifier.create(result)
                .assertNext(auth -> {
                    assertThat(auth).isInstanceOf(UsernamePasswordAuthenticationToken.class);
                    assertThat(auth.isAuthenticated()).isTrue();
                    assertThat(auth.getPrincipal()).isSameAs(userDetails);
                    assertThat(auth.getName()).isEqualTo("johnniang");
                })
                .verifyComplete();
    }

    // ── Invalid code ────────────────────────────────────────────────

    @Test
    void shouldFailWhenCodeIsInvalid() {
        when(emailCodeService.verifyLoginCode("test@example.com", "wrong-code"))
                .thenReturn(Mono.error(new BadCredentialsException("Invalid or expired verification code")));

        var result = authenticate("test@example.com", "wrong-code");

        StepVerifier.create(result).expectError(BadCredentialsException.class).verify();
        verify(userDetailsService, never()).findByUsername(anyString());
    }

    // ── User not found ──────────────────────────────────────────────

    @Test
    void shouldFailWhenUserDetailsNotFound() {
        var user = createUserExtension("johnniang");
        when(emailCodeService.verifyLoginCode("test@example.com", "123456")).thenReturn(Mono.just(user));
        when(userDetailsService.findByUsername("johnniang"))
                .thenReturn(Mono.error(new BadCredentialsException("Invalid Credentials")));

        var result = authenticate("test@example.com", "123456");

        StepVerifier.create(result).expectError(BadCredentialsException.class).verify();
    }

    // ── Account status ──────────────────────────────────────────────

    @Test
    void shouldFailWhenAccountLocked() {
        var user = createUserExtension("johnniang");
        when(emailCodeService.verifyLoginCode("test@example.com", "123456")).thenReturn(Mono.just(user));

        var userDetails = User.withUsername("johnniang")
                .password("encoded")
                .authorities("ROLE_test")
                .accountLocked(true)
                .build();
        when(userDetailsService.findByUsername("johnniang")).thenReturn(Mono.just(userDetails));

        var result = authenticate("test@example.com", "123456");

        StepVerifier.create(result).expectError(LockedException.class).verify();
    }

    @Test
    void shouldFailWhenAccountDisabled() {
        var user = createUserExtension("johnniang");
        when(emailCodeService.verifyLoginCode("test@example.com", "123456")).thenReturn(Mono.just(user));

        var userDetails = User.withUsername("johnniang")
                .password("encoded")
                .authorities("ROLE_test")
                .disabled(true)
                .build();
        when(userDetailsService.findByUsername("johnniang")).thenReturn(Mono.just(userDetails));

        var result = authenticate("test@example.com", "123456");

        StepVerifier.create(result).expectError(DisabledException.class).verify();
    }

    @Test
    void shouldFailWhenAccountExpired() {
        var user = createUserExtension("johnniang");
        when(emailCodeService.verifyLoginCode("test@example.com", "123456")).thenReturn(Mono.just(user));

        var userDetails = User.withUsername("johnniang")
                .password("encoded")
                .authorities("ROLE_test")
                .accountExpired(true)
                .build();
        when(userDetailsService.findByUsername("johnniang")).thenReturn(Mono.just(userDetails));

        var result = authenticate("test@example.com", "123456");

        StepVerifier.create(result).expectError(AccountExpiredException.class).verify();
    }

    // ── Helpers ────────────────────────────────────────────────────

    private Mono<Authentication> authenticate(String email, String code) {
        var token = new EmailCodeAuthenticationToken(email, code);
        return manager.authenticate(token);
    }

    private UserDetails createUserDetails(String username) {
        return User.withUsername(username)
                .password("encoded-password")
                .authorities("ROLE_test")
                .build();
    }

    private run.halo.app.core.extension.User createUserExtension(String name) {
        var metadata = new Metadata();
        metadata.setName(name);
        var spec = new UserSpec();
        spec.setEmailVerified(true);
        spec.setEmail(name + "@example.com");
        var user = new run.halo.app.core.extension.User();
        user.setMetadata(metadata);
        user.setSpec(spec);
        return user;
    }
}
