package run.halo.app.extension.router;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jsonpatch.AddOperation;
import com.github.fge.jsonpatch.JsonPatch;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import run.halo.app.extension.FakeExtension;
import run.halo.app.extension.JsonExtension;
import run.halo.app.extension.Metadata;
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

    @Spy
    Scheme scheme = Scheme.buildFromType(FakeExtension.class);

    @InjectMocks
    ExtensionRouterFunctionFactory factory;

    WebTestClient webClient;

    @BeforeEach
    void setUp() {
        webClient = WebTestClient.bindToRouterFunction(factory.create()).build();
    }

    @Nested
    class PatchTest {

        @Test
        void shouldResponse404IfMethodNotPatch() {
            webClient.method(HttpMethod.POST)
                .uri("/apis/fake.halo.run/v1alpha1/fakes/my-fake")
                .exchange()
                .expectStatus().isNotFound();
        }

        @Test
        void shouldResponse415IfMediaTypeIsInsufficient() {
            webClient.method(HttpMethod.PATCH)
                .uri("/apis/fake.halo.run/v1alpha1/fakes/my-fake")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);

            webClient.method(HttpMethod.PATCH)
                .uri("/apis/fake.halo.run/v1alpha1/fakes/my-fake")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }

        @Test
        void shouldResponseBadRequestIfNoPatchBody() {
            webClient.method(HttpMethod.PATCH)
                .uri("/apis/fake.halo.run/v1alpha1/fakes/my-fake")
                .header(HttpHeaders.CONTENT_TYPE, "application/json-patch+json")
                .exchange()
                .expectStatus().isBadRequest();
        }

        @Test
        void shouldPatchCorrectly() {
            var fake = new FakeExtension();
            var metadata = new Metadata();
            metadata.setName("my-fake");
            fake.setMetadata(metadata);
            var mapper = Jackson2ObjectMapperBuilder.json().build();
            var jsonExt = mapper.convertValue(fake, JsonExtension.class);

            when(client.getJsonExtension(scheme.groupVersionKind(), "my-fake"))
                .thenReturn(Mono.just(jsonExt));

            var status = new FakeExtension.FakeStatus();
            status.setState("running");
            fake.setStatus(status);
            var updatedExt = mapper.convertValue(fake, JsonExtension.class);
            when(client.update(any(JsonExtension.class))).thenReturn(Mono.just(updatedExt));

            var stateNode = JsonNodeFactory.instance.textNode("running");
            var jsonPatch = new JsonPatch(List.of(
                new AddOperation(JsonPointer.of("status", "state"), stateNode)
            ));
            webClient.method(HttpMethod.PATCH)
                .uri("/apis/fake.halo.run/v1alpha1/fakes/my-fake")
                .header(HttpHeaders.CONTENT_TYPE, "application/json-patch+json")
                .bodyValue(jsonPatch)
                .exchange()
                .expectStatus().isOk()
                .expectBody(JsonExtension.class).isEqualTo(updatedExt);

            verify(client).<JsonExtension>update(assertArg(ext -> {
                var state = ext.getInternal().get("status").get("state")
                    .asText();
                assertEquals("running", state);
            }));
        }
    }


    @Test
    void shouldCreateSuccessfully() {
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