package run.halo.app.config;

import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(properties = "halo.console.location=classpath:/console/")
@AutoConfigureWebTestClient
class WebFluxConfigTest {

    @Autowired
    WebTestClient webClient;

    @Nested
    class ConsoleRequest {

        @Test
        void shouldRedirect() {
            List.of("/console", "/console/index", "/console/index.html")
                .forEach(index -> {
                    webClient.get().uri(index)
                        .exchange()
                        .expectStatus().isPermanentRedirect()
                        .expectHeader().location("/console/");
                });
        }

        @Test
        void shouldRequestConsoleIndex() {
            webClient.get().uri("/console/")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("console index\n");
        }
    }

}