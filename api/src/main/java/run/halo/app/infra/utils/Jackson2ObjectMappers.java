package run.halo.app.infra.utils;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Factory of Jackson 2 {@link ObjectMapper}.
 *
 * <p>Halo persists extension data and validates it against JSON schemas as Jackson 2 trees, because openapi4j only
 * supports Jackson 2. Before swagger-core 3.0, Halo borrowed that mapper from
 * {@code io.swagger.v3.core.util.Json#mapper()}, but the swagger-core mappers are Jackson 3 mappers now. So the mapper
 * is defined here explicitly.
 *
 * <p>The configuration mirrors the one swagger-core 2.x applied to its mapper, which is the configuration the persisted
 * extension JSON was written with. Keep it in sync with the JSON in the extension store when changing it.
 *
 * @author johnniang
 * @since 2.28.0
 */
public final class Jackson2ObjectMappers {

    private static final ObjectMapper SHARED = create();

    private Jackson2ObjectMappers() {}

    /**
     * Gets the shared mapper. Never mutate it.
     *
     * @return the shared Jackson 2 mapper
     */
    public static ObjectMapper shared() {
        return SHARED;
    }

    /**
     * Creates a Jackson 2 mapper with the configuration that swagger-core 2.x used to apply.
     *
     * @return a new Jackson 2 mapper
     */
    public static ObjectMapper create() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true)
                .configure(SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN, true)
                .configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setSerializationInclusion(NON_NULL);
    }
}
