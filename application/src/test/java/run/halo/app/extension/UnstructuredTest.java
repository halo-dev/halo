package run.halo.app.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import org.json.JSONException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import tools.jackson.databind.json.JsonMapper;

class UnstructuredTest {

    @Nested
    class Jackson2Test {

        ObjectMapper objectMapper = Unstructured.OBJECT_MAPPER;

        @Test
        void shouldSerializeCorrectly() throws JsonProcessingException {
            Map extensionMap = objectMapper.readValue(extensionJson, Map.class);
            var extension = new Unstructured(extensionMap);

            var gotNode = objectMapper.valueToTree(extension);
            assertEquals(objectMapper.readTree(extensionJson), gotNode);
        }

        @Test
        void shouldSetCreationTimestamp() throws JsonProcessingException, JSONException {
            Map extensionMap = objectMapper.readValue(extensionJson, Map.class);
            var extension = new Unstructured(extensionMap);

            var beforeChange = objectMapper.writeValueAsString(extension);

            var metadata = extension.getMetadata();
            metadata.setCreationTimestamp(metadata.getCreationTimestamp());

            var afterChange = objectMapper.writeValueAsString(extension);

            JSONAssert.assertEquals(beforeChange, afterChange, true);
        }

        @Test
        void shouldDeserializeCorrectly() throws JsonProcessingException, JSONException {
            var extension = objectMapper.readValue(extensionJson, Unstructured.class);
            var gotJson = objectMapper.writeValueAsString(extension);
            JSONAssert.assertEquals(extensionJson, gotJson, true);
        }

        @Test
        void shouldGetExtensionCorrectly() throws JsonProcessingException {
            var extension = objectMapper.readValue(extensionJson, Unstructured.class);

            assertEquals("fake.halo.run/v1alpha1", extension.getApiVersion());
            assertEquals("Fake", extension.getKind());
            MetadataOperator.equals(createMetadata(), extension.getMetadata());
        }

        @Test
        void shouldSetExtensionCorrectly() {
            var extension = createUnstructured();

            assertEquals("fake.halo.run/v1alpha1", extension.getApiVersion());
            assertEquals("Fake", extension.getKind());
            assertTrue(MetadataOperator.equals(createMetadata(), extension.getMetadata()));
        }

        @Test
        void shouldBeEqual() {
            assertEquals(new Unstructured(), new Unstructured());
            assertEquals(createUnstructured(), createUnstructured());
        }

        @Test
        void shouldNotBeEqual() {
            var another = createUnstructured();
            another.getMetadata().setName("fake-extension-2");
            assertNotEquals(createUnstructured(), another);
        }

        @Test
        void shouldGetFinalizersCorrectly() throws JsonProcessingException {
            var extension = objectMapper.readValue(extensionJson, Unstructured.class);

            assertEquals(Set.of("finalizer.1", "finalizer.2"),
                extension.getMetadata().getFinalizers());

            extension.getMetadata().setFinalizers(Set.of("finalizer.3", "finalizer.4"));
            assertEquals(Set.of("finalizer.3", "finalizer.4"),
                extension.getMetadata().getFinalizers());
        }

        @Test
        void shouldSetLabelsCorrectly() throws JsonProcessingException {
            var extension = objectMapper.readValue(extensionJson, Unstructured.class);

            assertEquals(Map.of("category", "fake", "default", "true"),
                extension.getMetadata().getLabels());

            extension.getMetadata().setLabels(Map.of("category", "fake", "default", "false"));
            assertEquals(Map.of("category", "fake", "default", "false"),
                extension.getMetadata().getLabels());
        }

        @Test
        void shouldSetAnnotationsCorrectly() throws JsonProcessingException {
            var extension = objectMapper.readValue(extensionJson, Unstructured.class);

            assertNull(extension.getMetadata().getAnnotations());

            extension.getMetadata()
                .setAnnotations(Map.of("annotation1", "value1", "annotation2", "value2"));
            assertEquals(Map.of("annotation1", "value1", "annotation2", "value2"),
                extension.getMetadata().getAnnotations());
        }
    }

    @Nested
    class Jackson3Test {

        JsonMapper objectMapper = Unstructured.jsonMapper();

        @Test
        void shouldSerializeCorrectly() {
            Map extensionMap = objectMapper.readValue(extensionJson, Map.class);
            var extension = new Unstructured(extensionMap);

            var gotNode = objectMapper.valueToTree(extension);
            assertEquals(objectMapper.readTree(extensionJson), gotNode);
        }

