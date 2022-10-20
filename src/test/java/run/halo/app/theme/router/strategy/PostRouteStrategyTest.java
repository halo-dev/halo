package run.halo.app.theme.router.strategy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.content.TestPost;
import run.halo.app.infra.SystemSetting;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.finders.vo.PostVo;

/**
 * Tests for {@link PostRouteStrategy}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class PostRouteStrategyTest extends RouterStrategyTestSuite {

    @Mock
    private PostFinder postFinder;

    @InjectMocks
    private PostRouteStrategy postRouteStrategy;

    @Override
    public void setUp() {
        lenient().when(postFinder.getByName(any())).thenReturn(PostVo.from(TestPost.postV1()));
    }

    @Test
    void getRouteFunctionWhenSlugPathVariable() {
        RouterFunction<ServerResponse> routeFunction = getRouterFunction();

        SystemSetting.ThemeRouteRules themeRouteRules = getThemeRouteRules();
        themeRouteRules.setPost("/posts-test/{slug}");
        permalinkHttpGetRouter.insert("/posts-test/fake-slug",
            postRouteStrategy.getHandler(themeRouteRules, "fake-slug"));

        WebTestClient client = getWebTestClient(routeFunction);

        client.get()
            .uri("/posts-test/fake-slug")
            .exchange()
            .expectStatus()
            .isOk();
    }

    @Test
    void getRouteFunctionWhenNamePathVariable() {
        RouterFunction<ServerResponse> routeFunction = getRouterFunction();

        SystemSetting.ThemeRouteRules themeRouteRules = getThemeRouteRules();
        themeRouteRules.setPost("/posts-test/{slug}");
        permalinkHttpGetRouter.insert("/posts-test/fake-name",
            postRouteStrategy.getHandler(themeRouteRules, "fake-name"));

        WebTestClient client = getWebTestClient(routeFunction);

        client.get()
            .uri("/posts-test/fake-name")
            .exchange()
            .expectStatus()
            .isOk();
    }

    @Test
    void getRouteFunctionWhenYearMonthSlugPathVariable() {
        RouterFunction<ServerResponse> routeFunction = getRouterFunction();

        SystemSetting.ThemeRouteRules themeRouteRules = getThemeRouteRules();
        themeRouteRules.setPost("/{year}/{month}/{slug}");
        permalinkHttpGetRouter.insert("/{year}/{month}/{slug}",
            postRouteStrategy.getHandler(themeRouteRules, "fake-name"));

        WebTestClient client = getWebTestClient(routeFunction);

        client.get()
            .uri("/2022/08/fake-slug")
            .exchange()
            .expectStatus()
            .isOk();
    }

    @Test
    void getRouteFunctionWhenQueryParam() {
        RouterFunction<ServerResponse> routeFunction = getRouterFunction();

        SystemSetting.ThemeRouteRules themeRouteRules = getThemeRouteRules();
        themeRouteRules.setPost("/?p={slug}");
        permalinkHttpGetRouter.insert("/?p=fake-name",
            postRouteStrategy.getHandler(themeRouteRules, "fake-name"));

        WebTestClient client = getWebTestClient(routeFunction);

        client.get()
            .uri(uriBuilder -> uriBuilder.path("/")
                .queryParam("p", "fake-name")
                .build()
            )
            .exchange()
            .expectStatus()
            .isOk();

        client.get()
            .uri(uriBuilder -> uriBuilder.path("/")
                .queryParam("p", "nothing")
                .build()
            )
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.NOT_FOUND);
    }
}