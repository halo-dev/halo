package run.halo.app.plugin.resources;

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
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.ReverseProxy;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.plugin.HaloPluginManager;
import run.halo.app.plugin.PluginApplicationContext;
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
    private ReactiveExtensionClient extensionClient;

    @Mock
    private PluginApplicationContext pluginApplicationContext;
    @Mock
    private HaloPluginManager haloPluginManager;

    private ReverseProxyRouterFunctionFactory reverseProxyRouterFunctionFactory;

    @BeforeEach
    void setUp() {
        JsBundleRuleProvider jsBundleRuleProvider = new JsBundleRuleProvider(haloPluginManager);
        reverseProxyRouterFunctionFactory = new ReverseProxyRouterFunctionFactory(extensionClient,
            jsBundleRuleProvider);

        ReverseProxy reverseProxy = mockReverseProxy();

        when(pluginApplicationContext.getPluginId()).thenReturn("fakeA");
        when(extensionClient.list(eq(ReverseProxy.class), any(), any())).thenReturn(
            Flux.just(reverseProxy));
    }

    @Test
    void create() {
        var routerFunction = reverseProxyRouterFunctionFactory.create(pluginApplicationContext);
        StepVerifier.create(routerFunction)
            .expectNextCount(1)
            .verifyComplete();
    }


    private ReverseProxy mockReverseProxy() {
        ReverseProxy.ReverseProxyRule reverseProxyRule =
            new ReverseProxy.ReverseProxyRule("/static/**",
                new ReverseProxy.FileReverseProxyProvider("static", ""));
        ReverseProxy reverseProxy = new ReverseProxy();
        Metadata metadata = new Metadata();
        metadata.setLabels(
            Map.of(PluginConst.PLUGIN_NAME_LABEL_NAME, "fakeA"));
        reverseProxy.setMetadata(metadata);
        reverseProxy.setRules(List.of(reverseProxyRule));
        return reverseProxy;
    }
}
