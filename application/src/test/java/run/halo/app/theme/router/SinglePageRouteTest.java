package run.halo.app.theme.router;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.i18n.LocaleContextResolver;
import org.springframework.web.util.UriUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.SinglePageFinder;
import run.halo.app.theme.finders.vo.SinglePageVo;
import run.halo.app.theme.router.SinglePageRoute.NameSlugPair;

/**
 * Tests for {@link SinglePageRoute}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class SinglePageRouteTest {

    @Mock
    ViewNameResolver viewNameResolver;

    @Mock
    SinglePageFinder singlePageFinder;

    @Mock
    ViewResolver viewResolver;

    @Mock
    ExtensionClient client;

    @Mock
    LocaleContextResolver localeContextResolver;

    @Mock
    TitleVisibilityIdentifyCalculator titleVisibilityIdentifyCalculator;

    @InjectMocks
    SinglePageRoute singlePageRoute;

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

        when(localeContextResolver.resolveLocaleContext(any()))
            .thenReturn(new SimpleLocaleContext(Locale.getDefault()));
        webTestClient.get()
            .uri("/archives/fake-name")
            .exchange()
            .expectStatus().isOk();
    }

    @Test
    void shouldNotThrowErrorIfSlugNameContainsSpecialChars() {
        var specialChars = "/with-special-chars-{}-[]-{{}}-{[]}-[{}]";
        var specialCharsUri =
            URI.create(UriUtils.encodePath(specialChars, UTF_8));
        var mockHttpRequest = MockServerHttpRequest.get(specialCharsUri.toString())
            .accept(MediaType.TEXT_HTML)
            .build();
        var mockExchange = MockServerWebExchange.from(mockHttpRequest);
        var request = MockServerRequest.builder()
            .exchange(mockExchange)
            .uri(specialCharsUri)
            .method(HttpMethod.GET)
            .header(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE)
            .build();
        var nameSlugPair = new NameSlugPair("fake-single-page", specialChars);
        singlePageRoute.setQuickRouteMap(Map.of(nameSlugPair, r -> ServerResponse.ok().build()));
        StepVerifier.create(singlePageRoute.route(request))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Nested
    class SinglePageReconcilerTest {

        @Test
        void shouldRemoveRouteIfSinglePageUnpublished() {
            var name = "fake-single-page";
            var page = newSinglePage(name, false);
            when(client.fetch(SinglePage.class, name)).thenReturn(
                Optional.of(page));

            var routeMap = Mockito.<Map<NameSlugPair, HandlerFunction<ServerResponse>>>mock(
                invocation -> new HashMap<NameSlugPair, HandlerFunction<ServerResponse>>());
            singlePageRoute.setQuickRouteMap(routeMap);
            var result = singlePageRoute.reconcile(new Reconciler.Request(name));
            assertNotNull(result);
            assertFalse(result.reEnqueue());
            verify(client).fetch(SinglePage.class, name);
            verify(routeMap).remove(NameSlugPair.from(page));
        }

        @Test
        void shouldAddRouteIfSinglePagePublished() {
            var name = "fake-single-page";
            var page = newSinglePage(name, true);
            when(client.fetch(SinglePage.class, name)).thenReturn(
                Optional.of(page));

            var routeMap = Mockito.<Map<NameSlugPair, HandlerFunction<ServerResponse>>>mock(
                invocation -> new HashMap<NameSlugPair, HandlerFunction<ServerResponse>>());
            singlePageRoute.setQuickRouteMap(routeMap);
            var result = singlePageRoute.reconcile(new Reconciler.Request(name));
            assertNotNull(result);
            assertFalse(result.reEnqueue());
            verify(client).fetch(SinglePage.class, name);
            verify(routeMap).put(eq(NameSlugPair.from(page)), any());
        }

        @Test
        void shouldRemoveRouteIfSinglePageDeleted() {
            var name = "fake-single-page";
            var page = newDeletedSinglePage(name);
            when(client.fetch(SinglePage.class, name)).thenReturn(
                Optional.of(page));

            var routeMap = Mockito.<Map<NameSlugPair, HandlerFunction<ServerResponse>>>mock(
                invocation -> new HashMap<NameSlugPair, HandlerFunction<ServerResponse>>());
            singlePageRoute.setQuickRouteMap(routeMap);
            var result = singlePageRoute.reconcile(new Reconciler.Request(name));
            assertNotNull(result);
            assertFalse(result.reEnqueue());
            verify(client).fetch(SinglePage.class, name);
            verify(routeMap).remove(NameSlugPair.from(page));
        }

        @Test
        void shouldRemoveRouteIfSinglePageRecycled() {
            var name = "fake-single-page";
            var page = newRecycledSinglePage(name);
            when(client.fetch(SinglePage.class, name)).thenReturn(
                Optional.of(page));

            var routeMap = Mockito.<Map<NameSlugPair, HandlerFunction<ServerResponse>>>mock(
                invocation -> new HashMap<NameSlugPair, HandlerFunction<ServerResponse>>());
            singlePageRoute.setQuickRouteMap(routeMap);
            var result = singlePageRoute.reconcile(new Reconciler.Request(name));
            assertNotNull(result);
            assertFalse(result.reEnqueue());
            verify(client).fetch(SinglePage.class, name);
            verify(routeMap).remove(NameSlugPair.from(page));
        }


        SinglePage newSinglePage(String name, boolean published) {
            var metadata = new Metadata();
            metadata.setName(name);
            var page = new SinglePage();
            page.setMetadata(metadata);
            var spec = new SinglePage.SinglePageSpec();
            spec.setSlug("/fake-slug");
            page.setSpec(spec);
            var status = new SinglePage.SinglePageStatus();
            page.setStatus(status);
            SinglePage.changePublishedState(page, published);
            return page;
        }

        SinglePage newDeletedSinglePage(String name) {
            var page = newSinglePage(name, true);
            page.getMetadata().setDeletionTimestamp(Instant.now());
            return page;
        }

        SinglePage newRecycledSinglePage(String name) {
            var page = newSinglePage(name, true);
            page.getSpec().setDeleted(true);
            return page;
        }
    }
}
