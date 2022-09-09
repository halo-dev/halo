package run.halo.app.theme.router.strategy;

import static org.mockito.ArgumentMatchers.any;
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
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.CategoryFinder;

/**
 * Tests for {@link CategoriesRouteStrategy}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class CategoriesRouteStrategyTest {
    @Mock
    private ViewResolver viewResolver;

    @Mock
    private CategoryFinder categoryFinder;

    @InjectMocks
    private CategoriesRouteStrategy categoriesRouteStrategy;

    @BeforeEach
    void setUp() {
        lenient().when(categoryFinder.listAsTree())
            .thenReturn(List.of());
    }

    @Test
    void getRouteFunction() {
        RouterFunction<ServerResponse> routeFunction =
            categoriesRouteStrategy.getRouteFunction(DefaultTemplateEnum.ARCHIVES.getValue(),
                "/categories-test");

        WebTestClient client = WebTestClient.bindToRouterFunction(routeFunction)
            .handlerStrategies(HandlerStrategies.builder()
                .viewResolver(viewResolver)
                .build())
            .build();

        when(viewResolver.resolveViewName(eq(DefaultTemplateEnum.CATEGORIES.getValue()), any()))
            .thenReturn(Mono.just(new EmptyView()));

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