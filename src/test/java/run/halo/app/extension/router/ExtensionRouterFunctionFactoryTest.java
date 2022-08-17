package run.halo.app.extension.router;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import run.halo.app.extension.FakeExtension;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.router.ExtensionRouterFunctionFactory.CreateHandler;
import run.halo.app.extension.router.ExtensionRouterFunctionFactory.GetHandler;
import run.halo.app.extension.router.ExtensionRouterFunctionFactory.ListHandler;
import run.halo.app.extension.router.ExtensionRouterFunctionFactory.UpdateHandler;

@ExtendWith(MockitoExtension.class)
class ExtensionRouterFunctionFactoryTest {

    @Mock
    ReactiveExtensionClient client;

    @Test
    void shouldCreateSuccessfully() {
        var scheme = Scheme.buildFromType(FakeExtension.class);
        var factory = new ExtensionRouterFunctionFactory(scheme, client);

        var routerFunction = factory.create();

        testCases().forEach(testCase -> {
            List<HttpMessageReader<?>> messageReaders =
                HandlerStrategies.withDefaults().messageReaders();
            var request = ServerRequest.create(testCase.webExchange, messageReaders);
            var handlerFunc = routerFunction.route(request).block();
            assertInstanceOf(testCase.expectHandlerType, handlerFunc);
        });
    }

    List<TestCase> testCases() {
        var listWebExchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/apis/fake.halo.run/v1alpha1/fakes").build());

        var getWebExchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/apis/fake.halo.run/v1alpha1/fakes/my-fake").build()
        );

        var createWebExchange = MockServerWebExchange.from(
            MockServerHttpRequest.post("/apis/fake.halo.run/v1alpha1/fakes").body("{}")
        );

        var updateWebExchange = MockServerWebExchange.from(
            MockServerHttpRequest.put("/apis/fake.halo.run/v1alpha1/fakes/my-fake").body("{}")
        );

        return List.of(
            new TestCase(listWebExchange, ListHandler.class),
            new TestCase(getWebExchange, GetHandler.class),
            new TestCase(createWebExchange, CreateHandler.class),
            new TestCase(updateWebExchange, UpdateHandler.class)
        );
    }

    record TestCase(ServerWebExchange webExchange,
                    Class<? extends HandlerFunction<ServerResponse>> expectHandlerType) {
    }

}