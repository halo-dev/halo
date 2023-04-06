package run.halo.app.plugin;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.function.server.support.RouterFunctionMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.plugin.resources.ReverseProxyRouterFunctionRegistry;

/**
 * Tests for {@link PluginCompositeRouterFunction}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class PluginCompositeRouterFunctionTest {
    ServerCodecConfigurer codecConfigurer = ServerCodecConfigurer.create();

    @Mock
    ReverseProxyRouterFunctionRegistry reverseProxyRouterFunctionRegistry;

    @Mock
    ObjectProvider<RouterFunction> rawRouterFunctionsProvider;

    @Mock
    ObjectProvider<CustomEndpoint> customEndpointsProvider;

    @InjectMocks
    PluginCompositeRouterFunction compositeRouterFunction;

    HandlerFunction<ServerResponse> handlerFunction;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        var fakeContext = mock(PluginApplicationContext.class);
        ExtensionContextRegistry.getInstance().register("fake-plugin", fakeContext);

        when(rawRouterFunctionsProvider.orderedStream()).thenReturn(Stream.empty());
        when(customEndpointsProvider.orderedStream()).thenReturn(Stream.empty());

        when(fakeContext.getBeanProvider(RouterFunction.class))
            .thenReturn(rawRouterFunctionsProvider);
        when(fakeContext.getBeanProvider(CustomEndpoint.class)).thenReturn(customEndpointsProvider);

        compositeRouterFunction =
            new PluginCompositeRouterFunction(reverseProxyRouterFunctionRegistry);

        handlerFunction = request -> ServerResponse.ok().build();
        RouterFunction<ServerResponse> routerFunction = request -> Mono.just(handlerFunction);

        when(reverseProxyRouterFunctionRegistry.getRouterFunctions())
            .thenReturn(List.of(routerFunction));
    }

    @AfterEach
    void cleanUp() {
        ExtensionContextRegistry.getInstance().remove("fake-plugin");
    }

    @Test
    void route() {
        RouterFunctionMapping mapping = new RouterFunctionMapping(compositeRouterFunction);
        mapping.setMessageReaders(this.codecConfigurer.getReaders());

        Mono<Object> result = mapping.getHandler(createExchange("https://example.com/match"));

        StepVerifier.create(result)
            .expectNext(handlerFunction)
            .expectComplete()
            .verify();

        verify(rawRouterFunctionsProvider).orderedStream();
        verify(customEndpointsProvider).orderedStream();
    }

    private ServerWebExchange createExchange(String urlTemplate) {
        return MockServerWebExchange.from(MockServerHttpRequest.get(urlTemplate));
    }

}