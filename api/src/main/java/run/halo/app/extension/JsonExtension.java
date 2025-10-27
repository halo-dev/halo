package run.halo.app.extension;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.LongNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

/**
 * JsonExtension is representation an extension using ObjectNode. This extension is preparing for
 * patching in the future.
 *
 * @author johnniang
 */
@JsonSerialize(using = JsonExtension.ObjectNodeExtensionSerializer.class)
@JsonDeserialize(using = JsonExtension.ObjectNodeExtensionDeSerializer.class)
public class JsonExtension implements Extension {

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder()
        .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
        .build();

    private final ObjectNode objectNode;

    public JsonExtension() {
        this(JSON_MAPPER.createObjectNode());
    }

    public JsonExtension(ObjectNode objectNode) {
        this.objectNode = objectNode;
    }

    public JsonExtension(Extension e) {
        this((ObjectNode) JSON_MAPPER.valueToTree(e));
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
        return apiVersionNode == null ? null : apiVersionNode.asString();
    }

    @Override
    public String getKind() {
        return objectNode.get("kind").asString();
    }

    @Override
    public void setApiVersion(String apiVersion) {
        objectNode.set("apiVersion", new StringNode(apiVersion));
    }

    @Override
    public void setKind(String kind) {
        objectNode.set("kind", new StringNode(kind));
    }

    @Override
    public void setMetadata(MetadataOperator metadata) {
        objectNode.set("metadata", JSON_MAPPER.valueToTree(metadata));
    }

    public static class ObjectNodeExtensionSerializer extends ValueSerializer<JsonExtension> {

        @Override
        public void serialize(JsonExtension value, JsonGenerator gen,
            SerializationContext ctxt) throws JacksonException {
            gen.writeTree(value.objectNode);
        }
    }

    public static class ObjectNodeExtensionDeSerializer
        extends ValueDeserializer<JsonExtension> {

        @Override
        public JsonExtension deserialize(JsonParser p, DeserializationContext ctxt)
            throws JacksonException {
            var treeNode = p.readValueAsTree();
            return new JsonExtension((ObjectNode) treeNode);
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

    /**
     * Get object mapper.
     *
     * @return object mapper
     */
    public static JsonMapper getJsonMapper() {
        return JSON_MAPPER;
    }

    public MetadataOperator getMetadataOrCreate() {
        var metadataNode = JSON_MAPPER.createObjectNode();
        objectNode.set("metadata", metadataNode);
        return new ObjectNodeMetadata(metadataNode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JsonExtension that = (JsonExtension) o;
        return Objects.equals(objectNode, that.objectNode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectNode);
    }

    class ObjectNodeMetadata implements MetadataOperator {

        private final ObjectNode objectNode;

        public ObjectNodeMetadata(ObjectNode objectNode) {
            this.objectNode = objectNode;
        }

        @Override
        public String getName() {
            var nameNode = objectNode.get("name");
            return JSON_MAPPER.convertValue(nameNode, String.class);
        }

        @Override
        public String getGenerateName() {
            var generateNameNode = objectNode.get("generateName");
            return JSON_MAPPER.convertValue(generateNameNode, String.class);
        }

        @Override
        public Map<String, String> getLabels() {
            var labelsNode = objectNode.get("labels");
            return JSON_MAPPER.convertValue(labelsNode, new TypeReference<>() {
            });
        }

        @Override
        public Map<String, String> getAnnotations() {
            var annotationsNode = objectNode.get("annotations");
            return JSON_MAPPER.convertValue(annotationsNode, new TypeReference<>() {
            });
        }

        @Override
        public Long getVersion() {
            var versionNode = objectNode.get("version");
            return JSON_MAPPER.convertValue(versionNode, Long.class);
        }

        @Override
        public Instant getCreationTimestamp() {
            return JSON_MAPPER.convertValue(objectNode.get("creationTimestamp"), Instant.class);
        }

        @Override
        public Instant getDeletionTimestamp() {
            return JSON_MAPPER.convertValue(objectNode.get("deletionTimestamp"), Instant.class);
        }

        @Override
        public Set<String> getFinalizers() {
            return JSON_MAPPER.convertValue(objectNode.get("finalizers"), new TypeReference<>() {
            });
        }

        @Override
        public void setName(String name) {
            if (name != null) {
                objectNode.set("name", StringNode.valueOf(name));
            }
        }

        @Override
        public void setGenerateName(String generateName) {
            if (generateName != null) {
                objectNode.set("generateName", StringNode.valueOf(generateName));
            }
        }

        @Override
        public void setLabels(Map<String, String> labels) {
            if (labels != null) {
                objectNode.set("labels", JSON_MAPPER.valueToTree(labels));
            }
        }

        @Override
        public void setAnnotations(Map<String, String> annotations) {
            if (annotations != null) {
                objectNode.set("annotations", JSON_MAPPER.valueToTree(annotations));
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
                objectNode.set("creationTimestamp", JSON_MAPPER.valueToTree(creationTimestamp));
            }
        }

        @Override
        public void setDeletionTimestamp(Instant deletionTimestamp) {
            if (deletionTimestamp != null) {
                objectNode.set("deletionTimestamp", JSON_MAPPER.valueToTree(deletionTimestamp));
            }
        }

        @Override
        public void setFinalizers(Set<String> finalizers) {
            if (finalizers != null) {
                objectNode.set("finalizers", JSON_MAPPER.valueToTree(finalizers));
            }
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ObjectNodeMetadata that = (ObjectNodeMetadata) o;
            return Objects.equals(objectNode, that.objectNode);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(objectNode);
        }
    }
}
