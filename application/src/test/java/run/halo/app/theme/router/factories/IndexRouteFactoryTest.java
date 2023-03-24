package run.halo.app.theme.router.factories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.theme.finders.PostFinder;

/**
 * Tests for {@link IndexRouteFactory}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class IndexRouteFactoryTest extends RouteFactoryTestSuite {
    @Mock
    private PostFinder postFinder;

    @InjectMocks
    private IndexRouteFactory indexRouteFactory;

    @Test
    void create() {
        RouterFunction<ServerResponse> routerFunction = indexRouteFactory.create("/");
        WebTestClient webTestClient = getWebTestClient(routerFunction);

        webTestClient.get()
            .uri("/")
            .exchange()
            .expectStatus().isOk();

        webTestClient.get()
            .uri("/page/1")
            .exchange()
            .expectStatus().isOk();
    }
}