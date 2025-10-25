package run.halo.app.infra.utils;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.util.Json;
import java.util.Map;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/**
 * Json utilities.
 *
 * @author guqing
 * @since 2.0.0
 */
public class JsonUtils {

    /**
     * @deprecated Use {@link #jsonMapper()} instead.
     */
    @Deprecated(forRemoval = true, since = "2.22.0")
    public static final ObjectMapper DEFAULT_JSON_MAPPER = Json.mapper();

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder()
        .changeDefaultPropertyInclusion(
            value -> value.withValueInclusion(NON_NULL).withContentInclusion(NON_NULL)
        )
        .build();

    private JsonUtils() {
    }

    /**
     * @deprecated Use {@link #jsonMapper()} instead.
     */
    @Deprecated(forRemoval = true, since = "2.22.0")
    public static ObjectMapper mapper() {
        return DEFAULT_JSON_MAPPER;
    }

    public static JsonMapper jsonMapper() {
        return JSON_MAPPER;
    }

    /**
     * Converts a map to the object specified type.
     *
     * @param sourceMap source map must not be empty
     * @param type object type must not be null
     * @param <T> target object type
     * @return the object specified type
     */
    @NonNull
    public static <T> T mapToObject(@NonNull Map<String, ?> sourceMap, @NonNull Class<T> type) {
        return JSON_MAPPER.convertValue(sourceMap, type);
    }

    /**
     * Converts object to json format.
     *
     * @param source source object must not be null
     * @return json format of the source object
     */
    @NonNull
    public static String objectToJson(@NonNull Object source) {
        Assert.notNull(source, "Source object must not be null");
        try {
            return JSON_MAPPER.writeValueAsString(source);
        } catch (JacksonException e) {
            throw new JsonParseException(e);
        }
    }

    /**
     * Method to deserialize JSON content from given JSON content String.
     *
     * @param json json content
     * @param toValueType object type to convert
     * @param <T> real type to convert
     * @return converted object
     */
    public static <T> T jsonToObject(String json, Class<T> toValueType) {
        return JSON_MAPPER.readValue(json, toValueType);
    }

    /**
     * Method to deserialize JSON content from given JSON content String.
     *
     * @param json json content
     * @param typeReference type reference to convert
     * @param <T> real type to convert
     * @return converted object
     */
    public static <T> T jsonToObject(String json, TypeReference<T> typeReference) {
        return JSON_MAPPER.readValue(json, typeReference);
    }

    /**
     * Method to deserialize JSON content from given JSON content String.
     *
     * @param json json content
     * @param typeReference type reference to convert
     * @param <T> real type to convert
     * @return converted object
     */
    public static <T> T jsonToObject(String json,
        com.fasterxml.jackson.core.type.TypeReference<T> typeReference) {
        try {
            return mapper().readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new JsonParseException(e);
        }
    }

    /**
     * Method to deserialize JSON content and serialize back from given Object.
     *
     * @param source source object to copy
     * @param <T> real type to deep copy
     * @return deep copy of the source object
     */
    @SuppressWarnings("unchecked")
    public static <T> T deepCopy(T source) {
        return (T) JSON_MAPPER.readValue(objectToJson(source), source.getClass());
    }
}
