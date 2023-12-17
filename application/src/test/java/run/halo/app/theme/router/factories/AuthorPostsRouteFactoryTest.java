package run.halo.app.theme.router.factories;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.extension.ReactiveExtensionClient;

/**
 * Tests for {@link AuthorPostsRouteFactory}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class AuthorPostsRouteFactoryTest extends RouteFactoryTestSuite {
    @Mock
    ReactiveExtensionClient client;
    @InjectMocks
    AuthorPostsRouteFactory authorPostsRouteFactory;

    @Test
    void create() {
        RouterFunction<ServerResponse> routerFunction = authorPostsRouteFactory.create(null);
        WebTestClient webClient = getWebTestClient(routerFunction);

        when(client.fetch(eq(User.class), eq("fake-user")))
            .thenReturn(Mono.just(new User()));
        webClient.get()
            .uri("/authors/fake-user")
            .exchange()
            .expectStatus().isOk();
    }
}