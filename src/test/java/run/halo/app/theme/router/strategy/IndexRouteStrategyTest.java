package run.halo.app.theme.router.strategy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

/**
 * Tests for {@link IndexRouteStrategy}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class IndexRouteStrategyTest {
    @Mock
    private ViewResolver viewResolver;

    private IndexRouteStrategy indexRouteStrategy;

    @BeforeEach
    void setUp() {
        indexRouteStrategy = new IndexRouteStrategy();
    }

    @Test
    void getRouteFunction() {
        RouterFunction<ServerResponse> routeFunction =
            indexRouteStrategy.getRouteFunction(DefaultTemplateEnum.INDEX.getValue(),
                null);

        WebTestClient client = WebTestClient.bindToRouterFunction(routeFunction)
            .handlerStrategies(HandlerStrategies.builder()
                .viewResolver(viewResolver)
                .build())
            .build();

        when(viewResolver.resolveViewName(eq(DefaultTemplateEnum.INDEX.getValue()), any()))
            .thenReturn(Mono.just(new EmptyView()));

        client.get()
            .uri("/")
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