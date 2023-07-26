package run.halo.app.core.extension.endpoint;

import static org.springframework.test.web.reactive.server.WebTestClient.bindToRouterFunction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Tests for {@link SystemInitializationEndpoint}.
 *
 * @author guqing
 * @since 2.9.0
 */
@ExtendWith(MockitoExtension.class)
class SystemInitializationEndpointTest {

    @InjectMocks
    SystemInitializationEndpoint initializationEndpoint;

    WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = bindToRouterFunction(initializationEndpoint.endpoint()).build();
    }

    @Test
    void initialize() {
        webTestClient.post()
            .uri("/system/initialize")
            .exchange()
            .expectStatus()
            .isBadRequest();
    }
}
