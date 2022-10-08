package run.halo.app.plugin;

import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import run.halo.app.plugin.resources.ReverseProxyRouterFunctionRegistry;

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
    private ReverseProxyRouterFunctionRegistry reverseProxyRouterFunctionRegistry;

    private PluginCompositeRouterFunction compositeRouterFunction;

    private HandlerFunction<ServerResponse> handlerFunction;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        compositeRouterFunction =
            new PluginCompositeRouterFunction(reverseProxyRouterFunctionRegistry);

        handlerFunction = request -> ServerResponse.ok().build();
        RouterFunction<ServerResponse> routerFunction = request -> Mono.just(handlerFunction);

        when(reverseProxyRouterFunctionRegistry.getRouterFunctions())
            .thenReturn(List.of(routerFunction));
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
    }

    private ServerWebExchange createExchange(String urlTemplate) {
        return MockServerWebExchange.from(MockServerHttpRequest.get(urlTemplate));
    }

}