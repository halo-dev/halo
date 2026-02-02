package run.halo.app.infra.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jsonpatch.JsonPatch;
import java.util.function.Supplier;
import org.springframework.util.Assert;
import run.halo.app.extension.JsonExtension;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.Version;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.exc.JsonNodeException;
import tools.jackson.databind.module.SimpleModule;

/**
 * Jackson module to adapt legacy {@link JsonNode} serialization and deserialization.
 * This module makes sure the plugin system using legacy Jackson can correctly
 *
 * @author johnniang
 * @since 2.23.0
 */
public class JacksonAdapterModule extends SimpleModule {

    private final Supplier<ObjectMapper> objectMapper;

    public JacksonAdapterModule(Supplier<ObjectMapper> objectMapper) {
        super(JacksonAdapterModule.class.getSimpleName(), new Version(1, 0, 0, null, null, null));
        this.objectMapper = objectMapper;
        addSerializer(new JsonNodeSerializer());
        addSerializer(new JsonPatchSerializer());
        addSerializer(new JsonExtensionSerializer());

        addDeserializer(JsonNode.class, new JsonNodeDeserializer<>(JsonNode.class));
        addDeserializer(ObjectNode.class, new JsonNodeDeserializer<>(ObjectNode.class));
        addDeserializer(JsonExtension.class, new JsonExtensionDeSerializer());
    }

    class JsonExtensionDeSerializer extends ValueDeserializer<JsonExtension> {

        @Override
        public JsonExtension deserialize(JsonParser p,
            DeserializationContext ctxt) throws JacksonException {
            var json = p.readValueAsTree().toString();
            try {
                return objectMapper.get().readValue(json, JsonExtension.class);
            } catch (JsonProcessingException e) {
                throw InvalidFormatException.from(p, "Failed to deserialize JsonExtension");
            }
        }

    }

    class JsonExtensionSerializer extends ValueSerializer<JsonExtension> {

        @Override
        public void serialize(JsonExtension value, JsonGenerator gen,
            SerializationContext ctxt) throws JacksonException {
            try {
                var raw = objectMapper.get().writeValueAsString(value);
                gen.writeRawValue(raw);
            } catch (JsonProcessingException e) {
                throw InvalidFormatException.from(gen, "Failed to serialize JsonExtension");
            }
        }

        @Override
        public Class<?> handledType() {
            return JsonExtension.class;
        }
    }

    class JsonPatchSerializer extends ValueSerializer<JsonPatch> {

        @Override
        public void serialize(JsonPatch value, JsonGenerator gen,
            SerializationContext ctxt)
            throws JacksonException {
            try {
                gen.writeRawValue(objectMapper.get().writeValueAsString(value));
            } catch (JsonProcessingException e) {
                throw InvalidFormatException.from(gen, "Failed to serialize JsonPatch");
            }
        }

        @Override
        public Class<?> handledType() {
            return JsonPatch.class;
        }

    }

    class JsonNodeSerializer extends ValueSerializer<JsonNode> {

        @Override
        public void serialize(JsonNode value, JsonGenerator gen,
            SerializationContext ctxt)
            throws JacksonException {
            try {
                gen.writeRawValue(objectMapper.get().writeValueAsString(value));
            } catch (JsonProcessingException e) {
                throw InvalidFormatException.from(gen, "Failed to serialize JsonNode");
            }
        }

        @Override
        public Class<?> handledType() {
            return JsonNode.class;
        }

    }

    class JsonNodeDeserializer<T extends JsonNode>
        extends ValueDeserializer<T> {

        private final Class<T> type;

        JsonNodeDeserializer(Class<T> type) {
            this.type = type;
        }

        @Override
        public T deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
            var json = p.readValueAsTree().toString();
            var mapper = objectMapper.get();
            Assert.notNull(mapper, "Legacy ObjectMapper must not be null");
            try {
                return mapper.readValue(json, type);
            } catch (JsonProcessingException e) {
                throw JsonNodeException.from(
                    p, "Failed to bridge legacy JSON node", e
                );
            }
        }
    }

}
