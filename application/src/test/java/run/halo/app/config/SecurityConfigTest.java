package run.halo.app.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.web.server.header.StrictTransportSecurityServerHttpHeadersWriter.STRICT_TRANSPORT_SECURITY;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
@AutoConfigureWebTestClient
class SecurityConfigTest {

    @Autowired
    WebTestClient webClient;

    @Test
    void shouldNotIncludeSubdomainForHstsHeader() {
        webClient.get()
            .uri(builder -> builder.scheme("https").path("/fake").build())
            .accept(MediaType.TEXT_HTML)
            .exchange()
            .expectHeader()
            .value(STRICT_TRANSPORT_SECURITY,
                hsts -> assertFalse(hsts.contains("includeSubDomains")));

        webClient.get()
            .uri(builder -> builder.scheme("https").path("/apis/fake").build())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectHeader()
            .value(STRICT_TRANSPORT_SECURITY,
                hsts -> assertFalse(hsts.contains("includeSubDomains")));
    }

}
