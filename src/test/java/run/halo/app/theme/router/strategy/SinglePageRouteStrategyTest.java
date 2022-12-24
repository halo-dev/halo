package run.halo.app.theme.router.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static run.halo.app.theme.DefaultTemplateEnum.SINGLE_PAGE;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.Metadata;
import run.halo.app.theme.finders.SinglePageFinder;
import run.halo.app.theme.finders.vo.SinglePageVo;
import run.halo.app.theme.router.ViewNameResolver;

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

    @Mock
    private ViewNameResolver viewNameResolver;

    @InjectMocks
    private SinglePageRouteStrategy strategy;


    @Override
    public void setUp() {
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
        when(singlePageFinder.getByName(any())).thenReturn(Mono.empty());
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

        when(singlePageFinder.getByName(any())).thenReturn(Mono.empty());
        createClient().get()
            .uri("/fake / slug")
            .exchange()
            .expectStatus().isOk();
    }

    @Test
    void shouldResponse200IfSlugNameContainsChineseChars() {
        permalinkHttpGetRouter.insert("/中文",
            strategy.getHandler(getThemeRouteRules(), "fake-name"));

        when(singlePageFinder.getByName(any())).thenReturn(Mono.empty());

        createClient().get()
            .uri("/中文")
            .exchange()
            .expectStatus().isOk();
    }

    @Test
    void ensureModel() {
        // fix gh-2912
        Metadata metadata = new Metadata();
        metadata.setName("fake-name");
        SinglePageVo singlePageVo = SinglePageVo.builder()
            .metadata(metadata)
            .spec(new SinglePage.SinglePageSpec())
            .build();

        when(singlePageFinder.getByName(eq("fake-name"))).thenReturn(Mono.just(singlePageVo));
        permalinkHttpGetRouter.insert("/fake-slug",
            strategy.getHandler(getThemeRouteRules(), "fake-name"));

        when(viewNameResolver.resolveViewNameOrDefault(any(), any(), any()))
            .thenReturn(Mono.just("page"));
        when(viewResolver.resolveViewName(eq(SINGLE_PAGE.getValue()), any()))
            .thenReturn(Mono.just(new EmptyView() {
                @Override
                public Mono<Void> render(Map<String, ?> model, MediaType contentType,
                    ServerWebExchange exchange) {
                    assertThat(model.get("name")).isEqualTo("fake-name");
                    assertThat(model.get("plural")).isEqualTo("singlepages");
                    assertThat(model.get("_templateId")).isEqualTo("page");
                    assertThat(model.get("singlePage")).isEqualTo(singlePageVo);
                    assertThat(model.get("groupVersionKind")).isEqualTo(
                        GroupVersionKind.fromExtension(SinglePage.class));
                    return Mono.empty();
                }
            }));
        createClient().get()
            .uri("/fake-slug")
            .exchange()
            .expectStatus().isOk()
            .expectBody();
    }

    WebTestClient createClient() {
        RouterFunction<ServerResponse> routeFunction = getRouterFunction();
        return getWebTestClient(routeFunction);
    }
}
