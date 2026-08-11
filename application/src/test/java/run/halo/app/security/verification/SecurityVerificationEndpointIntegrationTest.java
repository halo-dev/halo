package run.halo.app.security.verification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.User.UserSpec;
import run.halo.app.core.user.service.EmailVerificationService;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.Metadata;
import run.halo.app.infra.exception.EmailVerificationFailed;
import run.halo.app.security.authentication.twofactor.totp.TotpAuthService;

@SpringBootTest
@AutoConfigureWebTestClient
class SecurityVerificationEndpointIntegrationTest {

    private static final String USERNAME = "faker";

    WebTestClient webClient;

    @Autowired
    ApplicationContext applicationContext;

    @MockitoBean
    UserService userService;

    @MockitoBean
    EmailVerificationService emailVerificationService;

    @MockitoBean
    TotpAuthService totpAuthService;

    @MockitoBean
    RateLimiterRegistry rateLimiterRegistry;

    @BeforeEach
    void setUp() {
        webClient = WebTestClient.bindToApplicationContext(applicationContext)
                .apply(springSecurity())
                .configureClient()
                .build();
        // resilience4j configs (application.yaml) are shadowed by the test application.yaml,
        // so the real registry has no named configs; stub with defaults like other endpoint tests.
        when(rateLimiterRegistry.rateLimiter(anyString(), anyString()))
                .thenAnswer(invocation -> RateLimiter.ofDefaults(invocation.getArgument(0)));
    }

    User user(boolean emailVerified, String totpEncryptedSecret) {
        var spec = new UserSpec();
        spec.setDisplayName("Faker");
        spec.setEmail("faker@halo.run");
        spec.setEmailVerified(emailVerified);
        spec.setTotpEncryptedSecret(totpEncryptedSecret);
        var user = new User();
        user.setSpec(spec);
        var metadata = new Metadata();
        metadata.setName(USERNAME);
        user.setMetadata(metadata);
        return user;
    }

    @Test
    void shouldRedirectToLoginWhenAnonymous() {
        webClient
                .get()
                .uri("/security-verification")
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader()
                .valueEquals("Location", "/login?authentication_required");
    }

