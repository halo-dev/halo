package run.halo.app.infra.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.v3.core.util.Json;
import java.util.Map;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

/**
 * Json utilities.
 *
 * @author guqing
 * @see JavaTimeModule
 * @since 2.0.0
 */
public class JsonUtils {
    public static final ObjectMapper DEFAULT_JSON_MAPPER = Json.mapper();

    private JsonUtils() {
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
        return DEFAULT_JSON_MAPPER.convertValue(sourceMap, type);
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
            return DEFAULT_JSON_MAPPER.writeValueAsString(source);
        } catch (JsonProcessingException e) {
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
        try {
            return DEFAULT_JSON_MAPPER.readValue(json, toValueType);
        } catch (Exception e) {
            throw new JsonParseException(e);
        }
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
        try {
            return DEFAULT_JSON_MAPPER.readValue(json, typeReference);
        } catch (Exception e) {
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
        try {
            return (T) DEFAULT_JSON_MAPPER.readValue(objectToJson(source), source.getClass());
        } catch (JsonProcessingException e) {
            throw new JsonParseException(e);
        }
    }
}
