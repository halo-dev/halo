package run.halo.app.extension.router;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static run.halo.app.extension.GroupVersionKind.fromExtension;

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
import run.halo.app.extension.exception.ExtensionNotFoundException;

@ExtendWith(MockitoExtension.class)
class ExtensionDeleteHandlerTest {

    @Mock
    ReactiveExtensionClient client;

    @Test
    void shouldBuildPathPatternCorrectly() {
        var scheme = Scheme.buildFromType(FakeExtension.class);
        var deleteHandler = new ExtensionDeleteHandler(scheme, client);
        var pathPattern = deleteHandler.pathPattern();
        assertEquals("/apis/fake.halo.run/v1alpha1/fakes/{name}", pathPattern);
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
            .pathVariable("name", "my-fake")
            .body(Mono.just(unstructured));
        when(client.get(eq(FakeExtension.class), eq("my-fake"))).thenReturn(Mono.just(fake));
        when(client.delete(eq(fake))).thenReturn(Mono.just(fake));

        var scheme = Scheme.buildFromType(FakeExtension.class);
        var deleteHandler = new ExtensionDeleteHandler(scheme, client);
        var responseMono = deleteHandler.handle(serverRequest);

        StepVerifier.create(responseMono)
            .assertNext(response -> {
                assertEquals(HttpStatus.OK, response.statusCode());
                assertEquals(MediaType.APPLICATION_JSON, response.headers().getContentType());
                assertTrue(response instanceof EntityResponse<?>);
                assertEquals(fake, ((EntityResponse<?>) response).entity());
            })
            .verifyComplete();
        verify(client, times(1)).get(eq(FakeExtension.class), eq("my-fake"));
        verify(client, times(1)).delete(any());
        verify(client, times(0)).update(any());
    }

    @Test
    void shouldReturnErrorWhenNoNameProvided() {
        var serverRequest = MockServerRequest.builder()
            .body(Mono.empty());
        var scheme = Scheme.buildFromType(FakeExtension.class);
        var deleteHandler = new ExtensionDeleteHandler(scheme, client);
        assertThrows(IllegalArgumentException.class, () -> deleteHandler.handle(serverRequest));
    }

    @Test
    void shouldReturnErrorWhenExtensionNotFound() {
        var serverRequest = MockServerRequest.builder()
            .pathVariable("name", "my-fake")
            .build();
        when(client.get(FakeExtension.class, "my-fake")).thenReturn(
            Mono.error(
                new ExtensionNotFoundException(fromExtension(FakeExtension.class), "my-fake")));

        var scheme = Scheme.buildFromType(FakeExtension.class);
        var deleteHandler = new ExtensionDeleteHandler(scheme, client);
        var responseMono = deleteHandler.handle(serverRequest);

        StepVerifier.create(responseMono)
            .verifyError(ExtensionNotFoundException.class);

        verify(client, times(1)).get(same(FakeExtension.class), anyString());
        verify(client, times(0)).update(any());
        verify(client, times(0)).delete(any());
    }
}
