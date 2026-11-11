package run.halo.app.extension.router;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.FakeExtension;
import run.halo.app.extension.JsonExtension;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.Unstructured;

@ExtendWith(MockitoExtension.class)
class ExtensionMutationIdentityTest {
    private static final JsonMapper MAPPER = JsonMapper.builder().build();
    private static final Scheme SCHEME = Scheme.buildFromType(FakeExtension.class);
    private static final String VALID = """
            {"apiVersion":"fake.halo.run/v1alpha1","kind":"Fake","metadata":{"name":"my-fake","version":1},
             "status":{"state":"init"}}
            """;

    @Mock
    ReactiveExtensionClient client;

    static Stream<String> invalidDocuments() throws IOException {
        List<String> documents = new ArrayList<>();
        for (String field : List.of("apiVersion", "kind", "metadata")) {
            var missing = (ObjectNode) MAPPER.readTree(VALID);
            missing.remove(field);
            documents.add(missing.toString());
            for (String value : List.of("null", "[]", "{}", "123", "true", "\"\"", "\" \"")) {
                var invalid = (ObjectNode) MAPPER.readTree(VALID);
                invalid.set(field, MAPPER.readTree(value));
                documents.add(invalid.toString());
            }
        }
        for (String value : List.of("null", "[]", "{}", "123", "true", "\"\"", "\" \"", "\"other\"")) {
            var invalid = (ObjectNode) MAPPER.readTree(VALID);
            ((ObjectNode) invalid.get("metadata")).set("name", MAPPER.readTree(value));
            documents.add(invalid.toString());
        }
        var missingName = (ObjectNode) MAPPER.readTree(VALID);
        ((ObjectNode) missingName.get("metadata")).remove("name");
        documents.add(missingName.toString());
        for (String version : List.of("fake.halo.run/v1alpha1/", "other.halo.run/v1alpha1", "fake.halo.run/v2")) {
            var invalid = (ObjectNode) MAPPER.readTree(VALID);
            invalid.put("apiVersion", version);
            documents.add(invalid.toString());
        }
        var otherKind = (ObjectNode) MAPPER.readTree(VALID);
        otherKind.put("kind", "Other");
        documents.add(otherKind.toString());
        return documents.stream();
    }

    static Stream<String> invalidGroupVersionKinds() throws IOException {
        List<String> documents = new ArrayList<>();
        for (String field : List.of("apiVersion", "kind")) {
            var missing = (ObjectNode) MAPPER.readTree(VALID);
            missing.remove(field);
            documents.add(missing.toString());
            for (String value : List.of("null", "[]", "{}", "123", "true", "\"\"", "\" \"")) {
                var invalid = (ObjectNode) MAPPER.readTree(VALID);
                invalid.set(field, MAPPER.readTree(value));
                documents.add(invalid.toString());
            }
        }
        for (String version : List.of("fake.halo.run/v1alpha1/", "other.halo.run/v1alpha1", "fake.halo.run/v2")) {
            var invalid = (ObjectNode) MAPPER.readTree(VALID);
            invalid.put("apiVersion", version);
            documents.add(invalid.toString());
        }
        var otherKind = (ObjectNode) MAPPER.readTree(VALID);
        otherKind.put("kind", "Other");
        documents.add(otherKind.toString());
        return documents.stream();
    }

    @ParameterizedTest
    @MethodSource("invalidGroupVersionKinds")
    void shouldRejectCreateWithMismatchedGroupVersionKind(String json) throws IOException {
        var extension = MAPPER.readValue(json, Unstructured.class);
        var request = MockServerRequest.builder().body(Mono.just(extension));
        StepVerifier.create(new ExtensionCreateHandler(SCHEME, client).handle(request))
                .verifyError(ServerWebInputException.class);
        verify(client, never()).create(any());
    }

    @Test
    void shouldAllowCreateWithMatchingGroupVersionKind() throws IOException {
        var extension = MAPPER.readValue(VALID, Unstructured.class);
        when(client.create(any(Unstructured.class))).thenReturn(Mono.just(extension));
        var request = MockServerRequest.builder().body(Mono.just(extension));
        StepVerifier.create(new ExtensionCreateHandler(SCHEME, client).handle(request))
                .assertNext(response -> assertEquals(HttpStatus.CREATED, response.statusCode()))
                .verifyComplete();
        verify(client).create(extension);
    }

    @ParameterizedTest
    @MethodSource("invalidDocuments")
    void shouldRejectInvalidPutIdentityBeforeUpdating(String json) throws IOException {
        var extension = MAPPER.readValue(json, Unstructured.class);
        var request =
                MockServerRequest.builder().pathVariable("name", "my-fake").body(Mono.just(extension));
        StepVerifier.create(new ExtensionUpdateHandler(SCHEME, client).handle(request))
                .verifyError(ServerWebInputException.class);
        verify(client, never()).update(any());
    }

    @ParameterizedTest
    @MethodSource("invalidDocuments")
    void shouldRejectInvalidPatchedIdentityWithoutMutatingSource(String json) throws IOException {
        assertRejectedPatch("[{\"op\":\"replace\",\"path\":\"\",\"value\":" + json + "}]");
    }

