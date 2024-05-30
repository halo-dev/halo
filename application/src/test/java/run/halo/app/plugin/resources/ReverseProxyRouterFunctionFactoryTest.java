package run.halo.app.plugin.resources;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.ResourceUtils;
import run.halo.app.core.extension.ReverseProxy;
import run.halo.app.core.extension.ReverseProxy.FileReverseProxyProvider;
import run.halo.app.core.extension.ReverseProxy.ReverseProxyRule;
import run.halo.app.extension.Metadata;
import run.halo.app.plugin.PluginConst;

/**
 * Tests for {@link ReverseProxyRouterFunctionFactory}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class ReverseProxyRouterFunctionFactoryTest {

    @Mock
    private PluginManager pluginManager;

    @Mock
    private ApplicationContext applicationContext;

    @Spy
    WebProperties webProperties = new WebProperties();

    @InjectMocks
    private ReverseProxyRouterFunctionFactory factory;

    @Test
    void shouldProxyStaticResourceWithCacheControl() throws FileNotFoundException {
        var cache = webProperties.getResources().getCache();
        cache.setUseLastModified(true);
        cache.getCachecontrol().setMaxAge(Duration.ofDays(7));

        var routerFunction = factory.create(mockReverseProxy(), "fakeA");
        assertNotNull(routerFunction);
        var webClient = WebTestClient.bindToRouterFunction(routerFunction).build();

        var pluginWrapper = Mockito.mock(PluginWrapper.class);
        var pluginRoot = ResourceUtils.getURL("classpath:plugin/plugin-for-reverseproxy/");
        var classLoader = new URLClassLoader(new URL[] {pluginRoot});
        when(pluginWrapper.getPluginClassLoader()).thenReturn(classLoader);
        when(pluginManager.getPlugin("fakeA")).thenReturn(pluginWrapper);

        webClient.get().uri("/plugins/fakeA/assets/static/test.txt")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().cacheControl(CacheControl.maxAge(Duration.ofDays(7)))
            .expectHeader().value(HttpHeaders.LAST_MODIFIED, Assertions::assertNotNull)
            .expectBody(String.class).isEqualTo("Fake content.");
    }

    @Test
    void shouldProxyStaticResourceWithoutLastModified() throws FileNotFoundException {
        var cache = webProperties.getResources().getCache();
        cache.setUseLastModified(false);
        cache.getCachecontrol().setMaxAge(Duration.ofDays(7));

        var routerFunction = factory.create(mockReverseProxy(), "fakeA");
        assertNotNull(routerFunction);
        var webClient = WebTestClient.bindToRouterFunction(routerFunction).build();

        var pluginWrapper = Mockito.mock(PluginWrapper.class);
        var pluginRoot = ResourceUtils.getURL("classpath:plugin/plugin-for-reverseproxy/");
        var classLoader = new URLClassLoader(new URL[] {pluginRoot});
        when(pluginWrapper.getPluginClassLoader()).thenReturn(classLoader);
        when(pluginManager.getPlugin("fakeA")).thenReturn(pluginWrapper);

        webClient.get().uri("/plugins/fakeA/assets/static/test.txt")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().cacheControl(CacheControl.maxAge(Duration.ofDays(7)))
            .expectHeader().lastModified(-1)
            .expectBody(String.class).isEqualTo("Fake content.");
    }

    @Test
    void shouldReturnNotFoundIfResourceNotFound() throws FileNotFoundException {
        var routerFunction = factory.create(mockReverseProxy(), "fakeA");
        assertNotNull(routerFunction);
        var webClient = WebTestClient.bindToRouterFunction(routerFunction).build();

        var pluginWrapper = Mockito.mock(PluginWrapper.class);
        var pluginRoot = ResourceUtils.getURL("classpath:plugin/plugin-for-reverseproxy/");
        var classLoader = new URLClassLoader(new URL[] {pluginRoot});
        when(pluginWrapper.getPluginClassLoader()).thenReturn(classLoader);
        when(pluginManager.getPlugin("fakeA")).thenReturn(pluginWrapper);

        webClient.get().uri("/plugins/fakeA/assets/static/non-existing-file.txt")
            .exchange()
            .expectHeader().cacheControl(CacheControl.empty())
            .expectStatus().isNotFound();
    }

    private ReverseProxy mockReverseProxy() {
        var reverseProxyRule = new ReverseProxyRule("/static/**",
            new FileReverseProxyProvider("static", ""));
        var reverseProxy = new ReverseProxy();
        var metadata = new Metadata();
        metadata.setLabels(
            Map.of(PluginConst.PLUGIN_NAME_LABEL_NAME, "fakeA"));
        reverseProxy.setMetadata(metadata);
        reverseProxy.setRules(List.of(reverseProxyRule));
        return reverseProxy;
    }
}
