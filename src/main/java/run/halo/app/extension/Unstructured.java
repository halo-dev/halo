package run.halo.app.extension;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;

/**
 * Unstructured is a generic Extension, which wraps ObjectNode to maintain the Extension data, like
 * apiVersion, kind, metadata and others.
 *
 * @author johnniang
 */
@JsonSerialize(using = Unstructured.UnstructuredSerializer.class)
@JsonDeserialize(using = Unstructured.UnstructuredDeserializer.class)
public class Unstructured implements Extension {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

    private final ObjectNode extension;

    public Unstructured() {
        this(OBJECT_MAPPER.createObjectNode());
    }

    public Unstructured(ObjectNode extension) {
        this.extension = extension;
    }

    @Override
    public String getApiVersion() {
        return extension.get("apiVersion").asText();
    }

    @Override
    public String getKind() {
        return extension.get("kind").asText();
    }

    @Override
    public MetadataOperator getMetadata() {
        var metaMap = extension.get("metadata");
        return OBJECT_MAPPER.convertValue(metaMap, Metadata.class);
    }

    @Override
    public void setApiVersion(String apiVersion) {
        extension.put("apiVersion", apiVersion);
    }

    @Override
    public void setKind(String kind) {
        extension.put("kind", kind);
    }

    @Override
    public void setMetadata(MetadataOperator metadata) {
        JsonNode metaNode = OBJECT_MAPPER.valueToTree(metadata);
        extension.set("metadata", metaNode);
    }

    ObjectNode getExtension() {
        return extension;
    }

    // TODO Add other convenient methods here to set and get nested fields in the future.

    public static class UnstructuredSerializer extends JsonSerializer<Unstructured> {

        @Override
        public void serialize(Unstructured value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
            gen.writeTree(value.extension);
        }

    }

    public static class UnstructuredDeserializer extends JsonDeserializer<Unstructured> {

        @Override
        public Unstructured deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {
            return new Unstructured(p.getCodec().readTree(p));
        }
    }

}
