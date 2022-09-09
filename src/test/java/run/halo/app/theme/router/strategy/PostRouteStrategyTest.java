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
import run.halo.app.content.TestPost;
import run.halo.app.content.permalinks.ExtensionLocator;
import run.halo.app.core.extension.Post;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.finders.vo.PostVo;
import run.halo.app.theme.router.PermalinkIndexer;

/**
 * Tests for {@link PostRouteStrategy}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class PostRouteStrategyTest {

    @Mock
    private ViewResolver viewResolver;

    @Mock
    private PermalinkIndexer permalinkIndexer;

    @Mock
    private PostFinder postFinder;

    @InjectMocks
    private PostRouteStrategy postRouteStrategy;

    @BeforeEach
    void setUp() {
        lenient().when(postFinder.getByName(any())).thenReturn(PostVo.from(TestPost.postV1()));
    }

    @Test
    void getRouteFunctionWhenSlugPathVariable() {
        RouterFunction<ServerResponse> routeFunction =
            postRouteStrategy.getRouteFunction(DefaultTemplateEnum.POST.getValue(),
                "/posts-test/{slug}");

        WebTestClient client = getWebTestClient(routeFunction);

        piling();

        when(permalinkIndexer.getPermalinks(any()))
            .thenReturn(List.of("/posts-test/fake-slug"));

        client.get()
            .uri("/posts-test/fake-slug")
            .exchange()
            .expectStatus()
            .isOk();
    }

    @Test
    void getRouteFunctionWhenNamePathVariable() {
        RouterFunction<ServerResponse> routeFunction =
            postRouteStrategy.getRouteFunction(DefaultTemplateEnum.POST.getValue(),
                "/posts-test/{name}");

        WebTestClient client = getWebTestClient(routeFunction);

        piling();

        when(permalinkIndexer.getPermalinks(any()))
            .thenReturn(List.of("/posts-test/fake-name"));

        client.get()
            .uri("/posts-test/fake-name")
            .exchange()
            .expectStatus()
            .isOk();
    }

    @Test
    void getRouteFunctionWhenYearMonthSlugPathVariable() {
        RouterFunction<ServerResponse> routeFunction =
            postRouteStrategy.getRouteFunction(DefaultTemplateEnum.POST.getValue(),
                "/{year}/{month}/{slug}");

        WebTestClient client = getWebTestClient(routeFunction);

        piling();

        when(permalinkIndexer.getPermalinks(any()))
            .thenReturn(List.of("/2022/08/fake-slug"));

        client.get()
            .uri("/2022/08/fake-slug")
            .exchange()
            .expectStatus()
            .isOk();
    }

    @Test
    void getRouteFunctionWhenQueryParam() {
        RouterFunction<ServerResponse> routeFunction =
            postRouteStrategy.getRouteFunction(DefaultTemplateEnum.POST.getValue(),
                "/?pp={name}");

        WebTestClient client = getWebTestClient(routeFunction);

        piling();

        client.get()
            .uri(uriBuilder -> uriBuilder.path("/")
                .queryParam("pp", "fake-name")
                .build()
            )
            .exchange()
            .expectStatus()
            .isOk();

        client.get()
            .uri(uriBuilder -> uriBuilder.path("/")
                .queryParam("pp", "nothing")
                .build()
            )
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    private void piling() {
        lenient().when(permalinkIndexer.getNames(any()))
            .thenReturn(List.of("fake-name"));

        lenient().when(permalinkIndexer.getSlugs(any()))
            .thenReturn(List.of("fake-slug"));

        lenient().when(permalinkIndexer.getSlugs(any()))
            .thenReturn(List.of("fake-slug"));

        lenient().when(permalinkIndexer.getNameBySlug(any(), eq("fake-slug")))
            .thenReturn("fake-name");

        ExtensionLocator extensionLocator =
            new ExtensionLocator(GroupVersionKind.fromExtension(Post.class), "fake-name",
                "fake-slug");
        lenient().when(permalinkIndexer.lookup(any()))
            .thenReturn(extensionLocator);

    }

    private WebTestClient getWebTestClient(RouterFunction<ServerResponse> routeFunction) {
        WebTestClient client = WebTestClient.bindToRouterFunction(routeFunction)
            .handlerStrategies(HandlerStrategies.builder()
                .viewResolver(viewResolver)
                .build())
            .build();

        when(viewResolver.resolveViewName(eq(DefaultTemplateEnum.POST.getValue()), any()))
            .thenReturn(Mono.just(new EmptyView()));
        return client;
    }
}