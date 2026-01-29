package run.halo.app.plugin;

import java.util.Map;
import java.util.Optional;
import run.halo.app.extension.ConfigMap;
import tools.jackson.databind.JsonNode;

/**
 * SettingFetcher must be a class instead of an interface due to backward compatibility.
 *
 * @author johnniang
 */
public interface SettingFetcher {

    <T> Optional<T> fetch(String group, Class<T> clazz);

    /**
     * Get values from {@link ConfigMap} by group.
     *
     * @param group the setting group
     * @return the setting value(non-null)
     * @deprecated use {@link #getSettingValue(String)} instead
     */
    @Deprecated(forRemoval = true, since = "2.23.0")
    com.fasterxml.jackson.databind.JsonNode get(String group);

    /**
     * Get setting value by group.
     *
     * @param group the setting group
     * @return the setting value(non-null)
     */
    JsonNode getSettingValue(String group);

    /**
     * Get values from {@link ConfigMap}.
     *
     * @return a unmodifiable map of values(non-null)
     * @deprecated use {@link #getSettingValues()} instead
     */
    @Deprecated(forRemoval = true, since = "2.23.0")
    Map<String, com.fasterxml.jackson.databind.JsonNode> getValues();

    /**
     * Get all setting values.
     *
     * @return all setting values, never null
     */
    Map<String, JsonNode> getSettingValues();

}
