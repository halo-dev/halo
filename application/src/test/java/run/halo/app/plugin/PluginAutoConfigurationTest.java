package run.halo.app.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.ResourceUtils;

@ExtendWith(MockitoExtension.class)
class PluginAutoConfigurationTest {

    @Mock
    PluginManager pluginManager;

    WebTestClient webClient;

    @BeforeEach
    void setUp() throws IOException {
        var webProperties = new WebProperties();
        webProperties.getResources().getCache().setUseLastModified(false);
        var routerFunction = new PluginAutoConfiguration().pluginJsBundleRoute(pluginManager, webProperties);
        webClient = WebTestClient.bindToRouterFunction(routerFunction).build();

        var pluginWrapper = mock(PluginWrapper.class);
        var pluginRoot = ResourceUtils.getURL("classpath:plugin/plugin-for-ui-assets/");
        var classLoader = new URLClassLoader(new URL[] {pluginRoot});
        lenient().when(pluginWrapper.getPluginClassLoader()).thenReturn(classLoader);
        lenient().when(pluginManager.getPlugin("fake-plugin")).thenReturn(pluginWrapper);
    }

    @Test
    void shouldServeUiAssets() {
        webClient
                .get()
                .uri("/plugins/fake-plugin/assets/ui/main.js")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("console.log(\"ui\");"));
    }

    @Test
    void shouldServeConsoleAssets() {
        webClient
                .get()
                .uri("/plugins/fake-plugin/assets/console/main.js")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("console.log(\"console\");"));
    }

    @Test
    void shouldUseExactAssetRouteLocation() {
        webClient
                .get()
                .uri("/plugins/fake-plugin/assets/ui/console-only.js")
                .exchange()
                .expectStatus()
                .isNotFound();

        webClient
                .get()
                .uri("/plugins/fake-plugin/assets/console/console-only.js")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("console.log(\"console-only\");"));
    }

    @Test
    void shouldNotServeAssetsFromParentClassLoader() throws IOException {
        var pluginWrapper = mock(PluginWrapper.class);
        var dependencyRoot = ResourceUtils.getURL("classpath:plugin/plugin-for-ui-assets/");
        var pluginRoot = ResourceUtils.getURL("classpath:plugin/plugin-for-console-assets/");
        var dependencyClassLoader = new URLClassLoader(new URL[] {dependencyRoot}, null);
        var classLoader = new URLClassLoader(new URL[] {pluginRoot}, dependencyClassLoader);
        lenient().when(pluginWrapper.getPluginClassLoader()).thenReturn(classLoader);
        lenient().when(pluginManager.getPlugin("fake-plugin")).thenReturn(pluginWrapper);

        webClient
                .get()
                .uri("/plugins/fake-plugin/assets/ui/main.js")
                .exchange()
                .expectStatus()
                .isNotFound();

        webClient
                .get()
                .uri("/plugins/fake-plugin/assets/console/main.js")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("console.log(\"console-only\");"));
    }

    @Test
    void shouldReturnNotFoundWhenPluginIsMissing() {
        webClient
                .get()
                .uri("/plugins/missing-plugin/assets/ui/main.js")
                .exchange()
                .expectStatus()
                .isNotFound();
    }
}
