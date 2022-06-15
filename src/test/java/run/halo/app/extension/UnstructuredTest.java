package run.halo.app.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static run.halo.app.extension.MetadataOperator.metadataDeepEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Map;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

class UnstructuredTest {

    ObjectMapper objectMapper = Unstructured.OBJECT_MAPPER;

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
                "version": 12345
            }
        }
        """;

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

        System.out.println(objectMapper.writeValueAsString(extension));
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
        metadataDeepEquals(createMetadata(), extension.getMetadata());
    }

    @Test
    void shouldSetExtensionCorrectly() {
        var extension = new Unstructured();
        extension.setApiVersion("fake.halo.run/v1alpha1");
        extension.setKind("Fake");
        extension.setMetadata(createMetadata());

        assertEquals("fake.halo.run/v1alpha1", extension.getApiVersion());
        assertEquals("Fake", extension.getKind());
        assertTrue(metadataDeepEquals(createMetadata(), extension.getMetadata()));
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
