package run.halo.app.plugin;

import java.util.Map;
import reactor.core.publisher.Mono;
import tools.jackson.databind.JsonNode;

/**
 * The {@link ReactiveSettingFetcher} to help plugin fetch own setting configuration.
 *
 * @author guqing
 * @since 2.4.0
 */
public interface ReactiveSettingFetcher {

    <T> Mono<T> fetch(String group, Class<T> clazz);

    @Deprecated(forRemoval = true, since = "2.23.0")
    Mono<com.fasterxml.jackson.databind.JsonNode> get(String group);

    /**
     * Get setting value by group.
     *
     * @param group the setting group
     * @return the setting value or empty if not found
     */
    Mono<JsonNode> getSettingValue(String group);

    @Deprecated(forRemoval = true, since = "2.23.0")
    Mono<Map<String, com.fasterxml.jackson.databind.JsonNode>> getValues();

    /**
     * Get all setting values.
     *
     * @return all setting values, never empty
     */
    Mono<Map<String, JsonNode>> getSettingValues();

}
