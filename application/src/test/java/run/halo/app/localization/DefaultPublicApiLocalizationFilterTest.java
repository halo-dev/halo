package run.halo.app.localization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;

class DefaultPublicApiLocalizationFilterTest {
    private final ExtensionGetter extensions = mock(ExtensionGetter.class);
    private final ResourceLocalizationProvider provider = mock(ResourceLocalizationProvider.class);
    private final DefaultPublicApiLocalizationFilter filter =
            new DefaultPublicApiLocalizationFilter(new PublicApiLocalizationResolver(extensions));
    private final LocalizationContext context =
            new LocalizationContext(Locale.CHINA, Locale.ENGLISH, List.of(), LocalizationScope.PUBLIC_API);

    @Test
    void explicitlyOptedInRouterSeesConfiguredContextOnlyDuringItsSubscription() {
        var request = MockServerRequest.builder().queryParam("locale", "en").build();
        when(extensions.getEnabledExtension(ResourceLocalizationProvider.class)).thenReturn(Mono.just(provider));
        when(provider.resolveContext(Locale.ENGLISH, LocalizationScope.PUBLIC_API))
                .thenReturn(Mono.just(context));
        filter.filter(
                        request,
                        ignored -> LocalizationContext.current()
                                .flatMap(current -> ServerResponse.ok()
                                        .header(
                                                "Test-Language",
                                                current.targetLocale().toLanguageTag())
                                        .build()))
                .as(StepVerifier::create)
                .assertNext(response ->
                        assertThat(response.headers().getFirst("Test-Language")).isEqualTo("en"))
                .verifyComplete();
        LocalizationContext.current().as(StepVerifier::create).verifyComplete();
    }

    @Test
    void lazyResponseBodyReceivesLanguageContextAndRetainsResponseMetadata() {
        when(extensions.getEnabledExtension(ResourceLocalizationProvider.class)).thenReturn(Mono.just(provider));
        when(provider.resolveContext(Locale.ENGLISH, LocalizationScope.PUBLIC_API))
                .thenReturn(Mono.just(context));
        var router = org.springframework.web.reactive.function.server.RouterFunctions.route()
                .GET(
                        "/products",
                        request -> ServerResponse.status(202)
                                .header("X-Source", "catalog")
                                .cookie(org.springframework.http.ResponseCookie.from("test", "preserved")
                                        .build())
                                .body(
                                        LocalizationContext.current()
                                                .map(value ->
                                                        value.targetLocale().toLanguageTag())
                                                .defaultIfEmpty("source"),
                                        String.class))
                .filter(filter)
                .build();
        var client = org.springframework.test.web.reactive.server.WebTestClient.bindToRouterFunction(router)
                .build();
        client.get()
                .uri("/products?locale=en")
                .exchange()
                .expectStatus()
                .isAccepted()
                .expectHeader()
                .valueEquals("X-Source", "catalog")
                .expectCookie()
                .valueEquals("test", "preserved")
                .expectBody(String.class)
                .isEqualTo("en");
        client.get()
                .uri("/products")
                .exchange()
                .expectStatus()
                .isAccepted()
                .expectBody(String.class)
                .isEqualTo("source");
    }

    @Test
    void lazyBodyRetainsAnExistingInternalContextRatherThanEnablingTranslation() {
        var internal = new LocalizationContext(Locale.CHINA, Locale.JAPANESE, List.of(), LocalizationScope.INTERNAL);
        var router = org.springframework.web.reactive.function.server.RouterFunctions.route()
                .GET(
                        "/products",
                        request -> ServerResponse.ok()
                                .body(
                                        LocalizationContext.current()
                                                .map(value -> value.scope().name())
                                                .defaultIfEmpty("missing"),
                                        String.class))
                .filter(filter)
                .build();
        var client = org.springframework.test.web.reactive.server.WebTestClient.bindToRouterFunction(router)
                .webFilter((exchange, chain) ->
                        chain.filter(exchange).contextWrite(view -> view.put(LocalizationContext.class, internal)))
                .build();
        client.get()
                .uri("/products?locale=en")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .isEqualTo("INTERNAL");
        verifyNoInteractions(extensions);
    }

    @Test
    void absentOrMalformedLanguageRetainsSourceWithoutCallingProvider() {
        for (var request : List.of(
                MockServerRequest.builder().build(),
                MockServerRequest.builder().queryParam("locale", "../console").build())) {
            filter.filter(
                            request,
                            ignored -> LocalizationContext.current()
                                    .flatMap(current ->
                                            ServerResponse.status(500).build())
                                    .switchIfEmpty(ServerResponse.ok().build()))
                    .as(StepVerifier::create)
                    .assertNext(response ->
                            assertThat(response.statusCode().value()).isEqualTo(200))
                    .verifyComplete();
        }
        verifyNoInteractions(extensions);
    }

    @Test
    void internalContextCannotBeOverriddenByQueryLanguage() {
        var internal = new LocalizationContext(Locale.CHINA, Locale.ENGLISH, List.of(), LocalizationScope.INTERNAL);
        var request = MockServerRequest.builder().queryParam("locale", "en").build();
        filter.filter(
                        request,
                        ignored -> LocalizationContext.current().flatMap(current -> {
                            assertThat(current).isEqualTo(internal);
                            return ServerResponse.ok().build();
                        }))
                .contextWrite(view -> view.put(LocalizationContext.class, internal))
                .as(StepVerifier::create)
                .expectNextCount(1)
                .verifyComplete();
        verifyNoInteractions(extensions);
    }

    @Test
    void resolverFailureDoesNotReplayHandlerOrHideItsError() {
        var request = MockServerRequest.builder().queryParam("locale", "en").build();
        var calls = new AtomicInteger();
        var handlerError = new IllegalStateException("domain failure");
        when(extensions.getEnabledExtension(ResourceLocalizationProvider.class))
                .thenReturn(Mono.error(new IllegalStateException()));
        filter.filter(request, ignored -> {
                    calls.incrementAndGet();
                    return Mono.error(handlerError);
                })
                .as(StepVerifier::create)
                .expectErrorMatches(error -> error == handlerError)
                .verify();
        assertThat(calls).hasValue(1);
    }
}
