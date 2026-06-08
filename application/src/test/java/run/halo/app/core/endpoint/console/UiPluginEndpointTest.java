package run.halo.app.core.endpoint.console;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.UiPluginBundleService;

@ExtendWith(MockitoExtension.class)
class UiPluginEndpointTest {

    @Mock
    UiPluginBundleService uiPluginBundleService;

    @Spy
    WebProperties webProperties = new WebProperties();

    UiPluginEndpoint endpoint;

    WebTestClient webClient;

    private long lastModified;

    @BeforeEach
    void setUp() {
        endpoint = new UiPluginEndpoint(uiPluginBundleService, webProperties);
        webClient = WebTestClient.bindToRouterFunction(endpoint.endpoint()).build();
        long currentTimeMillis = System.currentTimeMillis();
        this.lastModified = currentTimeMillis - currentTimeMillis % 1_000;
    }

    @Test
    void shouldRedirectToVersionedJsBundleUrl() {
        when(uiPluginBundleService.generateBundleVersion()).thenReturn(Mono.just("fake-version"));

        webClient
                .get()
                .uri("/ui-plugins/-/bundle.js")
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader()
                .cacheControl(CacheControl.noStore())
                .expectHeader()
                .location("/apis/api.console.halo.run/v1alpha1/ui-plugins/-/bundle.js?v=fake-version");
    }

    @Test
    void shouldRedirectToVersionedCssBundleUrl() {
        when(uiPluginBundleService.generateBundleVersion()).thenReturn(Mono.just("fake-version"));

        webClient
                .get()
                .uri("/ui-plugins/-/bundle.css")
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .expectHeader()
                .cacheControl(CacheControl.noStore())
                .expectHeader()
                .location("/apis/api.console.halo.run/v1alpha1/ui-plugins/-/bundle.css?v=fake-version");
    }

    @Test
    void shouldFetchJsBundleWithCacheControl() {
        var cache = webProperties.getResources().getCache();
        cache.setUseLastModified(true);
        cache.getCachecontrol().setNoStore(true);
        endpoint.afterPropertiesSet();

        when(uiPluginBundleService.getJsBundle("fake-version"))
                .thenReturn(Mono.fromSupplier(() -> mockResource("fake-js")));

        webClient
                .get()
                .uri("/ui-plugins/-/bundle.js?v=fake-version")
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .cacheControl(CacheControl.noStore())
                .expectHeader()
                .contentType("text/javascript")
                .expectHeader()
                .lastModified(lastModified)
                .expectBody(String.class)
                .isEqualTo("fake-js");
    }

    @Test
    void shouldFetchCssBundle() {
        when(uiPluginBundleService.getCssBundle("fake-version"))
                .thenReturn(Mono.fromSupplier(() -> mockResource("fake-css")));

        webClient
                .get()
                .uri("/ui-plugins/-/bundle.css?v=fake-version")
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .contentType("text/css")
                .expectBody(String.class)
                .isEqualTo("fake-css");
    }

    Resource mockResource(String content) {
        var resource = new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8));
        resource = spy(resource);
        try {
            doReturn(Instant.ofEpochMilli(lastModified).toEpochMilli())
                    .when(resource)
                    .lastModified();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return resource;
    }
}