    @ParameterizedTest
    @ValueSource(strings = {"null", "[]", "123", "true", "\"text\""})
    void shouldRejectNonObjectPatchRoot(String value) throws IOException {
        assertRejectedPatch("[{\"op\":\"replace\",\"path\":\"\",\"value\":" + value + "}]");
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "[{\"op\":\"remove\",\"path\":\"/metadata\"}]",
                "[{\"op\":\"remove\",\"path\":\"/kind\"}]",
                "[{\"op\":\"remove\",\"path\":\"/apiVersion\"}]",
                "[{\"op\":\"remove\",\"path\":\"/metadata/name\"}]",
                "[{\"op\":\"copy\",\"from\":\"/status/state\",\"path\":\"/metadata/name\"}]",
                "[{\"op\":\"move\",\"from\":\"/metadata/name\",\"path\":\"/status/state\"}]",
                "[{\"op\":\"add\",\"path\":\"/kind\",\"value\":\"Other\"}]"
            })
    void shouldRejectIndirectOrRemovedIdentity(String patch) throws IOException {
        assertRejectedPatch(patch);
    }

    private void assertRejectedPatch(String patchJson) throws IOException {
        var source = new JsonExtension(MAPPER, (ObjectNode) MAPPER.readTree(VALID));
        var before = source.getInternal().deepCopy();
        when(client.getJsonExtension(SCHEME.groupVersionKind(), "my-fake")).thenReturn(Mono.just(source));
        var request = MockServerRequest.builder()
                .pathVariable("name", "my-fake")
                .header("Content-Type", "application/json-patch+json")
                .body(Mono.just(JsonPatch.fromJson(MAPPER.readTree(patchJson))));
        StepVerifier.create(new ExtensionPatchHandler(SCHEME, client).handle(request))
                .verifyError(ServerWebInputException.class);
        verify(client, never()).update(any());
        assertEquals(before, source.getInternal());
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "[]",
                "[{\"op\":\"test\",\"path\":\"/metadata/name\",\"value\":\"my-fake\"}]",
                "[{\"op\":\"add\",\"path\":\"/metadata/labels\",\"value\":{\"test\":\"value\"}}]",
                "[{\"op\":\"add\",\"path\":\"/metadata/annotations\",\"value\":{\"test\":\"value\"}}]",
                "[{\"op\":\"remove\",\"path\":\"/status/state\"}]",
                "[{\"op\":\"copy\",\"from\":\"/status/state\",\"path\":\"/status/copy\"}]",
                "[{\"op\":\"move\",\"from\":\"/status/state\",\"path\":\"/status/moved\"}]",
                "[{\"op\":\"replace\",\"path\":\"/status/state\",\"value\":\"ready\"}]",
                "[{\"op\":\"remove\",\"path\":\"/kind\"},{\"op\":\"add\",\"path\":\"/kind\",\"value\":\"Fake\"}]"
            })
    void shouldAllowOrdinaryPatchOperations(String patch) throws IOException, JsonPatchException {
        assertSuccessfulPatch(patch);
    }

    @Test
    void shouldAllowRootReplacementWithSameIdentity() throws IOException, JsonPatchException {
        assertSuccessfulPatch("[{\"op\":\"replace\",\"path\":\"\",\"value\":" + VALID + "}]");
    }

    private void assertSuccessfulPatch(String json) throws IOException, JsonPatchException {
        var source = new JsonExtension(MAPPER, (ObjectNode) MAPPER.readTree(VALID));
        var patch = JsonPatch.fromJson(MAPPER.readTree(json));
        var expected = patch.apply(source.getInternal());
        when(client.getJsonExtension(SCHEME.groupVersionKind(), "my-fake")).thenReturn(Mono.just(source));
        when(client.update(any(JsonExtension.class))).thenAnswer(invocation -> {
            JsonExtension updated = invocation.getArgument(0);
            assertEquals(expected, updated.getInternal());
            assertEquals(1L, updated.getMetadata().getVersion());
            return Mono.just(updated);
        });
        var request = MockServerRequest.builder()
                .pathVariable("name", "my-fake")
                .header("Content-Type", "application/json-patch+json")
                .body(Mono.just(patch));
        StepVerifier.create(new ExtensionPatchHandler(SCHEME, client).handle(request))
                .assertNext(response -> assertEquals(HttpStatus.OK, response.statusCode()))
                .verifyComplete();
        verify(client).update(any(JsonExtension.class));
    }

    @Test
    void shouldNotCoerceNumericNameToMatchingString() throws IOException {
        var json = (ObjectNode) MAPPER.readTree(VALID);
        ((ObjectNode) json.get("metadata")).put("name", 123);
        assertFalse(ExtensionIdentity.matches(SCHEME, "123", new JsonExtension(MAPPER, json)));
        assertFalse(ExtensionIdentity.matches(SCHEME, "123", MAPPER.treeToValue(json, Unstructured.class)));
    }

    @Test
    void shouldAcceptCanonicalCoreIdentityButNotLeadingSlash() {
        var scheme = Scheme.buildFromType(ConfigMap.class);
        var core = new Unstructured(
                Map.of("apiVersion", "v1alpha1", "kind", "ConfigMap", "metadata", Map.of("name", "test")));
        assertTrue(ExtensionIdentity.matches(scheme, "test", core));
        var nonCanonical = new Unstructured(
                Map.of("apiVersion", "/v1alpha1", "kind", "ConfigMap", "metadata", Map.of("name", "test")));
        assertFalse(ExtensionIdentity.matches(scheme, "test", nonCanonical));
        assertFalse(ExtensionIdentity.matches(scheme, "test", null));
    }
}
