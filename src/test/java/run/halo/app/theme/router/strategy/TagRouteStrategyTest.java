package run.halo.app.theme.router.strategy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.extension.ListResult;
import run.halo.app.theme.finders.PostFinder;

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

    @InjectMocks
    private TagRouteStrategy tagRouteStrategy;

    @Override
    public void setUp() {
        lenient().when(postFinder.listByTag(anyInt(), anyInt(), any()))
            .thenReturn(new ListResult<>(1, 10, 0, List.of()));
    }

    @Test
    void getRouteFunction() {
        RouterFunction<ServerResponse> routeFunction = getRouterFunction();
        WebTestClient client = getWebTestClient(routeFunction);

        permalinkHttpGetRouter.insert("/tags-test/fake-slug",
            tagRouteStrategy.getHandler(getThemeRouteRules(), "fake-name"));

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