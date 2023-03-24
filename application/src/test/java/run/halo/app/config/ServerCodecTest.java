package run.halo.app.config;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@SpringBootTest
@AutoConfigureWebTestClient
@Import(ServerCodecTest.TestConfig.class)
class ServerCodecTest {

    static final String INSTANT = "2022-06-09T10:57:30Z";

    static final String LOCAL_DATE_TIME = "2022-06-10T10:57:30";

    @Autowired
    WebTestClient webClient;

    @Test
    @WithMockUser
    void timeSerializationTest() {
        webClient.get().uri("/fake/api/times")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.instant").value(equalTo(INSTANT))
            .jsonPath("$.localDateTime").value(equalTo(LOCAL_DATE_TIME))
        ;
    }

    @Test
    @WithMockUser
    void timeDeserializationTest() {
        webClient
            .mutateWith(csrf())
            .post().uri("/fake/api/time/report")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(Map.of("now", Instant.parse(INSTANT)))
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody(new ParameterizedTypeReference<Map<String, Instant>>() {
            }).isEqualTo(Map.of("now", Instant.parse(INSTANT)))
        ;
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class TestConfig {

        @Bean
        RouterFunction<ServerResponse> timesRouter() {
            return route().GET("/fake/api/times", request -> {
                var times = Map.of("instant", Instant.parse(INSTANT),
                    "localDateTime", LocalDateTime.parse(LOCAL_DATE_TIME));
                return ServerResponse
                    .ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(times);
            }).build();
        }

        @Bean
        RouterFunction<ServerResponse> reportTime() {
            final var type = new ParameterizedTypeReference<Map<String, Instant>>() {
            };
            return route().POST("/fake/api/time/report",
                    contentType(MediaType.APPLICATION_JSON).and(accept(MediaType.APPLICATION_JSON)),
                    request -> ServerResponse.ok()
                        .body(request.bodyToMono(type), type))
                .build();
        }
    }
}
