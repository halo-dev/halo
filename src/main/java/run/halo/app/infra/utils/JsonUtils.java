package run.halo.app.infra.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.Map;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Json utilities.
 *
 * @author guqing
 * @see JavaTimeModule
 * @since 2.0.0
 */
public class JsonUtils {
    public static final ObjectMapper DEFAULT_JSON_MAPPER = createDefaultJsonMapper();

    private JsonUtils() {
    }

    /**
     * Creates a default json mapper.
     *
     * @return object mapper
     */
    public static ObjectMapper createDefaultJsonMapper() {
        return createDefaultJsonMapper(null);
    }

    /**
     * Creates a default json mapper.
     *
     * @param strategy property naming strategy
     * @return object mapper
     */
    @NonNull
    public static ObjectMapper createDefaultJsonMapper(@Nullable PropertyNamingStrategy strategy) {
        // Create object mapper
        ObjectMapper mapper = new ObjectMapper();
        // Configure
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new JavaTimeModule());
        // Set property naming strategy
        if (strategy != null) {
            mapper.setPropertyNamingStrategy(strategy);
        }
        return mapper;
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
     * @throws JsonProcessingException throws when fail to convert
     */
    @NonNull
    public static String objectToJson(@NonNull Object source)
        throws JsonProcessingException {
        Assert.notNull(source, "Source object must not be null");

        return DEFAULT_JSON_MAPPER.writeValueAsString(source);
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
            throw new RuntimeException(e);
        }
    }
}
