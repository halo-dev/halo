package run.halo.app.security.authentication.login;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
    void shouldFallbackToEmailWhenPasswordWrong() {
        var userByUsername = createUserDetails("testuser", "encoded-wrong");
        when(userDetailsService.findByUsername("test@example.com")).thenReturn(Mono.just(userByUsername));
        when(passwordEncoder.matches("password", "encoded-wrong")).thenReturn(false);

        var emailUser = createUserExtension("actualuser");
        when(userService.findUserByVerifiedEmail("test@example.com")).thenReturn(Mono.just(emailUser));

        var userByEmail = createUserDetails("actualuser", "encoded-correct");
        when(userDetailsService.findByUsername("actualuser")).thenReturn(Mono.just(userByEmail));
        when(passwordEncoder.matches("password", "encoded-correct")).thenReturn(true);
        stubPasswordService();

        var result = authenticate("test@example.com", "password");

        StepVerifier.create(result)
                .expectSubscription()
                .assertNext(auth -> {
                    assertEquals(userByEmail, auth.getPrincipal());
                })
                .verifyComplete();
    }

    @Test
    void shouldFallbackToEmailWhenUsernameNotFound() {
        when(userDetailsService.findByUsername("test@example.com"))
                .thenReturn(Mono.error(new BadCredentialsException("Invalid Credentials")));

        var emailUser = createUserExtension("actualuser");
        when(userService.findUserByVerifiedEmail("test@example.com")).thenReturn(Mono.just(emailUser));

        var userByEmail = createUserDetails("actualuser", "encoded-password");
        when(userDetailsService.findByUsername("actualuser")).thenReturn(Mono.just(userByEmail));
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);
        stubPasswordService();

        var result = authenticate("test@example.com", "password");

        StepVerifier.create(result)
                .expectSubscription()
                .assertNext(auth -> {
                    assertEquals(userByEmail, auth.getPrincipal());
                })
                .verifyComplete();
    }

    @Test
    void shouldFailWhenBothUsernameAndEmailPasswordWrong() {
        var userByUsername = createUserDetails("testuser", "encoded-wrong1");
        when(userDetailsService.findByUsername("test@example.com")).thenReturn(Mono.just(userByUsername));
        when(passwordEncoder.matches("password", "encoded-wrong1")).thenReturn(false);

        var emailUser = createUserExtension("actualuser");
        when(userService.findUserByVerifiedEmail("test@example.com")).thenReturn(Mono.just(emailUser));

        var userByEmail = createUserDetails("actualuser", "encoded-wrong2");
        when(userDetailsService.findByUsername("actualuser")).thenReturn(Mono.just(userByEmail));
        when(passwordEncoder.matches("password", "encoded-wrong2")).thenReturn(false);

        var result = authenticate("test@example.com", "password");

        StepVerifier.create(result).expectError(BadCredentialsException.class).verify();
    }

    @Test
    void shouldFailWhenUsernameNotFoundAndEmailNotFound() {
        when(userDetailsService.findByUsername("test@example.com"))
                .thenReturn(Mono.error(new BadCredentialsException("Invalid Credentials")));

        when(userService.findUserByVerifiedEmail("test@example.com")).thenReturn(Mono.empty());

        var result = authenticate("test@example.com", "password");

        StepVerifier.create(result).expectError(BadCredentialsException.class).verify();
    }

    @Test
    void shouldFailWhenUsernameNotFoundAndEmailFoundButPasswordWrong() {
        when(userDetailsService.findByUsername("test@example.com"))
                .thenReturn(Mono.error(new BadCredentialsException("Invalid Credentials")));

        var emailUser = createUserExtension("actualuser");
        when(userService.findUserByVerifiedEmail("test@example.com")).thenReturn(Mono.just(emailUser));

        var userByEmail = createUserDetails("actualuser", "encoded-wrong");
        when(userDetailsService.findByUsername("actualuser")).thenReturn(Mono.just(userByEmail));
        when(passwordEncoder.matches("password", "encoded-wrong")).thenReturn(false);

        var result = authenticate("test@example.com", "password");

        StepVerifier.create(result).expectError(BadCredentialsException.class).verify();
    }

    @Test
    void shouldUpgradePasswordWhenNeeded() {
        var userDetails = createUserDetails("testuser", "encoded-password");
        when(userDetailsService.findByUsername("testuser")).thenReturn(Mono.just(userDetails));
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);

        var upgradedUser = createUserDetails("testuser", "new-encoded-password");
        when(passwordService.updatePassword(eq(userDetails), eq("password"))).thenReturn(Mono.just(upgradedUser));

        var result = authenticate("testuser", "password");

        StepVerifier.create(result)
                .expectSubscription()
                .assertNext(auth -> {
                    assertEquals(upgradedUser, auth.getPrincipal());
                    assertEquals("new-encoded-password", auth.getCredentials());
                })
                .verifyComplete();

        verify(passwordService).updatePassword(userDetails, "password");
    }

    @Test
    void shouldAuthenticateByPureUsernameWithoutEmailLookup() {
        var userDetails = createUserDetails("alice", "encoded-password");
        when(userDetailsService.findByUsername("alice")).thenReturn(Mono.just(userDetails));
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);
        stubPasswordService();

        var result = authenticate("alice", "password");

        StepVerifier.create(result)
                .assertNext(auth -> assertEquals(userDetails, auth.getPrincipal()))
                .verifyComplete();

        verify(userService, never()).findUserByVerifiedEmail(anyString());
    }

    @Test
    void shouldFallbackToEmailForPlainUsernameWhenNotFound() {
        when(userDetailsService.findByUsername("alice"))
                .thenReturn(Mono.error(new BadCredentialsException("Invalid Credentials")));

        var emailUser = createUserExtension("alice_real");
        when(userService.findUserByVerifiedEmail("alice")).thenReturn(Mono.just(emailUser));

        var userByEmail = createUserDetails("alice_real", "encoded-password");
        when(userDetailsService.findByUsername("alice_real")).thenReturn(Mono.just(userByEmail));
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);
        stubPasswordService();

        var result = authenticate("alice", "password");

        StepVerifier.create(result)
                .assertNext(auth -> assertEquals(userByEmail, auth.getPrincipal()))
                .verifyComplete();
    }

    @Test
    void shouldNotAttemptEmailFallbackOnNonAuthenticationException() {
        when(userDetailsService.findByUsername("testuser"))
                .thenReturn(Mono.error(new RuntimeException("Database error")));

        var result = authenticate("testuser", "password");

        StepVerifier.create(result).expectError(RuntimeException.class).verify();

        verify(userService, never()).findUserByVerifiedEmail(anyString());
    }

    @Test
    void shouldCreateUsernamePasswordAuthenticationToken() {
        var userDetails = createUserDetails("testuser", "encoded-password");
        when(userDetailsService.findByUsername("testuser")).thenReturn(Mono.just(userDetails));
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(false);

        when(userService.findUserByVerifiedEmail("testuser")).thenReturn(Mono.empty());

        var result = authenticate("testuser", "password");

        StepVerifier.create(result).expectError(BadCredentialsException.class).verify();
    }

    private Mono<Authentication> authenticate(String username, String password) {
        var manager = new LoginReactiveAuthenticationManager(
                userDetailsService, userService, passwordEncoder, passwordService);
        var token = UsernamePasswordAuthenticationToken.unauthenticated(username, password);
        return manager.authenticate(token);
    }

    /**
     * Sets up a default stub for passwordService.updatePassword that returns the input user. Tests that need specific
     * upgrade behavior (like shouldUpgradePasswordWhenNeeded) should set up their own stubs instead of calling this
     * method.
     */
    private void stubPasswordService() {
        when(passwordService.updatePassword(any(), anyString()))
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
