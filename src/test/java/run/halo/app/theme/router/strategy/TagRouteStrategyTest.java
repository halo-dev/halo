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
import run.halo.app.core.extension.Tag;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.router.PermalinkIndexer;

/**
 * Tests for {@link TagRouteStrategy}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class TagRouteStrategyTest {

    @Mock
    private PermalinkIndexer permalinkIndexer;

    @Mock
    private ViewResolver viewResolver;

    private TagRouteStrategy tagRouteStrategy;

    @BeforeEach
    void setUp() {
        tagRouteStrategy = new TagRouteStrategy(permalinkIndexer);
        GroupVersionKind gvk = GroupVersionKind.fromExtension(Tag.class);
        when(permalinkIndexer.containsSlug(eq(gvk), eq("fake-slug")))
            .thenReturn(true);
        when(permalinkIndexer.getNameBySlug(any(), eq("fake-slug")))
            .thenReturn("fake-name");
    }

    @Test
    void getRouteFunction() {
        RouterFunction<ServerResponse> routeFunction =
            tagRouteStrategy.getRouteFunction(DefaultTemplateEnum.TAG.getValue(),
                "/tags-test");

        WebTestClient client = WebTestClient.bindToRouterFunction(routeFunction)
            .handlerStrategies(HandlerStrategies.builder()
                .viewResolver(viewResolver)
                .build())
            .build();

        when(viewResolver.resolveViewName(eq(DefaultTemplateEnum.TAG.getValue()), any()))
            .thenReturn(Mono.just(new EmptyView()));

        client.get()
            .uri("/tags-test/fake-slug")
            .exchange()
            .expectStatus()
            .isOk();

        client.get()
            .uri("/tags-test/fake-slug/page/1")
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