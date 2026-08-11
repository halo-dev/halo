package run.halo.app.security.verification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.User.UserSpec;
import run.halo.app.core.user.service.EmailVerificationService;
import run.halo.app.core.user.service.UserService;
import run.halo.app.extension.Metadata;
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

    @BeforeEach
    void setUp() {
        webClient = WebTestClient.bindToApplicationContext(applicationContext)
                .apply(springSecurity())
                .configureClient()
                .build();
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
}
