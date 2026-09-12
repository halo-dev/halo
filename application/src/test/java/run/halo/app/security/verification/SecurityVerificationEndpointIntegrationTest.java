package run.halo.app.security.verification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpCookie;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
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
@Import(SecurityVerificationEndpointIntegrationTest.TestAuthenticationRouteConfiguration.class)
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

    private final Map<String, RateLimiter> rateLimiters = new ConcurrentHashMap<>();

    @BeforeEach
    void setUp() {
        webClient = WebTestClient.bindToApplicationContext(applicationContext)
                .apply(springSecurity())
                .configureClient()
                .build();
        // resilience4j configs (application.yaml) are shadowed by the test application.yaml,
        // so the real registry has no named configs; emulate the registry with per-name
        // caching like the real one, and give totp-validation a 1-permit budget so the
        // rate limit can be exercised deterministically in tests.
        when(rateLimiterRegistry.rateLimiter(anyString(), anyString()))
                .thenAnswer(invocation -> rateLimiters.computeIfAbsent(invocation.getArgument(0), name -> {
                    var configName = invocation.<String>getArgument(1);
                    if ("totp-validation".equals(configName)) {
                        return RateLimiter.of(
                                name,
                                RateLimiterConfig.custom()
                                        .limitForPeriod(1)
                                        .limitRefreshPeriod(Duration.ofHours(1))
                                        .build());
                    }
                    return RateLimiter.ofDefaults(name);
                }));
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
                    assertThat(body).contains("action=\"/security-verification/email\"");
                    assertThat(body).doesNotContain("action=\"/security-verification/totp\"");
                    // the single email form carries exactly one CSRF field
                    assertThat(body).containsOnlyOnce("name=\"_csrf\"");
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
                    assertThat(body).contains("href=\"/security-verification?method=email\"");
                    assertThat(body).contains("href=\"/security-verification?method=totp\"");
                    // email is the default method, so its tab is active and only its form is rendered
                    assertThat(body).contains("class=\"method-tab active\"");
                    assertThat(body).contains("action=\"/security-verification/email\"");
                    assertThat(body).doesNotContain("action=\"/security-verification/totp\"");
                    assertThat(body).doesNotContain("totpCode");
                    // the single rendered form carries exactly one CSRF field
                    assertThat(body).containsOnlyOnce("name=\"_csrf\"");
                });
    }

    @Test
    void shouldRenderTotpMethodWhenRequested() {
        when(userService.getUser(USERNAME)).thenReturn(Mono.just(user(true, "encrypted-secret")));
        webClient
                .mutateWith(mockUser(USERNAME))
                .get()
                .uri("/security-verification?method=totp")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .consumeWith(result -> {
                    var body = result.getResponseBody();
                    assertThat(body).contains("method-switcher");
                    // only the TOTP form is rendered when it is requested explicitly
                    assertThat(body).contains("action=\"/security-verification/totp\"");
                    assertThat(body).contains("totpCode");
                    assertThat(body).doesNotContain("action=\"/security-verification/email\"");
                    assertThat(body).doesNotContain("emailCode");
                    assertThat(body).containsOnlyOnce("name=\"_csrf\"");
                });
    }

    @Test
    void shouldRenderTotpPageByDefaultWhenOnlyTotpAvailable() {
        when(userService.getUser(USERNAME)).thenReturn(Mono.just(user(false, "encrypted-secret")));
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
                    assertThat(body).doesNotContain("class=\"method-switcher\"");
                    assertThat(body).contains("action=\"/security-verification/totp\"");
                    assertThat(body).contains("totpCode");
                    assertThat(body).doesNotContain("emailCode");
                    assertThat(body).containsOnlyOnce("name=\"_csrf\"");
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
                .uri("/security-verification/email?redirect=/uc/profile")
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
                .uri("/security-verification/totp?redirect=/uc/profile")
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
                .uri("/security-verification/totp?redirect=/uc/profile")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("redirect=/uc/profile&totpCode=123456")
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader()
                .valueEquals("Location", "/security-verification?error=invalid-code&redirect=/uc/profile&method=totp");
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
                .uri("/security-verification/email?redirect=/uc/profile")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("redirect=/uc/profile&emailCode=123456")
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader()
                .valueEquals(
                        "Location",
                        "/security-verification?error=rate-limit-exceeded&redirect=/uc/profile&method=email");
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
                .uri("/security-verification/totp?redirect=/uc/profile")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("redirect=/uc/profile&totpCode=123456")
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader()
                .valueEquals("Location", "/security-verification?error=invalid-code&redirect=/uc/profile&method=totp");
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
                .uri("/security-verification/email")
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
                .uri("/security-verification/email")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("redirect=/\\evil.com&emailCode=123456")
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader()
                .valueEquals("Location", "/uc/profile");
    }

    @Test
    void shouldAllowPasswordChangeAfterVerificationInSameSession() {
        var user = userWithPassword(true, null);
        when(userService.getUser(USERNAME)).thenReturn(Mono.just(user));
        when(emailVerificationService.verifySecurityVerificationCode(USERNAME, "123456"))
                .thenReturn(Mono.empty());
        when(userService.confirmPassword(USERNAME, "old-password")).thenReturn(Mono.just(true));
        when(userService.updateWithRawPassword(USERNAME, "new-password")).thenReturn(Mono.just(user));

        var session = establishAuthenticatedSession();

        // The password change is blocked before the session is verified.
        webClient
                .mutateWith(csrf())
                .put()
                .uri("/apis/uc.api.halo.run/v1alpha1/users/-/password")
                .cookie(session.getName(), session.getValue())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "oldPassword": "old-password",
                          "password": "new-password"
                        }\
                        """)
                .exchange()
                .expectStatus()
                .isForbidden();

        // Verify via email code within the same session.
        webClient
                .mutateWith(csrf())
                .post()
                .uri("/security-verification/email?redirect=/uc/profile")
                .cookie(session.getName(), session.getValue())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("redirect=/uc/profile&emailCode=123456")
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader()
                .valueEquals("Location", "/uc/profile");

        // The verified session is allowed to change the password.
        webClient
                .mutateWith(csrf())
                .put()
                .uri("/apis/uc.api.halo.run/v1alpha1/users/-/password")
                .cookie(session.getName(), session.getValue())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "oldPassword": "old-password",
                          "password": "new-password"
                        }\
                        """)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.passwordSet")
                .isEqualTo(true);

        verify(userService, times(1)).updateWithRawPassword(USERNAME, "new-password");
    }

    @Test
    void shouldRedirectWithRateLimitErrorWhenVerificationAttemptsExceeded() {
        when(userService.getUser(USERNAME)).thenReturn(Mono.just(user(true, null)));
        when(emailVerificationService.verifySecurityVerificationCode(USERNAME, "123456"))
                .thenReturn(Mono.empty());

        var session = establishAuthenticatedSession();

        // First attempt consumes the only permit budgeted for this session.
        webClient
                .mutateWith(csrf())
                .post()
                .uri("/security-verification/email?redirect=/uc/profile")
                .cookie(session.getName(), session.getValue())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("redirect=/uc/profile&emailCode=123456")
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader()
                .valueEquals("Location", "/uc/profile");

        // Second attempt in the same session exceeds the limit.
        webClient
                .mutateWith(csrf())
                .post()
                .uri("/security-verification/email?redirect=/uc/profile")
                .cookie(session.getName(), session.getValue())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("redirect=/uc/profile&emailCode=123456")
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader()
                .valueEquals(
                        "Location",
                        "/security-verification?error=rate-limit-exceeded&redirect=/uc/profile&method=email");
    }

    User userWithPassword(boolean emailVerified, String totpEncryptedSecret) {
        var user = user(emailVerified, totpEncryptedSecret);
        user.getSpec().setPassword("fake-encoded-password");
        return user;
    }

    private HttpCookie establishAuthenticatedSession() {
        var result = webClient
                .mutateWith(csrf())
                .post()
                .uri("/login/test/authentication")
                .exchange()
                .expectStatus()
                .isNoContent()
                .expectCookie()
                .exists("SESSION")
                .expectBody()
                .returnResult();
        return result.getResponseCookies().getFirst("SESSION");
    }

    @TestConfiguration
    static class TestAuthenticationRouteConfiguration {

        @Bean
        @Order(Ordered.HIGHEST_PRECEDENCE)
        RouterFunction<ServerResponse> testAuthenticationRoute(
                ServerSecurityContextRepository securityContextRepository) {
            return RouterFunctions.route()
                    .POST("/login/test/authentication", request -> {
                        var authentication = new UsernamePasswordAuthenticationToken(
                                USERNAME, "password", createAuthorityList("ROLE_authenticated"));
                        return securityContextRepository
                                .save(request.exchange(), new SecurityContextImpl(authentication))
                                .then(ServerResponse.noContent().build());
                    })
                    .build();
        }
    }
}
