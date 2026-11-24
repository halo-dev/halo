package run.halo.app.localization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;

class PublicApiLocalizationWebFilterTest {
    private final ExtensionGetter extensions = mock(ExtensionGetter.class);
    private final ResourceLocalizationProvider provider = mock(ResourceLocalizationProvider.class);
    private final LocalizationContext context =
            new LocalizationContext(Locale.CHINA, Locale.ENGLISH, List.of(), LocalizationScope.PUBLIC_API);
    private final PublicApiLocalizationWebFilter filter =
            new PublicApiLocalizationWebFilter(new PublicApiLocalizationResolver(extensions));

    private RouterFunction<ServerResponse> router;
    private WebTestClient client;

    @BeforeEach
    void setUp() {
        router = RouterFunctions.route()
                .GET("/apis/api.content.halo.run/v1alpha1/singlepages", PublicApiLocalizationWebFilterTest::respond)
                .GET(
                        "/apis/api.plugin.halo.run/v1alpha1/plugins/demo/items",
                        PublicApiLocalizationWebFilterTest::respond)
                .GET("/apis/api.console.halo.run/v1alpha1/plugins", PublicApiLocalizationWebFilterTest::respond)
                .GET("/apis/content.halo.run/v1alpha1/posts", PublicApiLocalizationWebFilterTest::respond)
                .GET("/apis/api.notification.halo.run/v1alpha1/userspaces", PublicApiLocalizationWebFilterTest::respond)
                .POST("/apis/api.halo.run/v1alpha1/comments", PublicApiLocalizationWebFilterTest::respond)
                .build();
        client = WebTestClient.bindToRouterFunction(router).webFilter(filter).build();
    }

    @Test
    void appliesLocaleToPublicApiLazyResponseBody() {
        when(extensions.getEnabledExtension(ResourceLocalizationProvider.class)).thenReturn(Mono.just(provider));
        when(provider.resolveContext(Locale.ENGLISH, LocalizationScope.PUBLIC_API))
                .thenReturn(Mono.just(context));

        client.get()
                .uri("/apis/api.content.halo.run/v1alpha1/singlepages?locale=en")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .isEqualTo("en");
    }

    @Test
    void appliesLocaleToPluginPublicApi() {
        when(extensions.getEnabledExtension(ResourceLocalizationProvider.class)).thenReturn(Mono.just(provider));
        when(provider.resolveContext(Locale.ENGLISH, LocalizationScope.PUBLIC_API))
                .thenReturn(Mono.just(context));

        client.get()
                .uri("/apis/api.plugin.halo.run/v1alpha1/plugins/demo/items?locale=en")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .isEqualTo("en");
    }

    @Test
    void ignoresConsoleAndInternalApiPaths() {
        client.get()
                .uri("/apis/api.console.halo.run/v1alpha1/plugins?locale=en")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .isEqualTo("source");
        client.get()
                .uri("/apis/content.halo.run/v1alpha1/posts?locale=en")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .isEqualTo("source");
        client.get()
                .uri("/apis/api.notification.halo.run/v1alpha1/userspaces?locale=en")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .isEqualTo("source");
        verifyNoInteractions(extensions);
    }

    @Test
    void ignoresWriteRequestsAndMissingOrMalformedLocale() {
        client.post()
                .uri("/apis/api.halo.run/v1alpha1/comments?locale=en")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .isEqualTo("source");
        client.get()
                .uri("/apis/api.content.halo.run/v1alpha1/singlepages")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .isEqualTo("source");
        client.get()
                .uri("/apis/api.content.halo.run/v1alpha1/singlepages?locale=../console")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .isEqualTo("source");
        verifyNoInteractions(extensions);
    }

    @Test
    void existingContextTakesPrecedenceOverQueryLocale() {
        var existing = new LocalizationContext(Locale.CHINA, Locale.JAPANESE, List.of(), LocalizationScope.PUBLIC_PAGE);
        var configured = WebTestClient.bindToRouterFunction(router)
                .webFilter((exchange, chain) ->
                        chain.filter(exchange).contextWrite(view -> view.put(LocalizationContext.class, existing)))
                .webFilter(filter)
                .build();

        configured
                .get()
                .uri("/apis/api.content.halo.run/v1alpha1/singlepages?locale=en")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .isEqualTo("ja");
        verifyNoInteractions(extensions);
    }

    @Test
    void resolverFailureRetainsSourceContent() {
        when(extensions.getEnabledExtension(ResourceLocalizationProvider.class))
                .thenReturn(Mono.error(new IllegalStateException("provider unavailable")));

        client.get()
                .uri("/apis/api.content.halo.run/v1alpha1/singlepages?locale=en")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .isEqualTo("source");
    }

    @Test
    void resolvesLocaleUnderApplicationContextPath() {
        when(extensions.getEnabledExtension(ResourceLocalizationProvider.class)).thenReturn(Mono.just(provider));
        when(provider.resolveContext(Locale.ENGLISH, LocalizationScope.PUBLIC_API))
                .thenReturn(Mono.just(context));
        var resolved = new AtomicBoolean();
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/context/apis/api.content.halo.run/v1alpha1/singlepages?locale=en")
                        .contextPath("/context")
                        .build());

        StepVerifier.create(filter.filter(
                        exchange,
                        current -> Mono.deferContextual(view -> {
                            resolved.set(view.hasKey(LocalizationContext.class));
                            return Mono.empty();
                        })))
                .verifyComplete();

        assertThat(resolved).isTrue();
    }

    private static Mono<ServerResponse> respond(ServerRequest request) {
        return ServerResponse.ok()
                .body(
                        LocalizationContext.current()
                                .map(current -> current.targetLocale().toLanguageTag())
                                .defaultIfEmpty("source"),
                        String.class);
    }
}
