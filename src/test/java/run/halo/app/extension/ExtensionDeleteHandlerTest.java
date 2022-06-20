package run.halo.app.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
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
import run.halo.app.extension.ExtensionRouterFunctionFactory.ExtensionDeleteHandler;
import run.halo.app.extension.exception.ExtensionNotFoundException;

@ExtendWith(MockitoExtension.class)
class ExtensionDeleteHandlerTest {

    @Mock
    ExtensionClient client;

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
        when(client.fetch(eq(FakeExtension.class), eq("my-fake"))).thenReturn(Optional.of(fake));
        doNothing().when(client).update(any());

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
        verify(client, times(2)).fetch(eq(FakeExtension.class), eq("my-fake"));
        verify(client, times(1)).update(
            argThat(fakeToDelete -> fakeToDelete.getMetadata().getDeletionTimestamp() != null));
        verify(client, times(0)).delete(any());
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
        when(client.fetch(FakeExtension.class, "my-fake")).thenReturn(Optional.empty());

        var scheme = Scheme.buildFromType(FakeExtension.class);
        var deleteHandler = new ExtensionDeleteHandler(scheme, client);
        var responseMono = deleteHandler.handle(serverRequest);

        StepVerifier.create(responseMono)
            .verifyError(ExtensionNotFoundException.class);

        verify(client, times(1)).fetch(any(), anyString());
        verify(client, times(0)).update(any());
        verify(client, times(0)).delete(any());
    }
}
