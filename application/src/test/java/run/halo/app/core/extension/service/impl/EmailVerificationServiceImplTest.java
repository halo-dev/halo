package run.halo.app.core.extension.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static run.halo.app.core.extension.service.impl.EmailVerificationServiceImpl.MAX_ATTEMPTS;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.infra.exception.EmailVerificationFailed;

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
            var emailVerificationManager =
                new EmailVerificationServiceImpl.EmailVerificationManager();
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
            var emailVerificationManager =
                new EmailVerificationServiceImpl.EmailVerificationManager();
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
            var emailVerificationManager =
                new EmailVerificationServiceImpl.EmailVerificationManager();
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
            var emailVerificationManager =
                new EmailVerificationServiceImpl.EmailVerificationManager();
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
}