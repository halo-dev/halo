package run.halo.app.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

class JsonExtensionTest {

    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder().build();
    }

    @Test
    void serializeEmptyExt() throws JsonProcessingException, JSONException {
        var ext = new JsonExtension(objectMapper);
        var json = objectMapper.writeValueAsString(ext);
        JSONAssert.assertEquals("{}", json, true);
    }

    @Test
    void serializeExt() throws JsonProcessingException, JSONException {
        var ext = new JsonExtension(objectMapper);
        ext.setApiVersion("fake.halo.run/v1alpha");
        ext.setKind("Fake");
        var metadata = ext.getMetadataOrCreate();
        metadata.setName("fake-name");

        ext.getInternal().set("data", TextNode.valueOf("halo"));

        JSONAssert.assertEquals("""
            {
              "apiVersion": "fake.halo.run/v1alpha",
              "kind": "Fake",
              "metadata": {
                "name": "fake-name"
              },
              "data": "halo"
            }""", objectMapper.writeValueAsString(ext), true);
    }

    @Test
    void deserialize() throws JsonProcessingException {
        var json = """
            {
              "apiVersion": "fake.halo.run/v1alpha1",
              "kind": "Fake",
              "metadata": {
                "name": "faker"
              },
              "otherProperty": "otherPropertyValue"
            }""";

        var ext = objectMapper.readValue(json, JsonExtension.class);

        assertEquals("fake.halo.run/v1alpha1", ext.getApiVersion());
        assertEquals("Fake", ext.getKind());
        assertNotNull(ext.getMetadata());
        assertEquals("faker", ext.getMetadata().getName());
        assertNull(ext.getMetadata().getVersion());
        assertNull(ext.getMetadata().getFinalizers());
        assertNull(ext.getMetadata().getAnnotations());
        assertNull(ext.getMetadata().getLabels());
        assertNull(ext.getMetadata().getGenerateName());
        assertNull(ext.getMetadata().getCreationTimestamp());
        assertNull(ext.getMetadata().getDeletionTimestamp());
        assertEquals("otherPropertyValue", ext.getInternal().get("otherProperty").asText());
    }
}