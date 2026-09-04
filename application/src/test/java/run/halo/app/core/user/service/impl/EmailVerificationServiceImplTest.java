package run.halo.app.core.user.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static run.halo.app.core.user.service.impl.EmailVerificationServiceImpl.MAX_ATTEMPTS;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.notification.Subscription;
import run.halo.app.core.user.service.impl.EmailVerificationServiceImpl.EmailVerificationManager;
import run.halo.app.core.user.service.impl.EmailVerificationServiceImpl.EmailVerificationManager.UsernameEmail;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.exception.EmailVerificationFailed;
import run.halo.app.notification.NotificationCenter;
import run.halo.app.notification.NotificationReasonEmitter;

/**
 * Tests for {@link EmailVerificationServiceImpl}.
 *
 * @author guqing
 * @since 2.11.0
 */
@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceImplTest {

    @Nested
    class EmailVerificationManagerTest {

        @Test
        public void generateCodeTest() {
            var emailVerificationManager = new EmailVerificationServiceImpl.EmailVerificationManager();
            emailVerificationManager.generateCode("fake-user", "fake-email");
            var result = emailVerificationManager.contains("fake-user", "fake-email");
            assertThat(result).isTrue();

            emailVerificationManager.generateCode("guqing", "hi@halo.run");
            result = emailVerificationManager.contains("guqing", "hi@halo.run");
            assertThat(result).isTrue();

            result = emailVerificationManager.contains("123", "123");
            assertThat(result).isFalse();
        }

        @Test
        public void removeTest() {
            var emailVerificationManager = new EmailVerificationServiceImpl.EmailVerificationManager();
            emailVerificationManager.generateCode("fake-user", "fake-email");
            var result = emailVerificationManager.contains("fake-user", "fake-email");
            emailVerificationManager.removeCode("fake-user", "fake-email");
            result = emailVerificationManager.contains("fake-user", "fake-email");
            assertThat(result).isFalse();
        }

        @Test
        void verifyCodeTestNormal() {
            String username = "guqing";
            String email = "hi@halo.run";
            var emailVerificationManager = new EmailVerificationServiceImpl.EmailVerificationManager();
            var result = emailVerificationManager.verifyCode(username, email, "fake-code");
            assertThat(result).isFalse();

            var code = emailVerificationManager.generateCode(username, email);
            result = emailVerificationManager.verifyCode(username, email, "fake-code");
            assertThat(result).isFalse();

            result = emailVerificationManager.verifyCode(username, email, code);
            assertThat(result).isTrue();
        }

        @Test
        void verifyCodeFailedAfterMaxAttempts() {
            String username = "guqing";
            String email = "example@example.com";
            var emailVerificationManager = new EmailVerificationServiceImpl.EmailVerificationManager();
            var code = emailVerificationManager.generateCode(username, email);
            for (int i = 0; i <= MAX_ATTEMPTS; i++) {
                var result = emailVerificationManager.verifyCode(username, email, "fake-code");
                assertThat(result).isFalse();
            }

            assertThatThrownBy(() -> emailVerificationManager.verifyCode(username, email, code))
                    .isInstanceOf(EmailVerificationFailed.class)
                    .hasMessage("400 BAD_REQUEST \"Too many attempts. Please try again later.\"");
        }
    }

    @Test
    void shouldBeEqualUsernameEmailWithDifferentCase() {
        var expected = new UsernameEmail("faker", "a@b.com");
        var got = new UsernameEmail("faker", "A@B.com");
        assertEquals(expected, got);
    }

    @Nested
    class SecurityVerificationCodeTest {

        @Mock
        ReactiveExtensionClient client;

        @Mock
        NotificationReasonEmitter reasonEmitter;

        @Mock
        NotificationCenter notificationCenter;

        EmailVerificationServiceImpl service;

        @BeforeEach
        void setUp() {
            service = new EmailVerificationServiceImpl(client, reasonEmitter, notificationCenter);
        }

        User user(boolean emailVerified, String email) {
            var spec = new User.UserSpec();
            spec.setEmail(email);
            spec.setEmailVerified(emailVerified);
            var user = new User();
            user.setSpec(spec);
            var metadata = new Metadata();
            metadata.setName("faker");
            user.setMetadata(metadata);
            return user;
        }

        EmailVerificationManager manager() {
            return (EmailVerificationManager) ReflectionTestUtils.getField(service, "emailVerificationManager");
        }

        @Test
        void shouldRejectWhenEmailNotVerified() {
            when(client.get(User.class, "faker")).thenReturn(Mono.just(user(false, "faker@halo.run")));
            StepVerifier.create(service.sendSecurityVerificationCode("faker"))
                    .expectError(ServerWebInputException.class)
                    .verify();
        }

        @Test
        void shouldSendCodeWhenEmailVerified() {
            when(client.get(User.class, "faker")).thenReturn(Mono.just(user(true, "faker@halo.run")));
            when(reasonEmitter.emit(eq(EmailVerificationServiceImpl.SECURITY_VERIFICATION_REASON_TYPE), any()))
                    .thenReturn(Mono.empty());
            when(notificationCenter.subscribe(any(), any())).thenReturn(Mono.just(new Subscription()));
            StepVerifier.create(service.sendSecurityVerificationCode("faker")).verifyComplete();
            assertThat(manager().contains("faker", "faker@halo.run")).isTrue();
        }

        @Test
        void shouldVerifyCodeAndRemoveIt() {
            when(client.get(User.class, "faker")).thenReturn(Mono.just(user(true, "faker@halo.run")));
            var code = manager().generateCode("faker", "faker@halo.run");
            StepVerifier.create(service.verifySecurityVerificationCode("faker", code))
                    .verifyComplete();
            assertThat(manager().contains("faker", "faker@halo.run")).isFalse();
        }

        @Test
        void shouldRejectInvalidCode() {
            when(client.get(User.class, "faker")).thenReturn(Mono.just(user(true, "faker@halo.run")));
            manager().generateCode("faker", "faker@halo.run");
            StepVerifier.create(service.verifySecurityVerificationCode("faker", "000000"))
                    .expectError(EmailVerificationFailed.class)
                    .verify();
        }
    }
}
