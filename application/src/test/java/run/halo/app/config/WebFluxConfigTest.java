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
        void shouldRequestConsoleIndex() {
            List.of(
                    "/console",
                    "/console/index",
                    "/console/index.html",
                    "/console/dashboard",
                    "/console/fake"
                )
                .forEach(uri -> webClient.get().uri(uri)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(String.class).isEqualTo("console index\n"));
        }

        @Test
        void shouldRequestConsoleAssetsCorrectly() {
            webClient.get().uri("/console/assets/fake.txt")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("fake.\n");
        }

        @Test
        void shouldResponseNotFoundWhenAssetsNotExist() {
            webClient.get().uri("/console/assets/not-found.txt")
                .exchange()
                .expectStatus().isNotFound();
        }
    }

}