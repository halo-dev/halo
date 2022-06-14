package run.halo.app.plugin.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Metadata;
import run.halo.app.plugin.PluginApplicationContext;

/**
 * Tests for {@link ReverseProxyRouterFunctionFactory}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class ReverseProxyRouterFunctionFactoryTest {

    @Mock
    private ExtensionClient extensionClient;

    @Mock
    private PluginApplicationContext pluginApplicationContext;

    private ReverseProxyRouterFunctionFactory reverseProxyRouterFunctionFactory;

    @BeforeEach
    void setUp() {
        reverseProxyRouterFunctionFactory = new ReverseProxyRouterFunctionFactory(extensionClient);

        ReverseProxy reverseProxy = mockReverseProxy();

        when(pluginApplicationContext.getPluginId()).thenReturn("fakeA");
        when(extensionClient.list(eq(ReverseProxy.class), any(), any())).thenReturn(
            List.of(reverseProxy));
    }

    @Test
    void create() {
        RouterFunction<ServerResponse> routerFunction =
            reverseProxyRouterFunctionFactory.create(pluginApplicationContext);
        assertThat(routerFunction).isNotNull();
    }


    private ReverseProxy mockReverseProxy() {
        ReverseProxy.ReverseProxyRule reverseProxyRule =
            new ReverseProxy.ReverseProxyRule("/static/**",
                new ReverseProxy.FileReverseProxyProvider("static", ""));
        ReverseProxy reverseProxy = new ReverseProxy();
        Metadata metadata = new Metadata();
        metadata.setLabels(
            Map.of(ReverseProxyRouterFunctionFactory.REVERSE_PROXY_PLUGIN_LABEL_NAME, "fakeA"));
        reverseProxy.setMetadata(metadata);
        reverseProxy.setRules(List.of(reverseProxyRule));
        return reverseProxy;
    }
}
