package run.halo.app.plugin.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pf4j.PluginClassLoader;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.core.io.Resource;
import run.halo.app.infra.exception.AccessDeniedException;

/**
 * Tests for {@link BundleResourceUtils}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class BundleResourceUtilsTest {

    @Mock
    private PluginManager pluginManager;

    private PluginClassLoader pluginClassLoader;

    @BeforeEach
    void setUp() throws MalformedURLException {
        PluginWrapper pluginWrapper = Mockito.mock(PluginWrapper.class);
        pluginClassLoader = Mockito.mock(PluginClassLoader.class);
        lenient().when(pluginWrapper.getPluginClassLoader()).thenReturn(pluginClassLoader);
        lenient().when(pluginManager.getPlugin(eq("fake-plugin"))).thenReturn(pluginWrapper);

        lenient()
                .when(pluginClassLoader.getResource(eq("console/main.js")))
                .thenReturn(new URL("file://console/main.js"));
        lenient()
                .when(pluginClassLoader.getResource(eq("console/style.css")))
                .thenReturn(new URL("file://console/style.css"));
    }

    @Test
    void getSelectedBundleResource() {
        Resource jsBundleResource =
                BundleResourceUtils.getSelectedBundleResource(pluginManager, "fake-plugin", "main.js");
        assertThat(jsBundleResource).isNotNull();
        assertThat(jsBundleResource.exists()).isTrue();

        jsBundleResource = BundleResourceUtils.getSelectedBundleResource(pluginManager, "fake-plugin", "test.js");
        assertThat(jsBundleResource).isNull();

        jsBundleResource = BundleResourceUtils.getSelectedBundleResource(pluginManager, "nothing-plugin", "main.js");
        assertThat(jsBundleResource).isNull();

        assertThatThrownBy(() -> {
                    BundleResourceUtils.getSelectedBundleResource(pluginManager, "fake-plugin", "../test/main.js");
                })
                .isInstanceOf(AccessDeniedException.class);
    }

    @SuppressWarnings("deprecation")
    @Test
    void getJsBundleResourceShouldDelegateToSelectedBundleResource() throws IOException {
        lenient().when(pluginClassLoader.getResource(eq("ui/main.js"))).thenReturn(new URL("file://ui/main.js"));

        Resource jsBundleResource = BundleResourceUtils.getJsBundleResource(pluginManager, "fake-plugin", "main.js");

        assertThat(jsBundleResource).isNotNull();
        assertThat(jsBundleResource.getURL().toString()).isEqualTo("file://ui/main.js");
    }

    @Test
    void shouldPreferUiBundleLocation() throws IOException {
        lenient().when(pluginClassLoader.getResource(eq("ui/main.js"))).thenReturn(new URL("file://ui/main.js"));

        assertThat(BundleResourceUtils.selectBundleLocation(pluginManager, "fake-plugin"))
                .isEqualTo(BundleResourceUtils.UI_BUNDLE_LOCATION);

        Resource jsBundleResource =
                BundleResourceUtils.getSelectedBundleResource(pluginManager, "fake-plugin", "main.js");
        assertThat(jsBundleResource).isNotNull();
        assertThat(jsBundleResource.getURL().toString()).isEqualTo("file://ui/main.js");
    }

    @Test
    void shouldSkipConsoleWhenUiLocationIsSelected() throws IOException {
        lenient().when(pluginClassLoader.getResource(eq("ui/style.css"))).thenReturn(new URL("file://ui/style.css"));

        Resource jsBundleResource =
                BundleResourceUtils.getSelectedBundleResource(pluginManager, "fake-plugin", "main.js");
        assertThat(jsBundleResource).isNull();

        Resource cssBundleResource =
                BundleResourceUtils.getSelectedBundleResource(pluginManager, "fake-plugin", "style.css");
        assertThat(cssBundleResource).isNotNull();
        assertThat(cssBundleResource.getURL().toString()).isEqualTo("file://ui/style.css");
    }

    @Test
    void getBundleResourceFromSpecifiedLocation() throws IOException {
        lenient().when(pluginClassLoader.getResource(eq("ui/main.js"))).thenReturn(new URL("file://ui/main.js"));

        Resource uiBundleResource = BundleResourceUtils.getBundleResource(
                pluginManager, "fake-plugin", BundleResourceUtils.UI_BUNDLE_LOCATION, "main.js");
        assertThat(uiBundleResource).isNotNull();
        assertThat(uiBundleResource.getURL().toString()).isEqualTo("file://ui/main.js");

        Resource consoleBundleResource = BundleResourceUtils.getBundleResource(
                pluginManager, "fake-plugin", BundleResourceUtils.CONSOLE_BUNDLE_LOCATION, "main.js");
        assertThat(consoleBundleResource).isNotNull();
        assertThat(consoleBundleResource.getURL().toString()).isEqualTo("file://console/main.js");
    }

    @Test
    void shouldRejectUnsupportedBundleLocation() {
        assertThatThrownBy(
                        () -> BundleResourceUtils.getBundleResource(pluginManager, "fake-plugin", "admin", "main.js"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported bundle location: admin");
    }
}
