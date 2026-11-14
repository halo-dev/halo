package run.halo.app.extension.router;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.fge.jsonpatch.JsonPatch;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.web.reactive.function.server.EntityResponse;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.extension.FakeExtension;
import run.halo.app.extension.JsonExtension;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Scheme;

@ExtendWith(MockitoExtension.class)
class ExtensionPatchHandlerTest {

    static final String JSON_PATCH = "application/json-patch+json";

    @Mock
    ReactiveExtensionClient client;

    ObjectMapper objectMapper;

    Scheme scheme;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder().build();
        scheme = Scheme.buildFromType(FakeExtension.class);
    }

    @Test
    void shouldBuildPathPatternCorrectly() {
        var patchHandler = new ExtensionPatchHandler(scheme, client);
        assertEquals("/apis/fake.halo.run/v1alpha1/fakes/{name}", patchHandler.pathPattern());
    }

    @Test
    void shouldPatchWhenIdentityIsUnchanged() throws IOException {
        var source = fakeJsonExtension("my-fake");
        var patch = jsonPatch("""
                [{"op":"replace","path":"/status/state","value":"ready"}]
                """);
        when(client.getJsonExtension(eq(scheme.groupVersionKind()), eq("my-fake")))
                .thenReturn(Mono.just(source));
        when(client.update(any(JsonExtension.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        var handler = new ExtensionPatchHandler(scheme, client);
        var responseMono = handler.handle(patchRequest("my-fake", patch));

        StepVerifier.create(responseMono)
                .assertNext(response -> {
                    assertEquals(HttpStatus.OK, response.statusCode());
                    assertTrue(response instanceof EntityResponse<?>);
                    var updated = (JsonExtension) ((EntityResponse<?>) response).entity();
                    assertEquals("Fake", updated.getKind());
                    assertEquals("fake.halo.run/v1alpha1", updated.getApiVersion());
                    assertEquals("my-fake", updated.getMetadata().getName());
                    assertEquals(
                            "ready",
                            updated.getInternal().get("status").get("state").asText());
                })
                .verifyComplete();
        verify(client, times(1)).update(any(JsonExtension.class));
    }

    @Test
    void shouldRejectPatchThatChangesKind() throws IOException {
        shouldRejectIdentityPatch("""
                [{"op":"replace","path":"/kind","value":"Other"}]
                """);
    }

    @Test
    void shouldRejectPatchThatChangesApiVersion() throws IOException {
        shouldRejectIdentityPatch("""
                [{"op":"replace","path":"/apiVersion","value":"other.halo.run/v1alpha1"}]
                """);
    }

    @Test
    void shouldRejectPatchThatChangesName() throws IOException {
        shouldRejectIdentityPatch("""
                [{"op":"replace","path":"/metadata/name","value":"other-fake"}]
                """);
    }

    @Test
    void shouldRejectRootReplaceThatChangesIdentity() throws IOException {
        shouldRejectIdentityPatch("""
                [{"op":"replace","path":"","value":{
                  "apiVersion":"other.halo.run/v1alpha1",
                  "kind":"Other",
                  "metadata":{"name":"other-fake"}
                }}]
                """);
    }

    @Test
    void shouldReturnErrorWhenNoBodyProvided() {
        var request = MockServerRequest.builder()
                .pathVariable("name", "my-fake")
                .header(HttpHeaders.CONTENT_TYPE, JSON_PATCH)
                .body(Mono.empty());
        var handler = new ExtensionPatchHandler(scheme, client);
        StepVerifier.create(handler.handle(request)).verifyError(ServerWebInputException.class);
        verify(client, never()).update(any());
    }

    @Test
    void shouldReturnErrorWhenNoNameProvided() {
        var request = MockServerRequest.builder()
                .header(HttpHeaders.CONTENT_TYPE, JSON_PATCH)
                .body(Mono.empty());
        var handler = new ExtensionPatchHandler(scheme, client);
        assertThrows(IllegalArgumentException.class, () -> handler.handle(request));
    }

    @Test
    void shouldReturnErrorWhenContentTypeMissing() {
        var request =
                MockServerRequest.builder().pathVariable("name", "my-fake").body(Mono.empty());
        var handler = new ExtensionPatchHandler(scheme, client);
        StepVerifier.create(handler.handle(request)).verifyError(UnsupportedMediaTypeStatusException.class);
    }

    @Test
    void shouldReturnErrorWhenContentTypeUnsupported() {
        var request = MockServerRequest.builder()
                .pathVariable("name", "my-fake")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.empty());
        var handler = new ExtensionPatchHandler(scheme, client);
        StepVerifier.create(handler.handle(request)).verifyError(UnsupportedMediaTypeStatusException.class);
    }

    private void shouldRejectIdentityPatch(String patchJson) throws IOException {
        var source = fakeJsonExtension("my-fake");
        when(client.getJsonExtension(eq(scheme.groupVersionKind()), eq("my-fake")))
                .thenReturn(Mono.just(source));

        var handler = new ExtensionPatchHandler(scheme, client);
        StepVerifier.create(handler.handle(patchRequest("my-fake", jsonPatch(patchJson))))
                .verifyError(ServerWebInputException.class);
        verify(client, never()).update(any());
    }

    private JsonExtension fakeJsonExtension(String name) {
        var fake = FakeExtension.createFake(name);
        fake.getStatus().setState("init");
        return new JsonExtension(objectMapper, fake);
    }

    private JsonPatch jsonPatch(String json) throws IOException {
        return JsonPatch.fromJson(objectMapper.readTree(json));
    }

    private MockServerRequest patchRequest(String name, JsonPatch patch) {
        return MockServerRequest.builder()
                .pathVariable("name", name)
                .header(HttpHeaders.CONTENT_TYPE, JSON_PATCH)
                .body(Mono.just(patch));
    }
}
