package run.halo.app.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.web.reactive.function.server.EntityResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.extension.ExtensionRouterFunctionFactory.ExtensionCreateHandler;
import run.halo.app.extension.exception.ExtensionConvertException;
import run.halo.app.extension.exception.ExtensionNotFoundException;

@ExtendWith(MockitoExtension.class)
class ExtensionCreateHandlerTest {

    @Mock
    ExtensionClient client;

    @Test
    void shouldBuildPathPatternCorrectly() {
        var scheme = Scheme.buildFromType(FakeExtension.class);
        var getHandler = new ExtensionCreateHandler(scheme, client);
        var pathPattern = getHandler.pathPattern();
        assertEquals("/apis/fake.halo.run/v1alpha1/fakes", pathPattern);
    }

    @Test
    void shouldHandleCorrectly() {
        final var fake = new FakeExtension();
        var metadata = new Metadata();
        metadata.setName("my-fake");
        fake.setMetadata(metadata);

        var unstructured = new Unstructured();
        unstructured.setMetadata(metadata);
        unstructured.setApiVersion("fake.halo.run/v1alpha1");
        unstructured.setKind("Fake");

        var serverRequest = MockServerRequest.builder()
            .body(Mono.just(unstructured));
        when(client.fetch(eq(FakeExtension.class), eq("my-fake"))).thenReturn(Optional.of(fake));

        var scheme = Scheme.buildFromType(FakeExtension.class);
        var getHandler = new ExtensionCreateHandler(scheme, client);
        var responseMono = getHandler.handle(serverRequest);

        StepVerifier.create(responseMono)
            .consumeNextWith(response -> {
                assertEquals(HttpStatus.OK, response.statusCode());
                assertEquals(MediaType.APPLICATION_JSON, response.headers().getContentType());
                assertTrue(response instanceof EntityResponse<?>);
                assertEquals(fake, ((EntityResponse<?>) response).entity());
            })
            .verifyComplete();
        verify(client, times(1)).fetch(eq(FakeExtension.class), eq("my-fake"));
        verify(client, times(1)).create(eq(unstructured));
    }

    @Test
    void shouldReturnErrorWhenNoBodyProvided() {
        var serverRequest = MockServerRequest.builder()
            .body(Mono.empty());
        var scheme = Scheme.buildFromType(FakeExtension.class);
        var getHandler = new ExtensionCreateHandler(scheme, client);
        var responseMono = getHandler.handle(serverRequest);
        StepVerifier.create(responseMono)
            .verifyError(ExtensionConvertException.class);
    }

    @Test
    void shouldReturnErrorWhenExtensionNotFound() {
        final var unstructured = new Unstructured();
        var metadata = new Metadata();
        metadata.setName("my-fake");
        unstructured.setMetadata(metadata);
        unstructured.setApiVersion("fake.halo.run/v1alpha1");
        unstructured.setKind("Fake");

        var serverRequest = MockServerRequest.builder()
            .body(Mono.just(unstructured));
        when(client.fetch(eq(FakeExtension.class), eq("my-fake"))).thenReturn(Optional.empty());

        var scheme = Scheme.buildFromType(FakeExtension.class);
        var getHandler = new ExtensionCreateHandler(scheme, client);
        var responseMono = getHandler.handle(serverRequest);

        StepVerifier.create(responseMono)
            .verifyError(ExtensionNotFoundException.class);
    }
}
