package run.halo.app.security.authentication.login;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.ReactiveUserDetailsPasswordService;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.Metadata;

@ExtendWith(MockitoExtension.class)
class LoginReactiveAuthenticationManagerTest {

    @Mock
    ReactiveUserDetailsService userDetailsService;

    @Mock
    UserService userService;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    ReactiveUserDetailsPasswordService passwordService;

    // ── Plain username (no @) ──────────────────────────────────────

    @Test
    void shouldAuthenticateByUsernameWithCorrectPassword() {
        var userDetails = createUserDetails("testuser", "encoded-password");
        when(userDetailsService.findByUsername("testuser")).thenReturn(Mono.just(userDetails));
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);
        stubPasswordService();

        var result = authenticate("testuser", "password");

        StepVerifier.create(result)
                .expectSubscription()
                .assertNext(auth -> {
                    assertEquals(userDetails, auth.getPrincipal());
                    assertEquals("encoded-password", auth.getCredentials());
                })
                .verifyComplete();

        verify(userService, never()).findUserByVerifiedEmail(anyString());
    }

    @Test
    void shouldFailWhenUsernameNotFoundAndInputIsPlain() {
        when(userDetailsService.findByUsername("alice"))
                .thenReturn(Mono.error(new BadCredentialsException("Invalid Credentials")));

        var result = authenticate("alice", "password");

        StepVerifier.create(result).expectError(BadCredentialsException.class).verify();

        // plain input does not trigger email lookup
        verify(userService, never()).findUserByVerifiedEmail(anyString());
    }

    @Test
    void shouldFailWhenUsernameFoundButPasswordWrongAndInputIsPlain() {
        var userDetails = createUserDetails("testuser", "encoded-password");
        when(userDetailsService.findByUsername("testuser")).thenReturn(Mono.just(userDetails));
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(false);

        var result = authenticate("testuser", "password");

        StepVerifier.create(result).expectError(BadCredentialsException.class).verify();

        // plain input does not trigger email lookup
        verify(userService, never()).findUserByVerifiedEmail(anyString());
    }

    // ── Email input (contains @) ───────────────────────────────────

    @Test
    void shouldAuthenticateByEmailWithCorrectPassword() {
        // tryByUsername skips @ input, goes directly to tryByEmail
        var emailUser = createUserExtension("actualuser");
        when(userService.findUserByVerifiedEmail("test@example.com")).thenReturn(Mono.just(emailUser));

        var userDetails = createUserDetails("actualuser", "encoded-password");
        when(userDetailsService.findByUsername("actualuser")).thenReturn(Mono.just(userDetails));
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);
        stubPasswordService();

        var result = authenticate("test@example.com", "password");

        StepVerifier.create(result)
                .assertNext(auth -> assertEquals(userDetails, auth.getPrincipal()))
                .verifyComplete();

        // @ input bypasses username lookup entirely
        verify(userDetailsService, never()).findByUsername("test@example.com");
    }

    @Test
    void shouldFailWhenEmailNotFound() {
        when(userService.findUserByVerifiedEmail("test@example.com")).thenReturn(Mono.empty());

        var result = authenticate("test@example.com", "password");

        StepVerifier.create(result).expectError(BadCredentialsException.class).verify();
    }

    @Test
    void shouldFailWhenEmailFoundButPasswordWrong() {
        var emailUser = createUserExtension("actualuser");
        when(userService.findUserByVerifiedEmail("test@example.com")).thenReturn(Mono.just(emailUser));

        var userDetails = createUserDetails("actualuser", "encoded-password");
        when(userDetailsService.findByUsername("actualuser")).thenReturn(Mono.just(userDetails));
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(false);

        var result = authenticate("test@example.com", "password");

        StepVerifier.create(result).expectError(BadCredentialsException.class).verify();
    }

    // ── Password upgrade ───────────────────────────────────────────

    @Test
    void shouldUpgradePasswordWhenNeeded() {
        var userDetails = createUserDetails("testuser", "encoded-password");
        when(userDetailsService.findByUsername("testuser")).thenReturn(Mono.just(userDetails));
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);

        when(passwordEncoder.upgradeEncoding("encoded-password")).thenReturn(true);
        when(passwordEncoder.encode("password")).thenReturn("new-encoded-password");

        var upgradedUser = createUserDetails("testuser", "new-encoded-password");
        when(passwordService.updatePassword(eq(userDetails), eq("new-encoded-password")))
                .thenReturn(Mono.just(upgradedUser));

        var result = authenticate("testuser", "password");

        StepVerifier.create(result)
                .expectSubscription()
                .assertNext(auth -> {
                    assertEquals(upgradedUser, auth.getPrincipal());
                    assertEquals("new-encoded-password", auth.getCredentials());
                })
                .verifyComplete();

        verify(passwordService).updatePassword(userDetails, "new-encoded-password");
    }

    @Test
    void shouldUpgradePasswordWhenAuthenticatedByEmail() {
        var emailUser = createUserExtension("actualuser");
        when(userService.findUserByVerifiedEmail("test@example.com")).thenReturn(Mono.just(emailUser));

        var userDetails = createUserDetails("actualuser", "encoded-password");
        when(userDetailsService.findByUsername("actualuser")).thenReturn(Mono.just(userDetails));
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);

        when(passwordEncoder.upgradeEncoding("encoded-password")).thenReturn(true);
        when(passwordEncoder.encode("password")).thenReturn("new-encoded-password");

        var upgradedUser = createUserDetails("actualuser", "new-encoded-password");
        when(passwordService.updatePassword(eq(userDetails), eq("new-encoded-password")))
                .thenReturn(Mono.just(upgradedUser));

        var result = authenticate("test@example.com", "password");

        StepVerifier.create(result)
                .assertNext(auth -> {
                    assertEquals(upgradedUser, auth.getPrincipal());
                    assertEquals("new-encoded-password", auth.getCredentials());
                })
                .verifyComplete();

        verify(passwordService).updatePassword(userDetails, "new-encoded-password");
    }

    // ── Exception handling ─────────────────────────────────────────

    @Test
    void shouldPropagateNonAuthenticationException() {
        when(userDetailsService.findByUsername("testuser"))
                .thenReturn(Mono.error(new RuntimeException("Database error")));

        var result = authenticate("testuser", "password");

        StepVerifier.create(result).expectError(RuntimeException.class).verify();

        verify(userService, never()).findUserByVerifiedEmail(anyString());
    }

    @Test
    void shouldFailWhenAllStrategiesExhaustedForEmail() {
        when(userService.findUserByVerifiedEmail("unknown@example.com")).thenReturn(Mono.empty());

        var result = authenticate("unknown@example.com", "password");

        StepVerifier.create(result).expectError(BadCredentialsException.class).verify();
    }

    // ── Helpers ────────────────────────────────────────────────────

    private Mono<Authentication> authenticate(String username, String password) {
        var manager = new LoginReactiveAuthenticationManager(
                userDetailsService, userService, passwordEncoder, passwordService);
        var token = UsernamePasswordAuthenticationToken.unauthenticated(username, password);
        return manager.authenticate(token);
    }

    private void stubPasswordService() {
        lenient()
                .when(passwordService.updatePassword(any(), anyString()))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
    }

    private UserDetails createUserDetails(String username, String password) {
        return User.withUsername(username)
                .password(password)
                .authorities("ROLE_test")
                .build();
    }

    private run.halo.app.core.extension.User createUserExtension(String name) {
        var metadata = new Metadata();
        metadata.setName(name);
        var user = new run.halo.app.core.extension.User();
        user.setMetadata(metadata);
        return user;
    }
}
