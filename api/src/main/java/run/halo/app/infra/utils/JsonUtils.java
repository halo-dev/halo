package run.halo.app.infra.utils;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.Map;
import org.springframework.util.Assert;

/**
 * Json utilities.
 *
 * @author guqing
 * @see JavaTimeModule
 * @since 2.0.0
 * @deprecated Use {@link tools.jackson.databind.json.JsonMapper} directly instead.
 */
@Deprecated(forRemoval = true, since = "2.23.0")
public class JsonUtils {
    /**
     * Jackson 2 mapper used for extension JSON.
     *
     * <p>The configuration mirrors the one swagger-core 2.x applied to its mapper, because that is the configuration
     * the persisted extension JSON was written with before Halo upgraded to springdoc-openapi 3.2.1. Keep it in sync
     * with the JSON in the extension store when changing it.
     */
    public static final ObjectMapper DEFAULT_JSON_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true)
            .configure(SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN, true)
            .configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setSerializationInclusion(NON_NULL);

    private JsonUtils() {}

    public static ObjectMapper mapper() {
        return DEFAULT_JSON_MAPPER;
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
        return DEFAULT_JSON_MAPPER.convertValue(sourceMap, type);
    }

    /**
     * Converts object to json format.
     *
     * @param source source object must not be null
     * @return json format of the source object
     */
    public static String objectToJson(Object source) {
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
