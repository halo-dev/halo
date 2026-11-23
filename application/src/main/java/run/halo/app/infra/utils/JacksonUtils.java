package run.halo.app.infra.utils;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import java.util.Map;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.StreamWriteFeature;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Jackson 3 JSON utilities for Halo internals.
 *
 * <p>{@link JsonUtils} keeps using Jackson 2 on purpose because it is part of the public plugin API. Internal code
 * should use this class instead, so that Halo itself does not depend on Jackson 2 where the plugin boundary does not
 * require it.
 *
 * <p>The mapper is configured to mirror the JSON contract the deprecated {@link JsonUtils} mapper has been writing
 * with, e.g. omitting {@code null} values and writing enums via {@link Object#toString()}.
 *
 * @since 2.28.0
 */
public enum JacksonUtils {
    ;

    /** Shared mapper for internal JSON processing. */
    public static final JsonMapper MAPPER = JsonMapper.builder()
            .changeDefaultPropertyInclusion(v -> v.withValueInclusion(NON_NULL).withContentInclusion(NON_NULL))
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(EnumFeature.WRITE_ENUMS_USING_TO_STRING)
            .enable(MapperFeature.USE_GETTERS_AS_SETTERS)
            .enable(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS)
            .enable(StreamWriteFeature.WRITE_BIGDECIMAL_AS_PLAIN)
            .build();

    /**
     * Converts object to JSON format.
     *
     * @param source source object must not be null
     * @return JSON format of the source object
     */
    public static String objectToJson(Object source) {
        return MAPPER.writeValueAsString(source);
    }

    /**
     * Deserializes JSON content into the given type.
     *
     * @param json JSON content
     * @param toValueType object type to convert
     * @param <T> real type to convert
     * @return converted object
     */
    public static <T> T jsonToObject(String json, Class<T> toValueType) {
        return MAPPER.readValue(json, toValueType);
    }

    /**
     * Deserializes JSON content into the given type reference.
     *
     * @param json JSON content
     * @param typeReference type reference to convert
     * @param <T> real type to convert
     * @return converted object
     */
    public static <T> T jsonToObject(String json, TypeReference<T> typeReference) {
        return MAPPER.readValue(json, typeReference);
    }

    /**
     * Converts a map to the object specified type.
     *
     * @param sourceMap source map must not be empty
     * @param type object type must not be null
     * @param <T> target object type
     * @return the object specified type
     */
    public static <T> T mapToObject(Map<String, ?> sourceMap, Class<T> type) {
        return MAPPER.convertValue(sourceMap, type);
    }

    /**
     * Creates a deep copy of the source object via JSON.
     *
     * @param source source object to copy
     * @param <T> real type to deep copy
     * @return deep copy of the source object
     */
    @SuppressWarnings("unchecked")
    public static <T> T deepCopy(T source) {
        return (T) MAPPER.readValue(objectToJson(source), source.getClass());
    }

    /**
     * Applies an RFC 7386 JSON Merge Patch to the given target.
     *
     * <p>Object members are merged recursively, {@code null} removes the member and any other value replaces the target
     * member. The target is not modified.
     *
     * @param target the document to patch, may be null
     * @param patch the merge patch
     * @return the patched document
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc7386">RFC 7386</a>
     */
    public static JsonNode mergePatch(@Nullable JsonNode target, JsonNode patch) {
        if (!patch.isObject()) {
            return patch.deepCopy();
        }
        var result = target != null && target.isObject() ? ((ObjectNode) target).deepCopy() : MAPPER.createObjectNode();
        patch.properties().forEach(entry -> {
            var name = entry.getKey();
            var value = entry.getValue();
            if (value.isNull()) {
                result.remove(name);
                return;
            }
            if (value.isObject()) {
                result.set(name, mergePatch(result.get(name), value));
                return;
            }
            result.set(name, value.deepCopy());
        });
        return result;
    }
}
