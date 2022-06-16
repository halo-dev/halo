package run.halo.app.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.web.reactive.function.server.EntityResponse;
import reactor.test.StepVerifier;
import run.halo.app.extension.ExtensionRouterFunctionFactory.ExtensionListHandler;

@ExtendWith(MockitoExtension.class)
class ExtensionListHandlerTest {

    @Mock
    ExtensionClient client;

    @Test
    void shouldBuildPathPatternCorrectly() {
        var scheme = Scheme.buildFromType(FakeExtension.class);
        var getHandler = new ExtensionListHandler(scheme, client);
        var pathPattern = getHandler.pathPattern();
        assertEquals("/apis/fake.halo.run/v1alpha1/fakes", pathPattern);
    }

    @Test
    void shouldHandleCorrectly() {
        var scheme = Scheme.buildFromType(FakeExtension.class);
        var getHandler = new ExtensionListHandler(scheme, client);
        var serverRequest = MockServerRequest.builder().build();
        final var fake = new FakeExtension();
        when(client.list(eq(FakeExtension.class), any(), any())).thenReturn(List.of(fake));

        var responseMono = getHandler.handle(serverRequest);

        StepVerifier.create(responseMono)
            .consumeNextWith(response -> {
                assertEquals(HttpStatus.OK, response.statusCode());
                assertEquals(MediaType.APPLICATION_JSON, response.headers().getContentType());
                assertTrue(response instanceof EntityResponse<?>);
                assertEquals(List.of(fake), ((EntityResponse<?>) response).entity());
            })
            .verifyComplete();
    }

}
