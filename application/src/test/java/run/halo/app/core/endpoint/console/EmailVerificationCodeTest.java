package run.halo.app.core.endpoint.console;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.validation.Validator;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.user.service.EmailVerificationService;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;

/**
 * Tests for a part of {@link UserEndpoint} about sending email verification code.
 *
 * @author guqing
 * @see UserEndpoint
 * @see EmailVerificationService
 * @since 2.11.0
 */
@ExtendWith(SpringExtension.class)
@WithMockUser(username = "fake-user", password = "fake-password")
class EmailVerificationCodeTest {

    WebTestClient webClient;

    @Mock
    ReactiveExtensionClient client;

    @Mock
    EmailVerificationService emailVerificationService;

    @Mock
    UserService userService;

    @Mock
    RateLimiterRegistry rateLimiterRegistry;

    @Mock
    Validator validator;

    @InjectMocks
    UserEndpoint endpoint;

    @BeforeEach
    void setUp() {
        webClient = WebTestClient.bindToRouterFunction(endpoint.endpoint())
            .apply(springSecurity())
            .build();
    }

    @Test
    void sendEmailVerificationCode() {
        var config = RateLimiterConfig.custom()
            .limitRefreshPeriod(Duration.ofSeconds(10))
            .limitForPeriod(1)
            .build();
        var sendCodeRateLimiter = RateLimiterRegistry.of(config)
            .rateLimiter("send-email-verification-code-fake-user:hi@halo.run");
        when(rateLimiterRegistry.rateLimiter(
            "send-email-verification-code-fake-user:hi@halo.run",
            "send-email-verification-code")
        ).thenReturn(sendCodeRateLimiter);

        var user = new User();
        user.setMetadata(new Metadata());
        user.getMetadata().setName("fake-user");
        user.setSpec(new User.UserSpec());
        user.getSpec().setEmail("hi@halo.run");
        when(emailVerificationService.sendVerificationCode(anyString(), anyString()))
            .thenReturn(Mono.empty());
        webClient.post()
            .uri("/users/-/send-email-verification-code")
            .bodyValue(Map.of("email", "hi@halo.run"))
            .exchange()
            .expectStatus()
            .isOk();

        // request again to trigger rate limit
        webClient.post()
            .uri("/users/-/send-email-verification-code")
            .bodyValue(Map.of("email", "hi@halo.run"))
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    void verifyEmail() {
        var config = RateLimiterConfig.custom()
            .limitRefreshPeriod(Duration.ofSeconds(10))
            .limitForPeriod(1)
            .build();

        var verifyEmailRateLimiter = RateLimiterRegistry.of(config)
            .rateLimiter("verify-email-fake-user");
        when(rateLimiterRegistry.rateLimiter("verify-email-fake-user", "verify-email"))
            .thenReturn(verifyEmailRateLimiter);

        when(emailVerificationService.verify(anyString(), anyString()))
            .thenReturn(Mono.empty());
        when(userService.confirmPassword(anyString(), anyString()))
            .thenReturn(Mono.just(true));
        webClient.post()
            .uri("/users/-/verify-email")
            .bodyValue(Map.of("code", "fake-code-1", "password", "123456"))
            .exchange()
            .expectStatus()
            .isOk();

        // request again to trigger rate limit
        webClient.post()
            .uri("/users/-/verify-email")
            .bodyValue(Map.of("code", "fake-code-2", "password", "123456"))
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }
}
