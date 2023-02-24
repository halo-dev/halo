package run.halo.app.extension;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

/**
 * JsonExtension is representation an extension using ObjectNode. This extension is preparing for
 * patching in the future.
 *
 * @author johnniang
 */
@JsonSerialize(using = JsonExtension.ObjectNodeExtensionSerializer.class)
@JsonDeserialize(using = JsonExtension.ObjectNodeExtensionDeSerializer.class)
class JsonExtension implements Extension {

    private final ObjectMapper objectMapper;

    private final ObjectNode objectNode;

    public JsonExtension(ObjectMapper objectMapper) {
        this(objectMapper, objectMapper.createObjectNode());
    }

    public JsonExtension(ObjectMapper objectMapper, ObjectNode objectNode) {
        this.objectMapper = objectMapper;
        this.objectNode = objectNode;
    }

    public JsonExtension(ObjectMapper objectMapper, Extension e) {
        this(objectMapper, (ObjectNode) objectMapper.valueToTree(e));
    }

    @Override
    public MetadataOperator getMetadata() {
        var metadataNode = objectNode.get("metadata");
        if (metadataNode == null) {
            return null;
        }
        return new ObjectNodeMetadata((ObjectNode) metadataNode);
    }

    @Override
    public String getApiVersion() {
        var apiVersionNode = objectNode.get("apiVersion");
        return apiVersionNode == null ? null : apiVersionNode.asText();
    }

    @Override
    public String getKind() {
        return objectNode.get("kind").asText();
    }

    @Override
    public void setApiVersion(String apiVersion) {
        objectNode.set("apiVersion", new TextNode(apiVersion));
    }

    @Override
    public void setKind(String kind) {
        objectNode.set("kind", new TextNode(kind));
    }

    @Override
    public void setMetadata(MetadataOperator metadata) {
        objectNode.set("metadata", objectMapper.valueToTree(metadata));
    }

    public static class ObjectNodeExtensionSerializer extends JsonSerializer<JsonExtension> {

        @Override
        public void serialize(JsonExtension value, JsonGenerator gen,
            SerializerProvider serializers) throws IOException {
            gen.writeTree(value.objectNode);
        }
    }

    public static class ObjectNodeExtensionDeSerializer
        extends JsonDeserializer<JsonExtension> {

        @Override
        public JsonExtension deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {
            var mapper = (ObjectMapper) p.getCodec();
            var treeNode = mapper.readTree(p);
            return new JsonExtension(mapper, (ObjectNode) treeNode);
        }
    }

    /**
     * Get internal representation.
     *
     * @return internal representation
     */
    public ObjectNode getInternal() {
        return objectNode;
    }

    public MetadataOperator getMetadataOrCreate() {
        var metadataNode = objectMapper.createObjectNode();
        objectNode.set("metadata", metadataNode);
        return new ObjectNodeMetadata(metadataNode);
    }

    class ObjectNodeMetadata implements MetadataOperator {

        private final ObjectNode objectNode;

        public ObjectNodeMetadata(ObjectNode objectNode) {
            this.objectNode = objectNode;
        }

        @Override
        public String getName() {
            var nameNode = objectNode.get("name");
            return objectMapper.convertValue(nameNode, String.class);
        }

        @Override
        public String getGenerateName() {
            var generateNameNode = objectNode.get("generateName");
            return objectMapper.convertValue(generateNameNode, String.class);
        }

        @Override
        public Map<String, String> getLabels() {
            var labelsNode = objectNode.get("labels");
            return objectMapper.convertValue(labelsNode, new TypeReference<>() {
            });
        }

        @Override
        public Map<String, String> getAnnotations() {
            var annotationsNode = objectNode.get("annotations");
            return objectMapper.convertValue(annotationsNode, new TypeReference<>() {
            });
        }

        @Override
        public Long getVersion() {
            JsonNode versionNode = objectNode.get("version");
            return objectMapper.convertValue(versionNode, Long.class);
        }

        @Override
        public Instant getCreationTimestamp() {
            return objectMapper.convertValue(objectNode.get("creationTimestamp"), Instant.class);
        }

        @Override
        public Instant getDeletionTimestamp() {
            return objectMapper.convertValue(objectNode.get("deletionTimestamp"), Instant.class);
        }

        @Override
        public Set<String> getFinalizers() {
            return objectMapper.convertValue(objectNode.get("finalizers"), new TypeReference<>() {
            });
        }

        @Override
        public void setName(String name) {
            if (name != null) {
                objectNode.set("name", TextNode.valueOf(name));
            }
        }

        @Override
        public void setGenerateName(String generateName) {
            if (generateName != null) {
                objectNode.set("generateName", TextNode.valueOf(generateName));
            }
        }

        @Override
        public void setLabels(Map<String, String> labels) {
            if (labels != null) {
                objectNode.set("labels", objectMapper.valueToTree(labels));
            }
        }

        @Override
        public void setAnnotations(Map<String, String> annotations) {
            if (annotations != null) {
                objectNode.set("annotations", objectMapper.valueToTree(annotations));
            }
        }

        @Override
        public void setVersion(Long version) {
            if (version != null) {
                objectNode.set("version", LongNode.valueOf(version));
            }
        }

        @Override
        public void setCreationTimestamp(Instant creationTimestamp) {
            if (creationTimestamp != null) {
                objectNode.set("creationTimestamp", objectMapper.valueToTree(creationTimestamp));
            }
        }

        @Override
        public void setDeletionTimestamp(Instant deletionTimestamp) {
            if (deletionTimestamp != null) {
                objectNode.set("deletionTimestamp", objectMapper.valueToTree(deletionTimestamp));
            }
        }

        @Override
        public void setFinalizers(Set<String> finalizers) {
            if (finalizers != null) {
                objectNode.set("finalizers", objectMapper.valueToTree(finalizers));
            }
        }
    }
}
