package run.halo.app.extension.router;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
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
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/fake")
            .queryParam("sort", "metadata.name,desc"));
        var serverRequest = MockServerRequest.builder().exchange(exchange).build();
        final var fake01 = FakeExtension.createFake("fake01");
        final var fake02 = FakeExtension.createFake("fake02");
        var fakeListResult = new ListResult<>(0, 0, 2, List.of(fake01, fake02));
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
        verify(client).list(same(FakeExtension.class), any(), argThat(comp -> {
            var sortedFakes = Stream.of(fake01, fake02).sorted(comp).toList();
            return Objects.equals(List.of(fake02, fake01), sortedFakes);
        }), anyInt(), anyInt());
    }

}
