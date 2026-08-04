package run.halo.app.security.completion;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.user.service.EmailVerificationService;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;

@SpringBootTest
@AutoConfigureWebTestClient
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@WithMockUser(username = "fake-user", roles = "authenticated")
class EmailCompletionEndpointIntegrationTest {

    @Autowired
    WebTestClient webClient;

    @Autowired
    ReactiveExtensionClient client;

    @MockitoSpyBean
    SystemConfigFetcher systemConfigFetcher;

    @MockitoBean
    EmailVerificationService emailVerificationService;

    @MockitoBean
    RateLimiterRegistry rateLimiterRegistry;

    @BeforeEach
    void setUp() {
        webClient = webClient.mutateWith(SecurityMockServerConfigurers.csrf());
        var setting = new SystemSetting.User();
        setting.setMustVerifyEmailOnRegistration(true);
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(setting));
        createUser("fake-user", null, false);
    }

    void createUser(String name, String email, boolean verified) {
        var user = new User();
        user.setMetadata(new Metadata());
        user.getMetadata().setName(name);
        var spec = new User.UserSpec();
        spec.setDisplayName("Fake User");
        spec.setEmail(email);
        spec.setEmailVerified(verified);
        spec.setRegisteredAt(Instant.now());
        user.setSpec(spec);
        client.create(user).block();
    }

    @Test
    void shouldRenderPage() {
        webClient
                .get()
                .uri("/complete-profile")
                .header(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .value(body -> org.assertj.core.api.Assertions.assertThat(body).contains("完善资料"));
    }

    @Test
    void shouldRejectWithoutCodeWhenVerificationRequired() {
        webClient
                .post()
                .uri("/complete-profile")
                .bodyValue("email=fake%40example.com")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header(HttpHeaders.ACCEPT_LANGUAGE, "zh-CN")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .value(body -> org.assertj.core.api.Assertions.assertThat(body).contains("验证码"));
    }

    @Test
    void shouldVerifyEmailAndRedirect() {
        when(emailVerificationService.verify(eq("fake-user"), eq("123456"))).thenReturn(Mono.empty());

        webClient
                .post()
                .uri("/complete-profile")
                .bodyValue("email=fake%40example.com&code=123456")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .exchange()
                .expectStatus()
                .isFound()
                .expectHeader()
                .location("/uc");

        verify(emailVerificationService).verify(eq("fake-user"), eq("123456"));
    }

    @Test
    void shouldResetEmailVerifiedWhenSavingNewEmailWithoutCode() {
        var setting = new SystemSetting.User();
        setting.setMustVerifyEmailOnRegistration(false);
        when(systemConfigFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class))
                .thenReturn(Mono.just(setting));

        // Give the existing fake-user a verified email.
        var user = client.fetch(User.class, "fake-user").block();
        user.getSpec().setEmail("old@example.com");
        user.getSpec().setEmailVerified(true);
        client.update(user).block();

        webClient
                .post()
                .uri("/complete-profile")
                .bodyValue("email=new%40example.com")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .exchange()
                .expectStatus()
                .isFound()
                .expectHeader()
                .location("/uc");

        var updated = client.fetch(User.class, "fake-user").block();
        org.assertj.core.api.Assertions.assertThat(updated.getSpec().getEmail()).isEqualTo("new@example.com");
        org.assertj.core.api.Assertions.assertThat(updated.getSpec().isEmailVerified())
                .isFalse();
    }

    @Test
    void shouldSendEmailCode() {
        when(emailVerificationService.sendVerificationCode(anyString(), anyString()))
                .thenReturn(Mono.empty());

        var config = RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofSeconds(10))
                .limitForPeriod(1)
                .build();
        var sendCodeRateLimiter = RateLimiterRegistry.of(config).rateLimiter("send-email-verification-code");
        when(rateLimiterRegistry.rateLimiter(anyString(), eq("send-email-verification-code")))
                .thenReturn(sendCodeRateLimiter);

        webClient
                .post()
                .uri("/complete-profile/send-email-code")
                .bodyValue("{\"email\":\"fake@example.com\"}")
                .header("Content-Type", "application/json")
                .exchange()
                .expectStatus()
                .isAccepted();

        verify(emailVerificationService).sendVerificationCode(eq("fake-user"), eq("fake@example.com"));
    }
}
