package run.halo.app.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
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
import reactor.test.StepVerifier;
import run.halo.app.extension.ExtensionRouterFunctionFactory.ExtensionGetHandler;
import run.halo.app.extension.exception.ExtensionNotFoundException;

@ExtendWith(MockitoExtension.class)
class ExtensionGetHandlerTest {

    @Mock
    ExtensionClient client;

    @Test
    void shouldBuildPathPatternCorrectly() {
        var scheme = Scheme.buildFromType(FakeExtension.class);
        var getHandler = new ExtensionGetHandler(scheme, client);
        var pathPattern = getHandler.pathPattern();
        assertEquals("/apis/fake.halo.run/v1alpha1/fakes/{name}", pathPattern);
    }

    @Test
    void shouldHandleCorrectly() {
        var scheme = Scheme.buildFromType(FakeExtension.class);
        var getHandler = new ExtensionGetHandler(scheme, client);
        var serverRequest = MockServerRequest.builder()
            .pathVariable("name", "my-fake")
            .build();
        final var fake = new FakeExtension();
        when(client.fetch(eq(FakeExtension.class), eq("my-fake"))).thenReturn(Optional.of(fake));

        var responseMono = getHandler.handle(serverRequest);

        StepVerifier.create(responseMono)
            .consumeNextWith(response -> {
                assertEquals(HttpStatus.OK, response.statusCode());
                assertEquals(MediaType.APPLICATION_JSON, response.headers().getContentType());
                assertTrue(response instanceof EntityResponse<?>);
                assertEquals(fake, ((EntityResponse<?>) response).entity());
            })
            .verifyComplete();
    }

    @Test
    void shouldThrowExceptionWhenExtensionNotFound() {
        var scheme = Scheme.buildFromType(FakeExtension.class);
        var getHandler = new ExtensionGetHandler(scheme, client);
        var serverRequest = MockServerRequest.builder()
            .pathVariable("name", "my-fake")
            .build();
        when(client.fetch(eq(FakeExtension.class), eq("my-fake"))).thenReturn(Optional.empty());

        assertThrows(ExtensionNotFoundException.class, () -> getHandler.handle(serverRequest));
    }
}
