package run.halo.app.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UnstructuredTest {

    ObjectMapper objectMapper;

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

    @BeforeAll
    static void setUpGlobally() {
        Schemes.INSTANCE.register(FakeExtension.class);
    }

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void shouldSerializeCorrectly() throws JsonProcessingException {
        var extensionNode = (ObjectNode) objectMapper.readTree(extensionJson);
        var extension = new Unstructured(extensionNode);

        var gotNode = objectMapper.valueToTree(extension);
        assertEquals(extensionNode, gotNode);
    }

    @Test
    void shouldDeserializeCorrectly() throws JsonProcessingException {
        var extension = objectMapper.readValue(extensionJson, Unstructured.class);
        var wantJsonNode = objectMapper.readTree(extensionJson);
        assertEquals(wantJsonNode, extension.getExtension());
    }

    @Test
    void shouldGetExtensionCorrectly() throws JsonProcessingException {
        var extension = objectMapper.readValue(extensionJson, Unstructured.class);

        assertEquals("fake.halo.run/v1alpha1", extension.getApiVersion());
        assertEquals("Fake", extension.getKind());
        assertEquals(createMetadata(), extension.getMetadata());
    }

    @Test
    void shouldSetExtensionCorrectly() {
        var objectNode = objectMapper.createObjectNode();
        var extension = new Unstructured(objectNode);
        extension.setApiVersion("fake.halo.run/v1alpha1");
        extension.setKind("Fake");
        extension.setMetadata(createMetadata());

        assertEquals("fake.halo.run/v1alpha1", extension.getApiVersion());
        assertEquals("Fake", extension.getKind());
        assertEquals(createMetadata(), extension.getMetadata());
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