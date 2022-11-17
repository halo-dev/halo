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
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.finders.TagFinder;

/**
 * Tests for {@link TagRouteStrategy}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class TagRouteStrategyTest extends RouterStrategyTestSuite {

    @Mock
    private PostFinder postFinder;
    @Mock
    private TagFinder tagFinder;

    @InjectMocks
    private TagRouteStrategy tagRouteStrategy;

    @Test
    void getRouteFunction() {
        RouterFunction<ServerResponse> routeFunction = getRouterFunction();
        WebTestClient client = getWebTestClient(routeFunction);

        permalinkHttpGetRouter.insert("/tags-test/fake-slug",
            tagRouteStrategy.getHandler(getThemeRouteRules(), "fake-name"));
        when(tagFinder.getByName(any())).thenReturn(Mono.empty());

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