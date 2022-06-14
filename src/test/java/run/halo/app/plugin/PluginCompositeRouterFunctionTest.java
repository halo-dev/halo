package run.halo.app.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pf4j.PluginWrapper;
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
import run.halo.app.plugin.event.HaloPluginStartedEvent;
import run.halo.app.plugin.event.HaloPluginStoppedEvent;
import run.halo.app.plugin.resources.ReverseProxyRouterFunctionFactory;

/**
 * Tests for {@link PluginCompositeRouterFunction}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class PluginCompositeRouterFunctionTest {
    private final ServerCodecConfigurer codecConfigurer = ServerCodecConfigurer.create();

    @Mock
    private ReverseProxyRouterFunctionFactory reverseProxyRouterFunctionFactory;

    @Mock
    private PluginApplicationContext pluginApplicationContext;

    @Mock
    private PluginWrapper pluginWrapper;

    private PluginCompositeRouterFunction compositeRouterFunction;

    private HandlerFunction<ServerResponse> handlerFunction;
    private RouterFunction<ServerResponse> routerFunction;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        compositeRouterFunction =
            new PluginCompositeRouterFunction(reverseProxyRouterFunctionFactory);

        ExtensionContextRegistry.getInstance().register("fakeA", pluginApplicationContext);
        when(pluginWrapper.getPluginId()).thenReturn("fakeA");

        handlerFunction = request -> ServerResponse.ok().build();
        routerFunction = request -> Mono.just(handlerFunction);

        ObjectProvider objectProvider = mock(ObjectProvider.class);
        when(objectProvider.orderedStream()).thenReturn(Stream.of(routerFunction));

        when(pluginApplicationContext.getBeanProvider(RouterFunction.class))
            .thenReturn(objectProvider);
    }

    @Test
    void route() {
        // trigger haloPluginStartedEvent
        compositeRouterFunction.onPluginStarted(new HaloPluginStartedEvent(this, pluginWrapper));

        RouterFunctionMapping mapping = new RouterFunctionMapping(compositeRouterFunction);
        mapping.setMessageReaders(this.codecConfigurer.getReaders());

        Mono<Object> result = mapping.getHandler(createExchange("https://example.com/match"));

        StepVerifier.create(result)
            .expectNext(handlerFunction)
            .expectComplete()
            .verify();
    }

    @Test
    void onPluginStarted() {
        assertThat(compositeRouterFunction.getRouterFunction("fakeA")).isNull();

        // trigger haloPluginStartedEvent
        compositeRouterFunction.onPluginStarted(new HaloPluginStartedEvent(this, pluginWrapper));
        assertThat(compositeRouterFunction.getRouterFunction("fakeA")).isEqualTo(routerFunction);
    }

    @Test
    void onPluginStopped() {
        // trigger haloPluginStartedEvent
        compositeRouterFunction.onPluginStarted(new HaloPluginStartedEvent(this, pluginWrapper));
        assertThat(compositeRouterFunction.getRouterFunction("fakeA")).isEqualTo(routerFunction);

        // trigger HaloPluginStoppedEvent
        compositeRouterFunction.onPluginStopped(new HaloPluginStoppedEvent(this, pluginWrapper));
        assertThat(compositeRouterFunction.getRouterFunction("fakeA")).isNull();
    }

    private ServerWebExchange createExchange(String urlTemplate) {
        return MockServerWebExchange.from(MockServerHttpRequest.get(urlTemplate));
    }

}