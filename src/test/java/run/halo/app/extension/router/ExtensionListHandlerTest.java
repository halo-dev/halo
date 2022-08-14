package run.halo.app.extension.router;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.same;
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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.extension.FakeExtension;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Scheme;

@ExtendWith(MockitoExtension.class)
class ExtensionListHandlerTest {

    @Mock
    ReactiveExtensionClient client;

    @Test
    void shouldBuildPathPatternCorrectly() {
        var scheme = Scheme.buildFromType(FakeExtension.class);
        var listHandler = new ExtensionListHandler(scheme, client);
        var pathPattern = listHandler.pathPattern();
        assertEquals("/apis/fake.halo.run/v1alpha1/fakes", pathPattern);
    }

    @Test
    void shouldHandleCorrectly() {
        var scheme = Scheme.buildFromType(FakeExtension.class);
        var listHandler = new ExtensionListHandler(scheme, client);
        var serverRequest = MockServerRequest.builder().build();
        final var fake = new FakeExtension();
        var fakeListResult = new ListResult<>(0, 0, 1, List.of(fake));
        when(client.list(same(FakeExtension.class), any(), any(), anyInt(), anyInt()))
            .thenReturn(Mono.just(fakeListResult));

        var responseMono = listHandler.handle(serverRequest);

        StepVerifier.create(responseMono)
            .consumeNextWith(response -> {
                assertEquals(HttpStatus.OK, response.statusCode());
                assertEquals(MediaType.APPLICATION_JSON, response.headers().getContentType());
                assertTrue(response instanceof EntityResponse<?>);
                assertEquals(fakeListResult, ((EntityResponse<?>) response).entity());
            })
            .verifyComplete();
    }

}
