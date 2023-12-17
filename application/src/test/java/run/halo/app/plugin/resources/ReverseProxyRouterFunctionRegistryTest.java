package run.halo.app.plugin.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.ReverseProxy;
import run.halo.app.extension.Metadata;
import run.halo.app.plugin.ExtensionContextRegistry;
import run.halo.app.plugin.PluginApplicationContext;

/**
 * Tests for {@link ReverseProxyRouterFunctionRegistry}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class ReverseProxyRouterFunctionRegistryTest {

    @InjectMocks
    private ReverseProxyRouterFunctionRegistry registry;

    @Mock
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
        ReverseProxy mock = getMockReverseProxy();
        registry.register("fake-plugin", mock)
            .as(StepVerifier::create)
            .verifyComplete();

        assertThat(registry.reverseProxySize("fake-plugin")).isEqualTo(1);

        // repeat register a same reverse proxy
        registry.register("fake-plugin", mock)
            .as(StepVerifier::create)
            .verifyComplete();

        assertThat(registry.reverseProxySize("fake-plugin")).isEqualTo(1);

        verify(reverseProxyRouterFunctionFactory, times(2)).create(any(), any());
    }

    @Test
    void remove() {
        ReverseProxy mock = getMockReverseProxy();
        registry.register("fake-plugin", mock)
            .as(StepVerifier::create)
            .verifyComplete();

        registry.remove("fake-plugin").block();

        assertThat(registry.reverseProxySize("fake-plugin")).isEqualTo(0);
    }

    @Test
    void removeByKeyValue() {
        ReverseProxy mock = getMockReverseProxy();
        registry.register("fake-plugin", mock)
            .as(StepVerifier::create)
            .verifyComplete();

        registry.remove("fake-plugin", "test-reverse-proxy").block();

        assertThat(registry.reverseProxySize("fake-plugin")).isEqualTo(0);
    }

    private ReverseProxy getMockReverseProxy() {
        ReverseProxy mock = Mockito.mock(ReverseProxy.class);
        Metadata metadata = new Metadata();
        metadata.setName("test-reverse-proxy");
        when(mock.getMetadata()).thenReturn(metadata);
        RouterFunction<ServerResponse> routerFunction = request -> Mono.empty();

        when(reverseProxyRouterFunctionFactory.create(any(), any()))
            .thenReturn(Mono.just(routerFunction));
        return mock;
    }
}