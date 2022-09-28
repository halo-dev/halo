package run.halo.app.theme.router.strategy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static run.halo.app.theme.DefaultTemplateEnum.SINGLE_PAGE;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.result.view.ViewResolver;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListResult;
import run.halo.app.theme.finders.SinglePageFinder;
import run.halo.app.theme.router.PermalinkIndexer;

/**
 * Tests for {@link SinglePageRouteStrategy}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class SinglePageRouteStrategyTest {

    @Mock
    private PermalinkIndexer permalinkIndexer;

    @Mock
    private ViewResolver viewResolver;

    @Mock
    private SinglePageFinder singlePageFinder;

    @InjectMocks
    private SinglePageRouteStrategy strategy;

    @BeforeEach
    void setUp() {
        lenient().when(singlePageFinder.list(anyInt(), anyInt()))
            .thenReturn(new ListResult<>(1, 10, 0, List.of()));
        lenient().when(viewResolver.resolveViewName(eq(SINGLE_PAGE.getValue()), any()))
            .thenReturn(Mono.just(new EmptyView()));
    }

    @Test
    void shouldResponse404IfNoPermalinkFound() {
        createClient().get()
            .uri("/nothing")
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    void shouldResponse200IfPermalinkFound() {
        when(permalinkIndexer.getPermalinks(any()))
            .thenReturn(List.of("/fake-slug"));
        when(permalinkIndexer.getNameByPermalink(any(), eq("/fake-slug")))
            .thenReturn("fake-name");
        createClient().get()
            .uri("/fake-slug")
            .exchange()
            .expectStatus()
            .isOk();

        verify(permalinkIndexer).getNameByPermalink(any(), eq("/fake-slug"));
    }

    @Test
    void shouldResponse200IfSlugNameContainsSpecialChars() {
        when(permalinkIndexer.getPermalinks(any()))
            .thenReturn(List.of("/fake%20/%20slug"));
        when(permalinkIndexer.getNameByPermalink(any(), eq("/fake%20/%20slug")))
            .thenReturn("fake-name");
        createClient().get()
            .uri("/fake / slug")
            .exchange()
            .expectStatus().isOk();
        verify(permalinkIndexer).getNameByPermalink(any(), eq("/fake%20/%20slug"));
    }

    @Test
    void shouldResponse200IfSlugNameContainsChineseChars() {
        when(permalinkIndexer.getPermalinks(any()))
            .thenReturn(List.of("/%E4%B8%AD%E6%96%87"));
        when(permalinkIndexer.getNameByPermalink(any(), eq("/%E4%B8%AD%E6%96%87")))
            .thenReturn("fake-name");
        createClient().get()
            .uri("/中文")
            .exchange()
            .expectStatus().isOk();
        verify(permalinkIndexer).getNameByPermalink(any(), eq("/%E4%B8%AD%E6%96%87"));
    }

    WebTestClient createClient() {
        var routeFunction = strategy.getRouteFunction(SINGLE_PAGE.getValue(), null);

        return WebTestClient.bindToRouterFunction(routeFunction)
            .handlerStrategies(HandlerStrategies.builder()
                .viewResolver(viewResolver)
                .build())
            .build();
    }
}
