package run.halo.app.core.extension.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.infra.exception.RateLimitExceededException;

/**
 * Tests for {@link EmailPasswordRecoveryServiceImpl}.
 *
 * @author guqing
 * @since 2.11.0
 */
@ExtendWith(MockitoExtension.class)
class EmailPasswordRecoveryServiceImplTest {

    @Nested
    class ResetPasswordVerificationManagerTest {
        @Test
        public void generateTokenTest() {
            var verificationManager =
                new EmailPasswordRecoveryServiceImpl.ResetPasswordVerificationManager();
            verificationManager.generateToken("fake-user");
            var result = verificationManager.contains("fake-user");
            assertThat(result).isTrue();

            verificationManager.generateToken("guqing");
            result = verificationManager.contains("guqing");
            assertThat(result).isTrue();

            result = verificationManager.contains("123");
            assertThat(result).isFalse();
        }
    }

    @Test
    public void removeTest() {
        var verificationManager =
            new EmailPasswordRecoveryServiceImpl.ResetPasswordVerificationManager();
        verificationManager.generateToken("fake-user");
        var result = verificationManager.contains("fake-user");

        verificationManager.removeToken("fake-user");
        result = verificationManager.contains("fake-user");
        assertThat(result).isFalse();
    }

    @Test
    void verifyTokenTestNormal() {
        String username = "guqing";
        var verificationManager =
            new EmailPasswordRecoveryServiceImpl.ResetPasswordVerificationManager();
        var result = verificationManager.verifyToken(username, "fake-code");
        assertThat(result).isFalse();

        var token = verificationManager.generateToken(username);
        result = verificationManager.verifyToken(username, "fake-code");
        assertThat(result).isFalse();

        result = verificationManager.verifyToken(username, token);
        assertThat(result).isTrue();
    }

    @Test
    void verifyTokenFailedAfterMaxAttempts() {
        String username = "guqing";
        var verificationManager =
            new EmailPasswordRecoveryServiceImpl.ResetPasswordVerificationManager();
        var token = verificationManager.generateToken(username);
        for (int i = 0; i <= EmailPasswordRecoveryServiceImpl.MAX_ATTEMPTS; i++) {
            var result = verificationManager.verifyToken(username, "fake-code");
            assertThat(result).isFalse();
        }

        assertThatThrownBy(() -> verificationManager.verifyToken(username, token))
            .isInstanceOf(RateLimitExceededException.class)
            .hasMessage("429 TOO_MANY_REQUESTS \"You have exceeded your quota\"");
    }
}