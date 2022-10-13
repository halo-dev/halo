package run.halo.app.plugin.resources;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.reactive.function.server.RouterFunction;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.ReverseProxy;
import run.halo.app.extension.Metadata;
import run.halo.app.plugin.ExtensionContextRegistry;
import run.halo.app.plugin.PluginApplicationContext;

/**
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class ReverseProxyRouterFunctionRegistryTest {

    @InjectMocks
    private ReverseProxyRouterFunctionRegistry registry;

    @MockBean
    private ReverseProxyRouterFunctionFactory reverseProxyRouterFunctionFactory;

    @BeforeEach
    void setUp() {
        ExtensionContextRegistry instance = ExtensionContextRegistry.getInstance();
        instance.register("fake-plugin", Mockito.mock(PluginApplicationContext.class));
    }

    @AfterEach
    void tearDown() {
        ExtensionContextRegistry.getInstance().remove("fake-plugin");
    }

    @Test
    void register() {
        ReverseProxy mock = Mockito.mock(ReverseProxy.class);
        Metadata metadata = new Metadata();
        metadata.setName("test-reverse-proxy");
        when(mock.getMetadata()).thenReturn(metadata);

        when(reverseProxyRouterFunctionFactory.create(any(), any()))
            .thenReturn(Mockito.mock(RouterFunction.class));
        registry.register("fake-plugin", mock)
            .as(StepVerifier::create)
            .verifyComplete();
    }
}