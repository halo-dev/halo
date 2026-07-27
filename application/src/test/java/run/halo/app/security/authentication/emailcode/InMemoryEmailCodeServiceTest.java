package run.halo.app.security.authentication.emailcode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.User.UserSpec;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.Metadata;
import run.halo.app.notification.NotificationCenter;
import run.halo.app.notification.NotificationReasonEmitter;
import run.halo.app.notification.ReasonPayload;

/**
 * Tests for {@link InMemoryEmailCodeService}.
 *
 * @author johnniang
 * @since 2.26.0
 */
@ExtendWith(MockitoExtension.class)
class InMemoryEmailCodeServiceTest {

    @Mock
    UserService userService;

    @Mock
    NotificationReasonEmitter reasonEmitter;

    @Mock
    NotificationCenter notificationCenter;

    @InjectMocks
    InMemoryEmailCodeService emailCodeService;

    Clock baseClock;

    @BeforeEach
    void setUp() {
        baseClock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
        emailCodeService.setClock(baseClock);
    }

    // ── sendLoginCode ───────────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void shouldSendCodeForVerifiedEmail() {
        var user = createUser("johnniang", "test@example.com", true);
        when(userService.listByEmail("test@example.com")).thenReturn(Flux.just(user));
        when(notificationCenter.subscribe(any(), any())).thenReturn(Mono.empty());
        when(reasonEmitter.emit(eq(InMemoryEmailCodeService.LOGIN_EMAIL_CODE_REASON_TYPE), any(Consumer.class)))
                .thenReturn(Mono.empty());

        StepVerifier.create(emailCodeService.sendLoginCode("test@example.com")).verifyComplete();

        verify(reasonEmitter).emit(eq(InMemoryEmailCodeService.LOGIN_EMAIL_CODE_REASON_TYPE), any(Consumer.class));
    }

    @Test
    void shouldSilentlySkipWhenEmailNotVerified() {
        var user = createUser("johnniang", "test@example.com", false);
        when(userService.listByEmail("test@example.com")).thenReturn(Flux.just(user));

        StepVerifier.create(emailCodeService.sendLoginCode("test@example.com")).verifyComplete();

        verify(reasonEmitter, never()).emit(anyString(), any());
    }

    @Test
    void shouldSilentlySkipWhenEmailNotFound() {
        when(userService.listByEmail("test@example.com")).thenReturn(Flux.empty());

        StepVerifier.create(emailCodeService.sendLoginCode("test@example.com")).verifyComplete();

        verify(reasonEmitter, never()).emit(anyString(), any());
    }

    // ── verifyLoginCode ─────────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void shouldVerifyValidCodeAndReturnUser() {
        var user = createUser("johnniang", "test@example.com", true);
        when(notificationCenter.subscribe(any(), any())).thenReturn(Mono.empty());
        when(reasonEmitter.emit(anyString(), any(Consumer.class))).thenReturn(Mono.empty());
        when(userService.listByEmail("test@example.com")).thenReturn(Flux.just(user));

        // send code first
        emailCodeService.sendLoginCode("test@example.com").block();

        // capture the code from the emitted reason
        var captor = ArgumentCaptor.forClass(Consumer.class);
        verify(reasonEmitter).emit(eq(InMemoryEmailCodeService.LOGIN_EMAIL_CODE_REASON_TYPE), captor.capture());
        var builder = ReasonPayload.builder();
        captor.getValue().accept(builder);
        var code = builder.build().getAttributes().get("code").toString();

        // verify with the correct code
        StepVerifier.create(emailCodeService.verifyLoginCode("test@example.com", code))
                .assertNext(result -> assertThat(result.getMetadata().getName()).isEqualTo("johnniang"))
                .verifyComplete();
    }

    @Test
    void shouldFailWhenCodeIsInvalid() {
        StepVerifier.create(emailCodeService.verifyLoginCode("nonexistent@example.com", "000000"))
                .expectError(BadCredentialsException.class)
                .verify();
    }

    @Test
    void shouldFailWhenCodeIsWrong() {
        stubNotifications();
        // generate a code
        emailCodeService
                .sendLoginCodeNotification("johnniang", "test@example.com")
                .block();

        StepVerifier.create(emailCodeService.verifyLoginCode("test@example.com", "000000"))
                .expectError(BadCredentialsException.class)
                .verify();
    }

    @Test
    void shouldFailAfterMaxAttempts() {
        stubNotifications();
        // generate a code
        emailCodeService
                .sendLoginCodeNotification("johnniang", "test@example.com")
                .block();

        // exhaust all attempts
        for (int i = 0; i < InMemoryEmailCodeService.MAX_ATTEMPTS; i++) {
            StepVerifier.create(emailCodeService.verifyLoginCode("test@example.com", "wrong-" + i))
                    .expectError(BadCredentialsException.class)
                    .verify();
        }

        // now even the correct code should fail (blacklisted)
        StepVerifier.create(emailCodeService.verifyLoginCode("test@example.com", "any-code"))
                .expectError(BadCredentialsException.class)
                .verify();
    }

    @Test
    void shouldFailWhenCodeIsExpired() {
        stubNotifications();
        // generate a code at the base clock time
        emailCodeService
                .sendLoginCodeNotification("johnniang", "test@example.com")
                .block();

        // advance clock past CODE_TTL
        var expiredInstant =
                baseClock.instant().plus(InMemoryEmailCodeService.CODE_TTL).plusSeconds(1);
        emailCodeService.setClock(Clock.fixed(expiredInstant, ZoneOffset.UTC));

        StepVerifier.create(emailCodeService.verifyLoginCode("test@example.com", "any-code"))
                .expectError(BadCredentialsException.class)
                .verify();
    }

    // ── Helpers ────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private void stubNotifications() {
        when(notificationCenter.subscribe(any(), any())).thenReturn(Mono.empty());
        when(reasonEmitter.emit(anyString(), any(Consumer.class))).thenReturn(Mono.empty());
    }

    private User createUser(String name, String email, boolean emailVerified) {
        var metadata = new Metadata();
        metadata.setName(name);
        var spec = new UserSpec();
        spec.setEmail(email);
        spec.setEmailVerified(emailVerified);
        var user = new User();
        user.setMetadata(metadata);
        user.setSpec(spec);
        return user;
    }
}
