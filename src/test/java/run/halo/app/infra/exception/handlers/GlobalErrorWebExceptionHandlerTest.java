package run.halo.app.infra.exception.handlers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.infra.exception.AccessDeniedException;
import run.halo.app.infra.exception.NotFoundException;
import run.halo.app.infra.exception.ThemeUninstallException;

/**
 * Tests for {@link GlobalErrorWebExceptionHandler}.
 *
 * @author guqing
 * @since 2.0.0
 */
@SpringBootTest
@AutoConfigureWebTestClient
class GlobalErrorWebExceptionHandlerTest {
    @Autowired
    private WebTestClient client;

    @Test
    void renderErrorResponseWhenNotFoundError() {
        client.get()
            .uri(uriBuilder -> uriBuilder.path("/hello/errors")
                .queryParam("type", "notFound")
                .build())
            .exchange()
            .expectStatus().isNotFound()
            .expectBody()
            .jsonPath("$.title").isEqualTo("Not Found")
            .jsonPath("$.detail").isEqualTo("Not Found")
            .jsonPath("$.instance").isEqualTo("/hello/errors")
            .jsonPath("$.status").isEqualTo(404);
    }

    @Test
    void renderErrorResponseWhenBadRequestError() {
        client.get()
            .uri(uriBuilder -> uriBuilder.path("/hello/errors")
                .queryParam("type", "badRequest1")
                .build())
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody()
            .jsonPath("$.title").isEqualTo("Bad Request")
            .jsonPath("$.detail").isEqualTo("Bad Request")
            .jsonPath("$.instance").isEqualTo("/hello/errors")
            .jsonPath("$.status").isEqualTo(400);

        client.get()
            .uri(uriBuilder -> uriBuilder.path("/hello/errors")
                .queryParam("type", "badRequest2")
                .build())
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody()
            .jsonPath("$.title").isEqualTo("Bad Request")
            .jsonPath("$.detail").isEqualTo("Bad Request for state")
            .jsonPath("$.instance").isEqualTo("/hello/errors")
            .jsonPath("$.status").isEqualTo(400);

        client.get()
            .uri(uriBuilder -> uriBuilder.path("/hello/errors")
                .queryParam("type", "badRequest3")
                .build())
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody()
            .jsonPath("$.title").isEqualTo("Bad Request")
            .jsonPath("$.detail").isEqualTo("theme install error")
            .jsonPath("$.instance").isEqualTo("/hello/errors")
            .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    void renderErrorResponseWhenAccessDeniedError() {
        client.get()
            .uri(uriBuilder -> uriBuilder.path("/hello/errors")
                .queryParam("type", "accessDenied")
                .build())
            .exchange()
            .expectStatus().isForbidden()
            .expectBody()
            .jsonPath("$.title").isEqualTo("Forbidden")
            .jsonPath("$.detail").isEqualTo("Access Denied")
            .jsonPath("$.instance").isEqualTo("/hello/errors")
            .jsonPath("$.status").isEqualTo(403);
    }

    @Test
    void renderErrorResponseWhenOptimisticLockingFailureError() {
        client.get()
            .uri(uriBuilder -> uriBuilder.path("/hello/errors")
                .queryParam("type", "conflict")
                .build())
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.CONFLICT)
            .expectBody()
            .jsonPath("$.title").isEqualTo("Conflict")
            .jsonPath("$.detail").isEqualTo("Version conflict")
            .jsonPath("$.instance").isEqualTo("/hello/errors")
            .jsonPath("$.status").isEqualTo(409);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public RouterFunction<ServerResponse> routerFunction() {
            return RouterFunctions.route()
                .GET("/hello/errors", request -> {
                    String type = request.queryParam("type").orElse("other");
                    if (type.equals("notFound")) {
                        throw new NotFoundException("Not Found");
                    }
                    if (type.equals("badRequest1")) {
                        throw new IllegalArgumentException("Bad Request");
                    }
                    if (type.equals("badRequest2")) {
                        throw new IllegalStateException("Bad Request for state");
                    }
                    if (type.equals("badRequest3")) {
                        throw new ThemeUninstallException("theme install error");
                    }
                    if (type.equals("conflict")) {
                        throw new OptimisticLockingFailureException("Version conflict");
                    }
                    if (type.equals("accessDenied")) {
                        throw new AccessDeniedException("Access Denied");
                    }
                    throw new RuntimeException("Unknown Error");
                })
                .build();
        }
    }
}