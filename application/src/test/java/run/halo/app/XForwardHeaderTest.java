package run.halo.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.test.StepVerifier;

@SpringBootTest(webEnvironment = RANDOM_PORT,
    properties = "server.forward-headers-strategy=framework")
class XForwardHeaderTest {

    @LocalServerPort
    int port;

    @Test
    void shouldGetCorrectProtoFromXForwardHeaders() {
        var response = WebClient.create("http://localhost:" + port)
            .get().uri("/print-uri")
            .header("X-Forwarded-Proto", "https")
            .header("X-Forwarded-Host", "halo.run")
            .header("X-Forwarded-Port", "6666")
            .retrieve()
            .toEntity(String.class);
        StepVerifier.create(response)
            .assertNext(entity -> {
                assertEquals(HttpStatus.OK, entity.getStatusCode());
                assertEquals("\"https://halo.run:6666/print-uri\"", entity.getBody());
            })
            .verifyComplete();
    }

    @TestConfiguration
    static class Configuration {

        @Bean
        RouterFunction<ServerResponse> printUri() {
            return route(GET("/print-uri"),
                request -> {
                    var uri = request.exchange().getRequest().getURI();
                    return ServerResponse.ok().bodyValue(uri);
                });
        }
    }
}
