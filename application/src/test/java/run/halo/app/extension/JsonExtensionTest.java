package run.halo.app.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.StringNode;

class JsonExtensionTest {

    JsonMapper objectMapper = JsonMapper.shared();

    @Test
    void serializeEmptyExt() {
        var ext = new JsonExtension();
        var json = objectMapper.writeValueAsString(ext);
        JSONAssert.assertEquals("{}", json, true);
    }

    @Test
    void serializeExt() {
        var ext = new JsonExtension();
        ext.setApiVersion("fake.halo.run/v1alpha");
        ext.setKind("Fake");
        var metadata = ext.getMetadataOrCreate();
        metadata.setName("fake-name");

        ext.getInternal().set("data", StringNode.valueOf("halo"));

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
    void deserialize() {
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