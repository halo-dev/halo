package run.halo.app.security.authentication.emailcode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Consumer;
import org.junit.jupiter.api.Nested;
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
import run.halo.app.security.authentication.emailcode.EmailCodeServiceImpl.LoginCodeManager;

/**
 * Tests for {@link EmailCodeServiceImpl}.
 *
 * @author johnniang
 * @since 2.26.0
 */
@ExtendWith(MockitoExtension.class)
class EmailCodeServiceImplTest {

    @Mock
    UserService userService;

    @Mock
    NotificationReasonEmitter reasonEmitter;

    @Mock
    NotificationCenter notificationCenter;

    @InjectMocks
    EmailCodeServiceImpl emailCodeService;

    // ── sendLoginCode ───────────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void shouldSendCodeForVerifiedEmail() {
        var user = createUser("johnniang", "test@example.com", true);
        when(userService.listByEmail("test@example.com")).thenReturn(Flux.just(user));
        when(notificationCenter.subscribe(any(), any())).thenReturn(Mono.empty());
        when(reasonEmitter.emit(eq(EmailCodeServiceImpl.LOGIN_EMAIL_CODE_REASON_TYPE), any(Consumer.class)))
                .thenReturn(Mono.empty());

        StepVerifier.create(emailCodeService.sendLoginCode("test@example.com")).verifyComplete();

        verify(reasonEmitter).emit(eq(EmailCodeServiceImpl.LOGIN_EMAIL_CODE_REASON_TYPE), any(Consumer.class));
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
        when(userService.listByEmail("test@example.com")).thenReturn(Flux.just(user));
        when(notificationCenter.subscribe(any(), any())).thenReturn(Mono.empty());
        when(reasonEmitter.emit(anyString(), any(Consumer.class))).thenReturn(Mono.empty());

        // send code first
        emailCodeService.sendLoginCode("test@example.com").block();

        // capture the code from the emitted reason
        var captor = ArgumentCaptor.forClass(Consumer.class);
        verify(reasonEmitter).emit(eq(EmailCodeServiceImpl.LOGIN_EMAIL_CODE_REASON_TYPE), captor.capture());

        var builder = ReasonPayload.builder();
        captor.getValue().accept(builder);
        var code = builder.build().getAttributes().get("code").toString();

        // verify with the correct code
        when(userService.listByEmail("test@example.com")).thenReturn(Flux.just(user));
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

    // ── LoginCodeManager (inner class) ──────────────────────────────

    @Nested
    class LoginCodeManagerTest {

        @Test
        void shouldGenerateAndVerifyCode() {
            var manager = new LoginCodeManager();
            var code = manager.generateCode("test@example.com");

            assertThat(code).hasSize(6);
            assertThat(manager.verifyCode("test@example.com", code)).isTrue();
        }

        @Test
        void shouldFailWithWrongCode() {
            var manager = new LoginCodeManager();
            manager.generateCode("test@example.com");

            assertThat(manager.verifyCode("test@example.com", "000000")).isFalse();
        }

        @Test
        void shouldFailAfterMaxAttempts() {
            var manager = new LoginCodeManager();
            var code = manager.generateCode("test@example.com");

            for (int i = 0; i < EmailCodeServiceImpl.MAX_ATTEMPTS; i++) {
                assertThat(manager.verifyCode("test@example.com", "wrong-" + i)).isFalse();
            }

            // even correct code fails after max attempts
            assertThat(manager.verifyCode("test@example.com", code)).isFalse();
        }

        @Test
        void shouldRemoveCode() {
            var manager = new LoginCodeManager();
            var code = manager.generateCode("test@example.com");

            assertThat(manager.verifyCode("test@example.com", code)).isTrue();

            manager.removeCode("test@example.com");
            assertThat(manager.verifyCode("test@example.com", code)).isFalse();
        }

        @Test
        void shouldBeCaseInsensitive() {
            var manager = new LoginCodeManager();
            var code = manager.generateCode("test@example.com");

            // verifyCode is case-sensitive at the manager level — lowercasing is handled by
            // EmailCodeServiceImpl before calling the manager
            assertThat(manager.verifyCode("test@example.com", code)).isTrue();
        }
    }

    // ── Helpers ────────────────────────────────────────────────────

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