    @Test
    void shouldRenderPageWithEmailMethod() {
        when(userService.getUser(USERNAME)).thenReturn(Mono.just(user(true, null)));
        webClient
                .mutateWith(mockUser(USERNAME))
                .get()
                .uri("/security-verification")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    var body = result.getResponseBody();
                    assertThat(body).contains("security-verification-form");
                    assertThat(body).contains("emailCode");
                    assertThat(body).doesNotContain("class=\"method-switcher\"");
                });
    }

    @Test
    void shouldRenderPageWithMethodSwitcher() {
        when(userService.getUser(USERNAME)).thenReturn(Mono.just(user(true, "encrypted-secret")));
        webClient
                .mutateWith(mockUser(USERNAME))
                .get()
                .uri("/security-verification")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    var body = result.getResponseBody();
                    assertThat(body).contains("method-switcher");
                    assertThat(body).contains("totpCode");
                });
    }

    @Test
    void shouldRedirectAwayWhenNoMethodAvailable() {
        when(userService.getUser(USERNAME)).thenReturn(Mono.just(user(false, null)));
        webClient
                .mutateWith(mockUser(USERNAME))
                .get()
                .uri("/security-verification?redirect=/uc/profile")
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader()
                .valueEquals("Location", "/uc/profile");
    }

    @Test
    void shouldRedirectToDefaultWhenNoMethodAvailableAndNoRedirect() {
        when(userService.getUser(USERNAME)).thenReturn(Mono.just(user(false, null)));
        webClient
                .mutateWith(mockUser(USERNAME))
                .get()
                .uri("/security-verification")
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader()
                .valueEquals("Location", "/uc/profile");
    }

    @Test
    void shouldRedirectToDefaultWhenRedirectHasBackslash() {
        when(userService.getUser(USERNAME)).thenReturn(Mono.just(user(false, null)));
        // /\evil.com gets normalized by browsers to the external //evil.com
        webClient
                .mutateWith(mockUser(USERNAME))
                .get()
                .uri("/security-verification?redirect=/\\evil.com")
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader()
                .valueEquals("Location", "/uc/profile");
    }

    @Test
    void shouldSendEmailCodeWhenEmailVerified() {
        when(userService.getUser(USERNAME)).thenReturn(Mono.just(user(true, null)));
        when(emailVerificationService.sendSecurityVerificationCode(USERNAME)).thenReturn(Mono.empty());
        webClient
                .mutateWith(mockUser(USERNAME))
                .mutateWith(csrf())
                .post()
                .uri("/security-verification/email-code")
                .exchange()
                .expectStatus()
                .isAccepted();
    }

    @Test
    void shouldVerifyWithEmailCodeAndRedirect() {
        when(userService.getUser(USERNAME)).thenReturn(Mono.just(user(true, null)));
        when(emailVerificationService.verifySecurityVerificationCode(USERNAME, "123456"))
                .thenReturn(Mono.empty());
        webClient
                .mutateWith(mockUser(USERNAME))
                .mutateWith(csrf())
                .post()
                .uri("/security-verification?redirect=/uc/profile")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("redirect=/uc/profile&emailCode=123456")
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader()
                .valueEquals("Location", "/uc/profile");
    }

    @Test
    void shouldVerifyWithTotpCodeAndRedirect() {
        when(userService.getUser(USERNAME)).thenReturn(Mono.just(user(false, "encrypted-secret")));
        when(totpAuthService.decryptSecret("encrypted-secret")).thenReturn("raw-secret");
        when(totpAuthService.validateTotp("raw-secret", 123456)).thenReturn(true);
        webClient
                .mutateWith(mockUser(USERNAME))
                .mutateWith(csrf())
                .post()
                .uri("/security-verification?redirect=/uc/profile")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("redirect=/uc/profile&totpCode=123456")
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader()
                .valueEquals("Location", "/uc/profile");
    }

    @Test
    void shouldRejectTotpCodeWhenTotpNotConfigured() {
        when(userService.getUser(USERNAME)).thenReturn(Mono.just(user(true, null)));
        webClient
                .mutateWith(mockUser(USERNAME))
                .mutateWith(csrf())
                .post()
                .uri("/security-verification?redirect=/uc/profile")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("redirect=/uc/profile&totpCode=123456")
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader()
                .valueEquals("Location", "/security-verification?error=invalid-code&redirect=/uc/profile");
    }

    @Test
    void shouldRedirectWithRateLimitErrorWhenEmailCodeBlacklisted() {
        when(userService.getUser(USERNAME)).thenReturn(Mono.just(user(true, null)));
        when(emailVerificationService.verifySecurityVerificationCode(USERNAME, "123456"))
                .thenReturn(Mono.error(new EmailVerificationFailed(
                        "Too many attempts. Please try again later.",
                        null,
                        "problemDetail.user.email.verify.maxAttempts",
                        null)));
        webClient
                .mutateWith(mockUser(USERNAME))
                .mutateWith(csrf())
                .post()
                .uri("/security-verification?redirect=/uc/profile")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("redirect=/uc/profile&emailCode=123456")
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader()
                .valueEquals("Location", "/security-verification?error=rate-limit-exceeded&redirect=/uc/profile");
    }

    @Test
    void shouldRedirectWithErrorWhenCodeInvalid() {
        when(userService.getUser(USERNAME)).thenReturn(Mono.just(user(false, "encrypted-secret")));
        when(totpAuthService.decryptSecret("encrypted-secret")).thenReturn("raw-secret");
        when(totpAuthService.validateTotp("raw-secret", 123456)).thenReturn(false);
        webClient
                .mutateWith(mockUser(USERNAME))
                .mutateWith(csrf())
                .post()
                .uri("/security-verification?redirect=/uc/profile")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("redirect=/uc/profile&totpCode=123456")
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader()
                .valueEquals("Location", "/security-verification?error=invalid-code&redirect=/uc/profile");
    }

    @Test
    void shouldRedirectToDefaultWhenRedirectIsExternal() {
        when(userService.getUser(USERNAME)).thenReturn(Mono.just(user(true, null)));
        when(emailVerificationService.verifySecurityVerificationCode(USERNAME, "123456"))
                .thenReturn(Mono.empty());
        webClient
                .mutateWith(mockUser(USERNAME))
                .mutateWith(csrf())
                .post()
                .uri("/security-verification")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("redirect=http://evil.com&emailCode=123456")
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader()
                .valueEquals("Location", "/uc/profile");
    }

    @Test
    void shouldRedirectToDefaultWhenRedirectHasBackslashInPostBody() {
        when(userService.getUser(USERNAME)).thenReturn(Mono.just(user(true, null)));
        when(emailVerificationService.verifySecurityVerificationCode(USERNAME, "123456"))
                .thenReturn(Mono.empty());
        // /\evil.com gets normalized by browsers to the external //evil.com
        webClient
                .mutateWith(mockUser(USERNAME))
                .mutateWith(csrf())
                .post()
                .uri("/security-verification")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("redirect=/\\evil.com&emailCode=123456")
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader()
                .valueEquals("Location", "/uc/profile");
    }
}
