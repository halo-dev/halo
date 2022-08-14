package run.halo.app.extension.router;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Objects;
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
import run.halo.app.extension.FakeExtension;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.Unstructured;
import run.halo.app.extension.exception.ExtensionConvertException;
import run.halo.app.extension.exception.ExtensionNotFoundException;

@ExtendWith(MockitoExtension.class)
class ExtensionCreateHandlerTest {

    @Mock
    ReactiveExtensionClient client;

    @Test
    void shouldBuildPathPatternCorrectly() {
        var scheme = Scheme.buildFromType(FakeExtension.class);
        var createHandler = new ExtensionCreateHandler(scheme, client);
        var pathPattern = createHandler.pathPattern();
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
        when(client.create(any(Unstructured.class))).thenReturn(Mono.just(unstructured));

        var scheme = Scheme.buildFromType(FakeExtension.class);
        var getHandler = new ExtensionCreateHandler(scheme, client);
        var responseMono = getHandler.handle(serverRequest);

        StepVerifier.create(responseMono)
            .consumeNextWith(response -> {
                assertEquals(HttpStatus.CREATED, response.statusCode());
                assertEquals("/apis/fake.halo.run/v1alpha1/fakes/my-fake",
                    Objects.requireNonNull(response.headers().getLocation()).toString());
                assertEquals(MediaType.APPLICATION_JSON, response.headers().getContentType());
                assertTrue(response instanceof EntityResponse<?>);
                assertEquals(unstructured, ((EntityResponse<?>) response).entity());
            })
            .verifyComplete();
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
        doThrow(ExtensionNotFoundException.class).when(client).create(any());

        var scheme = Scheme.buildFromType(FakeExtension.class);
        var createHandler = new ExtensionCreateHandler(scheme, client);
        var responseMono = createHandler.handle(serverRequest);

        StepVerifier.create(responseMono)
            .verifyError(ExtensionNotFoundException.class);
        verify(client, times(1)).create(
            argThat(extension -> Objects.equals("my-fake", extension.getMetadata().getName())));
        verify(client, times(0)).fetch(same(FakeExtension.class), anyString());
    }
}