        @Test
        void shouldSetCreationTimestamp() throws JSONException {
            Map extensionMap = objectMapper.readValue(extensionJson, Map.class);
            var extension = new Unstructured(extensionMap);

            var beforeChange = objectMapper.writeValueAsString(extension);

            var metadata = extension.getMetadata();
            metadata.setCreationTimestamp(metadata.getCreationTimestamp());

            var afterChange = objectMapper.writeValueAsString(extension);

            JSONAssert.assertEquals(beforeChange, afterChange, true);
        }

        @Test
        void shouldDeserializeCorrectly() throws JSONException {
            var extension = objectMapper.readValue(extensionJson, Unstructured.class);
            var gotJson = objectMapper.writeValueAsString(extension);
            JSONAssert.assertEquals(extensionJson, gotJson, true);
        }

        @Test
        void shouldGetExtensionCorrectly() throws JsonProcessingException {
            var extension = objectMapper.readValue(extensionJson, Unstructured.class);

            assertEquals("fake.halo.run/v1alpha1", extension.getApiVersion());
            assertEquals("Fake", extension.getKind());
            MetadataOperator.equals(createMetadata(), extension.getMetadata());
        }

        @Test
        void shouldSetExtensionCorrectly() {
            var extension = createUnstructured();

            assertEquals("fake.halo.run/v1alpha1", extension.getApiVersion());
            assertEquals("Fake", extension.getKind());
            assertTrue(MetadataOperator.equals(createMetadata(), extension.getMetadata()));
        }

        @Test
        void shouldBeEqual() {
            assertEquals(new Unstructured(), new Unstructured());
            assertEquals(createUnstructured(), createUnstructured());
        }

        @Test
        void shouldNotBeEqual() {
            var another = createUnstructured();
            another.getMetadata().setName("fake-extension-2");
            assertNotEquals(createUnstructured(), another);
        }

        @Test
        void shouldGetFinalizersCorrectly() {
            var extension = objectMapper.readValue(extensionJson, Unstructured.class);

            assertEquals(Set.of("finalizer.1", "finalizer.2"),
                extension.getMetadata().getFinalizers());

            extension.getMetadata().setFinalizers(Set.of("finalizer.3", "finalizer.4"));
            assertEquals(Set.of("finalizer.3", "finalizer.4"),
                extension.getMetadata().getFinalizers());
        }

        @Test
        void shouldSetLabelsCorrectly() {
            var extension = objectMapper.readValue(extensionJson, Unstructured.class);

            assertEquals(Map.of("category", "fake", "default", "true"),
                extension.getMetadata().getLabels());

            extension.getMetadata().setLabels(Map.of("category", "fake", "default", "false"));
            assertEquals(Map.of("category", "fake", "default", "false"),
                extension.getMetadata().getLabels());
        }

        @Test
        void shouldSetAnnotationsCorrectly() {
            var extension = objectMapper.readValue(extensionJson, Unstructured.class);

            assertNull(extension.getMetadata().getAnnotations());

            extension.getMetadata()
                .setAnnotations(Map.of("annotation1", "value1", "annotation2", "value2"));
            assertEquals(Map.of("annotation1", "value1", "annotation2", "value2"),
                extension.getMetadata().getAnnotations());
        }

    }

    String extensionJson = """
        {
            "apiVersion": "fake.halo.run/v1alpha1",
            "kind": "Fake",
            "metadata": {
                "labels": {
                    "category": "fake",
                    "default": "true"
                },
                "name": "fake-extension",
                "creationTimestamp": "2011-12-03T10:15:30Z",
                "version": 12345,
                "finalizers": ["finalizer.1", "finalizer.2"]
            }
        }
        """;

    Unstructured createUnstructured() {
        var unstructured = new Unstructured();
        unstructured.setApiVersion("fake.halo.run/v1alpha1");
        unstructured.setKind("Fake");
        unstructured.setMetadata(createMetadata());
        return unstructured;
    }

    private Metadata createMetadata() {
        var metadata = new Metadata();
        metadata.setName("fake-extension");
        metadata.setLabels(Map.of("category", "fake", "default", "true"));
        metadata.setCreationTimestamp(Instant.parse("2011-12-03T10:15:30Z"));
        metadata.setVersion(12345L);
        return metadata;
    }

}
