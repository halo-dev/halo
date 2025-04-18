package run.halo.app.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
@AutoConfigureWebTestClient
class CsrfSecurityTest {

    @Autowired
    WebTestClient webClient;

    @Test
    void shouldNotCheckCsrfForPatAuthentication() {
        webClient.post()
            .uri("/fake")
            .headers(headers -> headers.setBearerAuth("pat_invalid"))
            .exchange()
            .expectStatus()
            .isUnauthorized()
            .expectHeader()
            .exists(HttpHeaders.WWW_AUTHENTICATE);
    }

}
