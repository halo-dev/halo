package run.halo.app.theme.router.strategy;

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

/**
 * Tests for {@link IndexRouteStrategy}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class IndexRouteStrategyTest extends RouterStrategyTestSuite {
    @Mock
    private PostFinder postFinder;

    @InjectMocks
    private IndexRouteStrategy indexRouteStrategy;

    @Test
    void getRouteFunction() {
        HandlerFunction<ServerResponse> handler = indexRouteStrategy.getHandler();
        RouterFunction<ServerResponse> routeFunction = getRouterFunction();

        List<String> routerPaths = indexRouteStrategy.getRouterPaths("/");
        for (String routerPath : routerPaths) {
            permalinkHttpGetRouter.insert(routerPath, handler);
        }

        final WebTestClient client = getWebTestClient(routeFunction);

        client.get()
            .uri("/")
            .exchange()
            .expectStatus()
            .isOk();

        client.get()
            .uri("/page/1")
            .exchange()
            .expectStatus()
            .isOk();

        client.get()
            .uri("/nothing")
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.NOT_FOUND);
    }
}