package run.halo.app.infra.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.util.Assert;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.Version;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.exc.JsonNodeException;
import tools.jackson.databind.module.SimpleModule;

/**
 * Jackson module to adapt legacy {@link JsonNode} serialization and deserialization.
 * This module makes sure the plugin system using legacy Jackson can correctly
 *
 * @author johnniang
 * @since 2.23.0
 */
class JacksonAdapterModule extends SimpleModule {

    private final ObjectProvider<ObjectMapper> objectMapper;

    JacksonAdapterModule(ObjectProvider<ObjectMapper> objectMapper) {
        super(JacksonAdapterModule.class.getSimpleName(), new Version(1, 0, 0, null, null, null));
        this.objectMapper = objectMapper;
        addSerializer(new JsonNodeSerializer());
        addDeserializer(JsonNode.class, new JsonNodeDeserializer<>(JsonNode.class));
        addDeserializer(ObjectNode.class, new JsonNodeDeserializer<>(ObjectNode.class));
    }

    static class JsonNodeSerializer extends ValueSerializer<JsonNode> {

        @Override
        public void serialize(JsonNode value, JsonGenerator gen,
            SerializationContext ctxt)
            throws JacksonException {
            gen.writeRawValue(value.toString());
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
            var mapper = objectMapper.getIfAvailable();
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
