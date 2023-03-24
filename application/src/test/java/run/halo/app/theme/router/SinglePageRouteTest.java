package run.halo.app.theme.router;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.Metadata;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.SinglePageFinder;
import run.halo.app.theme.finders.vo.SinglePageVo;
import run.halo.app.theme.router.factories.ModelConst;

/**
 * Tests for {@link SinglePageRoute}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class SinglePageRouteTest {

    @Mock
    private ViewNameResolver viewNameResolver;

    @Mock
    private SinglePageFinder singlePageFinder;

    @Mock
    protected ViewResolver viewResolver;

    @InjectMocks
    private SinglePageRoute singlePageRoute;

    @Test
    void handlerFunction() {
        // fix gh-3448
        when(viewNameResolver.resolveViewNameOrDefault(any(), any(), any()))
            .thenReturn(Mono.just(DefaultTemplateEnum.POST.getValue()));

        String pageName = "fake-page";
        when(viewResolver.resolveViewName(any(), any()))
            .thenReturn(Mono.just(new EmptyView() {
                @Override
                public Mono<Void> render(Map<String, ?> model, MediaType contentType,
                    ServerWebExchange exchange) {
                    assertThat(model).containsKey(ModelConst.TEMPLATE_ID);
                    assertThat(model.get(ModelConst.TEMPLATE_ID))
                        .isEqualTo(DefaultTemplateEnum.SINGLE_PAGE.getValue());
                    assertThat(model.get("name"))
                        .isEqualTo(pageName);
                    assertThat(model.get("plural")).isEqualTo("singlepages");
                    assertThat(model.get("singlePage")).isNotNull();
                    assertThat(model.get("groupVersionKind"))
                        .isEqualTo(GroupVersionKind.fromExtension(SinglePage.class));
                    return super.render(model, contentType, exchange);
                }
            }));

        SinglePage singlePage = new SinglePage();
        singlePage.setMetadata(new Metadata());
        singlePage.getMetadata().setName(pageName);
        singlePage.setSpec(new SinglePage.SinglePageSpec());
        when(singlePageFinder.getByName(eq(pageName)))
            .thenReturn(Mono.just(SinglePageVo.from(singlePage)));

        HandlerFunction<ServerResponse> handlerFunction =
            singlePageRoute.handlerFunction(pageName);
        RouterFunction<ServerResponse> routerFunction =
            RouterFunctions.route().GET("/archives/{name}", handlerFunction).build();

        WebTestClient webTestClient = WebTestClient.bindToRouterFunction(routerFunction)
            .handlerStrategies(HandlerStrategies.builder()
                .viewResolver(viewResolver)
                .build())
            .build();

        webTestClient.get()
            .uri("/archives/fake-name")
            .exchange()
            .expectStatus().isOk();
    }
}
