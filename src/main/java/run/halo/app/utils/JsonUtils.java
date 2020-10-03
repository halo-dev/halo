package run.halo.app.utils;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.Map;

/**
 * Json utilities.
 *
 * @author johnniang
 * @date 3/18/19
 */
public class JsonUtils {

    /**
     * Default json mapper.
     */
    public final static ObjectMapper DEFAULT_JSON_MAPPER = createDefaultJsonMapper();

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
        // Set property naming strategy
        if (strategy != null) {
            mapper.setPropertyNamingStrategy(strategy);
        }
        return mapper;
    }

    /**
     * Converts json to the object specified type.
     *
     * @param json json content must not be blank
     * @param type object type must not be null
     * @param <T>  target object type
     * @return object specified type
     * @throws IOException throws when fail to convert
     */
    @NonNull
    public static <T> T jsonToObject(@NonNull String json, @NonNull Class<T> type) throws IOException {
        return jsonToObject(json, type, DEFAULT_JSON_MAPPER);
    }

    /**
     * Converts json to the object specified type.
     *
     * @param json         json content must not be blank
     * @param type         object type must not be null
     * @param objectMapper object mapper must not be null
     * @param <T>          target object type
     * @return object specified type
     * @throws IOException throws when fail to convert
     */
    @NonNull
    public static <T> T jsonToObject(@NonNull String json, @NonNull Class<T> type, @NonNull ObjectMapper objectMapper) throws IOException {
        Assert.hasText(json, "Json content must not be blank");
        Assert.notNull(type, "Target type must not be null");
        Assert.notNull(objectMapper, "Object mapper must not null");

        return objectMapper.readValue(json, type);
    }

    //    /**
    //     * Converts input stream to object specified type.
    //     *
    //     * @param inputStream input stream must not be null
    //     * @param type        object type must not be null
    //     * @param <T>         target object type
    //     * @return object specified type
    //     * @throws IOException throws when fail to convert
    //     */
    //    @NonNull
    //    public static <T> T inputStreamToObject(@NonNull InputStream inputStream, @NonNull Class<T> type) throws IOException {
    //        return inputStreamToObject(inputStream, type, null);
    //    }
    //
    //    /**
    //     * Converts input stream to object specified type.
    //     *
    //     * @param inputStream  input stream must not be null
    //     * @param type         object type must not be null
    //     * @param objectMapper object mapper must not be null
    //     * @param <T>          target object type
    //     * @return object specified type
    //     * @throws IOException throws when fail to convert
    //     */
    //    @NonNull
    //    public static <T> T inputStreamToObject(@NonNull InputStream inputStream, @NonNull Class<T> type, @NonNull ObjectMapper objectMapper) throws IOException {
    //        Assert.notNull(inputStream, "Input stream must not be null");
    //
    //        String json = IOUtils.toString(inputStream);
    //        return jsonToObject(json, type, objectMapper);
    //    }

    /**
     * Converts object to json format.
     *
     * @param source source object must not be null
     * @return json format of the source object
     * @throws JsonProcessingException throws when fail to convert
     */
    @NonNull
    public static String objectToJson(@NonNull Object source) throws JsonProcessingException {
        return objectToJson(source, DEFAULT_JSON_MAPPER);
    }

    /**
     * Converts object to json format.
     *
     * @param source       source object must not be null
     * @param objectMapper object mapper must not be null
     * @return json format of the source object
     * @throws JsonProcessingException throws when fail to convert
     */
    @NonNull
    public static String objectToJson(@NonNull Object source, @NonNull ObjectMapper objectMapper) throws JsonProcessingException {
        Assert.notNull(source, "Source object must not be null");
        Assert.notNull(objectMapper, "Object mapper must not null");

        return objectMapper.writeValueAsString(source);
    }

    /**
     * Converts a map to the object specified type.
     *
     * @param sourceMap source map must not be empty
     * @param type      object type must not be null
     * @param <T>       target object type
     * @return the object specified type
     * @throws IOException throws when fail to convert
     */
    @NonNull
    public static <T> T mapToObject(@NonNull Map<String, ?> sourceMap, @NonNull Class<T> type) throws IOException {
        return mapToObject(sourceMap, type, DEFAULT_JSON_MAPPER);
    }

    /**
     * Converts a map to the object specified type.
     *
     * @param sourceMap    source map must not be empty
     * @param type         object type must not be null
     * @param objectMapper object mapper must not be null
     * @param <T>          target object type
     * @return the object specified type
     * @throws IOException throws when fail to convert
     */
    @NonNull
    public static <T> T mapToObject(@NonNull Map<String, ?> sourceMap, @NonNull Class<T> type, @NonNull ObjectMapper objectMapper) throws IOException {
        Assert.notEmpty(sourceMap, "Source map must not be empty");

        // Serialize the map
        String json = objectToJson(sourceMap, objectMapper);

        // Deserialize the json format of the map
        return jsonToObject(json, type, objectMapper);
    }

    /**
     * Converts a source object to a map
     *
     * @param source source object must not be null
     * @return a map
     * @throws IOException throws when fail to convert
     */
    @NonNull
    public static Map<?, ?> objectToMap(@NonNull Object source) throws IOException {
        return objectToMap(source, DEFAULT_JSON_MAPPER);
    }

    /**
     * Converts a source object to a map
     *
     * @param source       source object must not be null
     * @param objectMapper object mapper must not be null
     * @return a map
     * @throws IOException throws when fail to convert
     */
    @NonNull
    public static Map<?, ?> objectToMap(@NonNull Object source, @NonNull ObjectMapper objectMapper) throws IOException {

        // Serialize the source object
        String json = objectToJson(source, objectMapper);

        // Deserialize the json
        return jsonToObject(json, Map.class, objectMapper);
    }

}
