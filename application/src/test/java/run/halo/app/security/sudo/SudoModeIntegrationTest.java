package run.halo.app.security.sudo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.springframework.web.reactive.function.BodyInserters.fromFormData;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import run.halo.app.infra.exception.Exceptions;
import run.halo.app.infra.exception.RateLimitExceededException;

@SpringBootTest
@AutoConfigureWebTestClient
class SudoModeIntegrationTest {

    @Autowired
    WebTestClient webClient;

    @MockitoSpyBean
    SudoService sudoService;

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
        "PUT,/apis/api.console.halo.run/v1alpha1/users/bob/password",
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
