package run.halo.app.theme.router.factories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.theme.finders.PostFinder;

/**
 * Tests for {@link ArchiveRouteFactory}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class ArchiveRouteFactoryTest extends RouteFactoryTestSuite {
    @Mock
    private PostFinder postFinder;

    @InjectMocks
    private ArchiveRouteFactory archiveRouteFactory;

    @Test
    void create() {
        String prefix = "/new-archives";
        RouterFunction<ServerResponse> routerFunction = archiveRouteFactory.create(prefix);
        WebTestClient client = getWebTestClient(routerFunction);

        client.get()
            .uri(prefix)
            .exchange()
            .expectStatus().isOk();

        client.get()
            .uri(prefix + "/page/1")
            .exchange()
            .expectStatus().isOk();

        client.get()
            .uri(prefix + "/2022/09")
            .exchange()
            .expectStatus().isOk();

        client.get()
            .uri(prefix + "/2022/08/page/1")
            .exchange()
            .expectStatus().isOk();

        client.get()
            .uri(prefix + "/2022/8/page/1")
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.NOT_FOUND);

        client.get()
            .uri("/nothing")
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.NOT_FOUND);
    }
}