package run.halo.app.extension.router;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import run.halo.app.extension.FakeExtension;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.SchemeWatcherManager;
import run.halo.app.extension.SchemeWatcherManager.SchemeRegistered;
import run.halo.app.extension.SchemeWatcherManager.SchemeUnregistered;

@ExtendWith(MockitoExtension.class)
class ExtensionCompositeRouterFunctionTest {

    @Mock
    ReactiveExtensionClient client;

    @Mock
    SchemeManager schemeManager;

    @Mock
    SchemeWatcherManager watcherManager;

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
        extensionRouterFunc.onChange(
            new SchemeRegistered(Scheme.buildFromType(FakeExtension.class)));

        handlerFunc = extensionRouterFunc.route(request).block();
        assertNotNull(handlerFunc);
    }

    @Test
    void shouldNotRouteWhenSchemeUnregistered() {
        var exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/apis/fake.halo.run/v1alpha1/fakes").build());

        var messageReaders = HandlerStrategies.withDefaults().messageReaders();

        // trigger registering scheme
        extensionRouterFunc.onChange(
            new SchemeRegistered(Scheme.buildFromType(FakeExtension.class)));

        ServerRequest request = ServerRequest.create(exchange, messageReaders);
        var handlerFunc = extensionRouterFunc.route(request).block();
        assertNotNull(handlerFunc);

        // trigger registering scheme
        extensionRouterFunc.onChange(
            new SchemeUnregistered(Scheme.buildFromType(FakeExtension.class)));
        handlerFunc = extensionRouterFunc.route(request).block();
        assertNull(handlerFunc);
    }

    @Test
    void shouldRegisterWatcherAfterPropertiesSet() {
        extensionRouterFunc.afterPropertiesSet();
        verify(watcherManager).register(eq(extensionRouterFunc));
    }

    @Test
    void shouldBuildRouterFunctionsOnApplicationStarted() {
        var applicationStartedEvent = mock(ApplicationStartedEvent.class);
        extensionRouterFunc.onApplicationEvent(applicationStartedEvent);
        verify(schemeManager).schemes();
    }

}
