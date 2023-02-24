package run.halo.app.theme.router.factories;

import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import run.halo.app.theme.finders.CategoryFinder;

/**
 * Tests for {@link CategoriesRouteFactory}.
 *
 * @author guqing
 * @since 2.0.0
 */
class CategoriesRouteFactoryTest extends RouteFactoryTestSuite {

    @Mock
    private CategoryFinder categoryFinder;

    @InjectMocks
    private CategoriesRouteFactory categoriesRouteFactory;

    @Test
    void create() {
        String prefix = "/topics";
        RouterFunction<ServerResponse> routerFunction = categoriesRouteFactory.create(prefix);
        WebTestClient webClient = getWebTestClient(routerFunction);

        when(categoryFinder.listAsTree())
            .thenReturn(Flux.empty());
        webClient.get()
            .uri(prefix)
            .exchange()
            .expectStatus().isOk();
    }
}