package run.halo.app.theme.router.strategy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Category;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.ListResult;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.router.PermalinkIndexer;

/**
 * Tests for {@link CategoryRouteStrategy}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class CategoryRouteStrategyTest {
    @Mock
    private PermalinkIndexer permalinkIndexer;

    @Mock
    private ViewResolver viewResolver;

    @Mock
    private PostFinder postFinder;

    @InjectMocks
    private CategoryRouteStrategy categoryRouteStrategy;

    @BeforeEach
    void setUp() {
        GroupVersionKind gvk = GroupVersionKind.fromExtension(Category.class);
        when(permalinkIndexer.containsSlug(eq(gvk), eq("category-slug-1")))
            .thenReturn(true);
        when(permalinkIndexer.containsSlug(eq(gvk), eq("category-slug-2")))
            .thenReturn(true);
        when(permalinkIndexer.getNameBySlug(any(), eq("category-slug-1")))
            .thenReturn("category-name-1");
        when(permalinkIndexer.getNameBySlug(any(), eq("category-slug-2")))
            .thenReturn("category-name-2");

        lenient().when(postFinder.listByCategory(anyInt(), anyInt(), any()))
            .thenReturn(new ListResult<>(1, 10, 0, List.of()));
    }

    @Test
    void getRouteFunction() {
        RouterFunction<ServerResponse> routeFunction =
            categoryRouteStrategy.getRouteFunction(DefaultTemplateEnum.CATEGORY.getValue(),
                "/categories-test");

        WebTestClient client = WebTestClient.bindToRouterFunction(routeFunction)
            .handlerStrategies(HandlerStrategies.builder()
                .viewResolver(viewResolver)
                .build())
            .build();

        when(viewResolver.resolveViewName(eq(DefaultTemplateEnum.CATEGORY.getValue()), any()))
            .thenReturn(Mono.just(new EmptyView()));

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