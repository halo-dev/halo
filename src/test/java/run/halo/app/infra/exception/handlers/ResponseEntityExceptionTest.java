package run.halo.app.infra.exception.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebInputException;

@SpringBootTest
@AutoConfigureWebTestClient
class ResponseEntityExceptionTest {

    @Autowired
    WebTestClient webClient;

    @Test
    void shouldOkForGreetingEndpoint() {
        webClient.get().uri("/test")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class).isEqualTo("Hello Halo");
    }

    @Test
    void shouldGetErrorIfErrorIsTrue() {
        webClient.get().uri("/test?error=true")
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody(ProblemDetail.class)
            .value(problemDetail -> {
                assertEquals("Bad Request", problemDetail.getTitle());
                assertEquals("You have to input something", problemDetail.getDetail());
            });
    }

    @TestConfiguration
    static class TestConfig {

        @RestController
        @RequestMapping("/test")
        static class TestController {

            @GetMapping
            ResponseEntity<String> greet(@RequestParam(required = false) boolean error) {
                if (error) {
                    throw new ServerWebInputException("You have to input something");
                }
                return ResponseEntity.ok("Hello Halo");
            }
        }
    }
}
