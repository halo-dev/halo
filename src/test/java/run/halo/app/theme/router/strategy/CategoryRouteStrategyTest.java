package run.halo.app.theme.router.strategy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.theme.finders.CategoryFinder;
import run.halo.app.theme.finders.PostFinder;

/**
 * Tests for {@link CategoryRouteStrategy}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class CategoryRouteStrategyTest extends RouterStrategyTestSuite {

    @Mock
    private PostFinder postFinder;

    @Mock
    private CategoryFinder categoryFinder;

    @InjectMocks
    private CategoryRouteStrategy categoryRouteStrategy;

    @Test
    void getRouteFunction() {
        RouterFunction<ServerResponse> routeFunction = getRouterFunction();
        final WebTestClient client = getWebTestClient(routeFunction);

        permalinkHttpGetRouter.insert("/categories-test/category-slug-1",
            categoryRouteStrategy.getHandler(null, "category-slug-1"));
        permalinkHttpGetRouter.insert("/categories-test/category-slug-2",
            categoryRouteStrategy.getHandler(null, "category-slug-2"));

        when(categoryFinder.getByName(any())).thenReturn(Mono.empty());

        // /{prefix}/{slug}
        client.get()
            .uri("/categories-test/category-slug-1")
            .exchange()
            .expectStatus()
            .isOk();

        // /{prefix}/{slug}
        client.get()
            .uri("/categories-test/category-slug-2")
            .exchange()
            .expectStatus()
            .isOk();

        // /{prefix}/{slug}/page/{page}
        client.get()
            .uri("/categories-test/category-slug-2/page/1")
            .exchange()
            .expectStatus()
            .isOk();

        client.get()
            .uri("/categories-test/not-exist-slug")
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