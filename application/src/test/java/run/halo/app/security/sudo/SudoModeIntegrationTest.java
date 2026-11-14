package run.halo.app.security.sudo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.web.reactive.function.BodyInserters.fromFormData;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.exception.Exceptions;
import run.halo.app.infra.exception.RateLimitExceededException;

@SpringBootTest
@AutoConfigureWebTestClient
class SudoModeIntegrationTest {

    @Autowired
    WebTestClient webClient;

    @MockitoSpyBean
    SudoService sudoService;

    @Autowired
    ReactiveExtensionClient client;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    void shouldRejectAnonymousSudoStatus() {
        webClient.get().uri("/sudo").exchange().expectStatus().isForbidden();
    }

    @Test
    @WithMockUser(username = "alice")
    void shouldGetSudoStatus() {
        doReturn(Mono.just(new SudoStatus(
                        true, Instant.parse("2026-01-01T00:30:00Z"), List.of(new SudoMethod("totp", false, null)))))
                .when(sudoService)
                .status(any());

        webClient
                .get()
                .uri("/sudo")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.active")
                .isEqualTo(true)
                .jsonPath("$.expiresAt")
                .isEqualTo("2026-01-01T00:30:00Z")
                .jsonPath("$.methods[0].name")
                .isEqualTo("totp")
                .jsonPath("$.methods[0].canSendCode")
                .isEqualTo(false);
    }

    @Test
    @WithMockUser(username = "alice")
    void shouldSendSudoCode() {
        doReturn(Mono.empty()).when(sudoService).sendCode(eq("email"));

        webClient
                .post()
                .uri("/sudo/code")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(fromFormData("method", "email"))
                .exchange()
                .expectStatus()
                .isNoContent();

        verify(sudoService).sendCode(eq("email"));
    }

    @Test
    @WithMockUser(username = "alice")
    void shouldConfirmSudo() {
        doReturn(Mono.empty()).when(sudoService).confirm(eq("totp"), eq("123456"), any());

        webClient
                .post()
                .uri("/sudo/confirm")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(fromFormData("method", "totp").with("code", "123456"))
                .exchange()
                .expectStatus()
                .isNoContent();

        verify(sudoService).confirm(eq("totp"), eq("123456"), any());
    }

    @Test
    @WithMockUser(username = "alice")
    void shouldRejectSendCodeWhenRateLimited() {
        doReturn(Mono.error(new RateLimitExceededException(null)))
                .when(sudoService)
                .sendCode(eq("email"));

        webClient
                .post()
                .uri("/sudo/code")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(fromFormData("method", "email"))
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    @WithMockUser(username = "alice")
    void shouldRejectConfirmWhenRateLimited() {
        doReturn(Mono.error(new RateLimitExceededException(null)))
                .when(sudoService)
                .confirm(eq("totp"), eq("123456"), any());

        webClient
                .post()
                .uri("/sudo/confirm")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(fromFormData("method", "totp").with("code", "123456"))
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    @WithMockUser(username = "alice")
    void shouldRejectEmptyConfirmBody() {
        webClient
                .post()
                .uri("/sudo/confirm")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @ParameterizedTest
    @CsvSource({
        "PUT,/apis/uc.api.halo.run/v1alpha1/users/-/password",
        "PUT,/apis/api.console.halo.run/v1alpha1/users/-/password",
        "POST,/apis/uc.api.security.halo.run/v1alpha1/personalaccesstokens",
        "DELETE,/apis/uc.api.security.halo.run/v1alpha1/personalaccesstokens/pat-1",
        "PUT,/apis/uc.api.security.halo.run/v1alpha1/personalaccesstokens/pat-1/actions/revocation",
        "PUT,/apis/uc.api.security.halo.run/v1alpha1/personalaccesstokens/pat-1/actions/restoration"
    })
    @WithMockUser(username = "alice")
    void shouldReportSudoRequiredForSensitiveApis(HttpMethod method, String path) {
        doReturn(Mono.error(new SudoRequiredException(List.of("totp", "email"))))
                .when(sudoService)
                .requireSudo(any(), eq("alice"));

        webClient
                .method(method)
                .uri(path)
                .exchange()
                .expectStatus()
                .isForbidden()
                .expectBody()
                .jsonPath("$.type")
                .isEqualTo(Exceptions.SUDO_REQUIRED_TYPE)
                .jsonPath("$.methods[0]")
                .isEqualTo("totp")
                .jsonPath("$.methods[1]")
                .isEqualTo("email");
    }

    @ParameterizedTest
    @ValueSource(strings = {"alice", "bob"})
    @WithMockUser(username = "alice", roles = "super-role")
    void shouldResetPasswordWithoutSudoForSuperAdministrator(String username) {
        var user = createUserWithPassword(username);
        doReturn(Mono.error(new SudoRequiredException(List.of("totp", "email"))))
                .when(sudoService)
                .requireSudo(any(), eq("alice"));
        try {
            webClient
                    .put()
                    .uri("/apis/api.console.halo.run/v1alpha1/users/{name}/password", username)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("password", "NewPassword123!"))
                    .exchange()
                    .expectStatus()
                    .isOk();

            var updatedUser = client.get(User.class, username).block();
            assertThat(passwordEncoder.matches(
                            "NewPassword123!", updatedUser.getSpec().getPassword()))
                    .isTrue();
            verify(sudoService, never()).requireSudo(any(), any());
        } finally {
            client.delete(client.get(User.class, user.getMetadata().getName()).block())
                    .block();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"charlie", "dave"})
    @WithMockUser(username = "charlie", roles = "authenticated")
    void shouldRejectPasswordResetForNonSuperAdministrator(String username) {
        var user = createUserWithPassword(username);
        try {
            webClient
                    .put()
                    .uri("/apis/api.console.halo.run/v1alpha1/users/{name}/password", username)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("password", "NewPassword123!"))
                    .exchange()
                    .expectStatus()
                    .isForbidden();

            var storedUser = client.get(User.class, username).block();
            assertThat(storedUser.getSpec().getPassword())
                    .isEqualTo(user.getSpec().getPassword());
            verify(sudoService, never()).requireSudo(any(), any());
        } finally {
            client.delete(client.get(User.class, user.getMetadata().getName()).block())
                    .block();
        }
    }

    private User createUserWithPassword(String username) {
        var user = new User();
        user.setMetadata(new Metadata());
        user.getMetadata().setName(username);
        user.getSpec().setDisplayName(username);
        user.getSpec().setEmail(username + "@example.com");
        user.getSpec().setEmailVerified(true);
        user.getSpec().setPassword(passwordEncoder.encode("OldPassword123!"));
        return client.create(user).block();
    }

    @Test
    void shouldRejectJwtOnSudoProtocol() {
        webClient
                .mutateWith(SecurityMockServerConfigurers.mockJwt())
                .get()
                .uri("/sudo")
                .exchange()
                .expectStatus()
                .isForbidden();
    }
}
