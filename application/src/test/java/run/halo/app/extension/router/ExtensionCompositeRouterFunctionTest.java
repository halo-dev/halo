package run.halo.app.extension.router;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import run.halo.app.extension.FakeExtension;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.event.SchemeAddedEvent;
import run.halo.app.extension.event.SchemeRemovedEvent;

@ExtendWith(MockitoExtension.class)
class ExtensionCompositeRouterFunctionTest {

    @Mock
    ReactiveExtensionClient client;

    @InjectMocks
    ExtensionCompositeRouterFunction extensionRouterFunc;

    @Test
    void shouldRouteWhenSchemeRegistered() {
        var exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/apis/fake.halo.run/v1alpha1/fakes").build());

        var messageReaders = HandlerStrategies.withDefaults().messageReaders();
        ServerRequest request = ServerRequest.create(exchange, messageReaders);

        var handlerFunc = extensionRouterFunc.route(request).block();
        assertNull(handlerFunc);

        // trigger registering scheme
        extensionRouterFunc.onSchemeAddedEvent(
            new SchemeAddedEvent(this, Scheme.buildFromType(FakeExtension.class))
        );

        handlerFunc = extensionRouterFunc.route(request).block();
        assertNotNull(handlerFunc);
    }

    @Test
    void shouldNotRouteWhenSchemeUnregistered() {
        var exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/apis/fake.halo.run/v1alpha1/fakes").build());

        var messageReaders = HandlerStrategies.withDefaults().messageReaders();

        // trigger registering scheme
        extensionRouterFunc.onSchemeAddedEvent(
            new SchemeAddedEvent(this, Scheme.buildFromType(FakeExtension.class))
        );

        ServerRequest request = ServerRequest.create(exchange, messageReaders);
        var handlerFunc = extensionRouterFunc.route(request).block();
        assertNotNull(handlerFunc);

        // trigger registering scheme
        extensionRouterFunc.onSchemeRemovedEvent(
            new SchemeRemovedEvent(this, Scheme.buildFromType(FakeExtension.class))
        );
        handlerFunc = extensionRouterFunc.route(request).block();
        assertNull(handlerFunc);
    }

}
