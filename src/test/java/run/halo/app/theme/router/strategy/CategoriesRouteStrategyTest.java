package run.halo.app.theme.router.strategy;

import static org.mockito.Mockito.when;

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
import reactor.core.publisher.Flux;
import run.halo.app.theme.finders.CategoryFinder;

/**
 * Tests for {@link CategoriesRouteStrategy}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class CategoriesRouteStrategyTest extends RouterStrategyTestSuite {
    @Mock
    private CategoryFinder categoryFinder;

    @InjectMocks
    private CategoriesRouteStrategy categoriesRouteStrategy;

    @Test
    void getRouteFunction() {
        HandlerFunction<ServerResponse> handler = categoriesRouteStrategy.getHandler();
        RouterFunction<ServerResponse> routeFunction = getRouterFunction();

        WebTestClient client = getWebTestClient(routeFunction);

        List<String> routerPaths = categoriesRouteStrategy.getRouterPaths("/categories-test");
        for (String routerPath : routerPaths) {
            permalinkHttpGetRouter.insert(routerPath, handler);
        }

        when(categoryFinder.listAsTree()).thenReturn(Flux.empty());
        client.get()
            .uri("/categories-test")
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