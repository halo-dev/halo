package run.halo.app.theme.router.strategy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static run.halo.app.theme.DefaultTemplateEnum.SINGLE_PAGE;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListResult;
import run.halo.app.theme.finders.SinglePageFinder;

/**
 * Tests for {@link SinglePageRouteStrategy}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class SinglePageRouteStrategyTest extends RouterStrategyTestSuite {
    @Mock
    private SinglePageFinder singlePageFinder;

    @InjectMocks
    private SinglePageRouteStrategy strategy;

    @Override
    public void setUp() {
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
        permalinkHttpGetRouter.insert("/fake-slug",
            strategy.getHandler(getThemeRouteRules(), "fake-name"));
        createClient().get()
            .uri("/fake-slug")
            .exchange()
            .expectStatus()
            .isOk();
    }

    @Test
    void shouldResponse200IfSlugNameContainsSpecialChars() {
        permalinkHttpGetRouter.insert("/fake / slug",
            strategy.getHandler(getThemeRouteRules(), "fake-name"));
        createClient().get()
            .uri("/fake / slug")
            .exchange()
            .expectStatus().isOk();
    }

    @Test
    void shouldResponse200IfSlugNameContainsChineseChars() {
        permalinkHttpGetRouter.insert("/中文",
            strategy.getHandler(getThemeRouteRules(), "fake-name"));
        createClient().get()
            .uri("/中文")
            .exchange()
            .expectStatus().isOk();
    }

    WebTestClient createClient() {
        RouterFunction<ServerResponse> routeFunction = getRouterFunction();
        return getWebTestClient(routeFunction);
    }
}
