package run.halo.app.security.sudo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.springframework.web.reactive.function.BodyInserters.fromFormData;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

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
                .jsonPath("$.methods[0].sendable")
                .isEqualTo(false);
    }

    @Test
    @WithMockUser(username = "alice")
    void shouldSendSudoCode() {
        doReturn(Mono.empty()).when(sudoService).sendCode(eq("email"), any());

        webClient
                .post()
                .uri("/sudo/code")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(fromFormData("method", "email"))
                .exchange()
                .expectStatus()
                .isNoContent();

        verify(sudoService).sendCode(eq("email"), any());
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
    void shouldRejectEmptyConfirmBody() {
        webClient
                .post()
                .uri("/sudo/confirm")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .exchange()
                .expectStatus()
                .isBadRequest();
    }
}
