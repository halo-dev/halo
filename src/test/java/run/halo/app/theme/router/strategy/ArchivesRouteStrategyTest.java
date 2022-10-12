package run.halo.app.theme.router.strategy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.router.UrlContextListResult;

/**
 * Tests for {@link ArchivesRouteStrategy}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class ArchivesRouteStrategyTest extends RouterStrategyTestSuite {
    @Mock
    private PostFinder postFinder;

    @InjectMocks
    private ArchivesRouteStrategy archivesRouteStrategy;

    @Override
    public void setUp() {
        lenient().when(postFinder.list(any(), any())).thenReturn(
            new UrlContextListResult<>(1, 10, 1, List.of(), null, null));
    }

    @Test
    void getRouteFunctionWhenDefaultPattern() {
        HandlerFunction<ServerResponse> handler = archivesRouteStrategy.getHandler();
        RouterFunction<ServerResponse> routeFunction = getRouterFunction();

        WebTestClient client = getWebTestClient(routeFunction);

        List<String> routerPaths = archivesRouteStrategy.getRouterPaths("/archives");
        for (String routerPath : routerPaths) {
            permalinkHttpGetRouter.insert(routerPath, handler);
        }

        fixedAssertion(client, "/archives");

        client.get()
            .uri("/nothing")
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    private static void fixedAssertion(WebTestClient client, String prefix) {
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
    }

    @Test
    void getRouteFunctionWhenOtherPattern() {
        HandlerFunction<ServerResponse> handler = archivesRouteStrategy.getHandler();
        RouterFunction<ServerResponse> routeFunction = getRouterFunction();

        final WebTestClient client = getWebTestClient(routeFunction);

        List<String> routerPaths = archivesRouteStrategy.getRouterPaths("/archives-test");
        for (String routerPath : routerPaths) {
            permalinkHttpGetRouter.insert(routerPath, handler);
        }

        fixedAssertion(client, "/archives-test");

        client.get()
            .uri("/archives")
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.NOT_FOUND);
    }
}