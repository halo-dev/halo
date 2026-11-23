package run.halo.app.infra.utils;

import com.google.common.hash.Hashing;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import run.halo.app.core.extension.content.Constant;
import run.halo.app.extension.ConfigMap;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Utility class for merging configuration maps containing JSON strings.
 *
 * @author johnniang
 * @since 2.22.0
 */
@Slf4j
public enum SystemConfigUtils {
    ;

    private static final String DATA_SNAPSHOT_ANNO = "halo.run/data-snapshot";

    /**
     * Merge two configuration maps containing JSON strings.
     *
     * @param defaultMap the default configuration map
     * @param overrideMap the override configuration map
     * @return the merged configuration map
     */
    public static Map<String, String> mergeMap(Map<String, String> defaultMap, Map<String, String> overrideMap) {
        if (CollectionUtils.isEmpty(defaultMap)) {
            return overrideMap;
        }
        if (CollectionUtils.isEmpty(overrideMap)) {
            return defaultMap;
        }
        var result = new HashMap<>(defaultMap);
        for (Map.Entry<String, String> entry : overrideMap.entrySet()) {
            var group = entry.getKey();
            var overrideJson = entry.getValue();
            if (result.containsKey(group)) {
                // Perform a deep merge of the two JSON strings
                String defaultJson = result.get(group);
                result.put(group, mergeJsonStrings(defaultJson, overrideJson));
            } else {
                // Key only exists in override map
                result.put(group, overrideJson);
            }
        }
        return result;
    }

    /**
     * Compute the merged ConfigMap from default and override ConfigMaps.
     *
     * @param defaultConfigMap the default ConfigMap
     * @param overrideConfigMap the override ConfigMap
     * @return the merged ConfigMap
     */
    public static ConfigMap mergeConfigMap(ConfigMap defaultConfigMap, ConfigMap overrideConfigMap) {
        var mergedData = mergeMap(
                defaultConfigMap.getData() != null ? defaultConfigMap.getData() : Map.of(),
                overrideConfigMap.getData() != null ? overrideConfigMap.getData() : Map.of());
        var merged = new ConfigMap();
        merged.setMetadata(overrideConfigMap.getMetadata());
        merged.setData(mergedData);
        return merged;
    }

    private static String mergeJsonStrings(String mainJson, String updateJson) {
        var mainNode = JacksonUtils.MAPPER.readTree(mainJson);
        var updateNode = JacksonUtils.MAPPER.readTree(updateJson);

        // This performs a deep merge into the mainNode
        var mergedNode = deepMerge(mainNode, updateNode);

        return JacksonUtils.MAPPER.writeValueAsString(mergedNode);
    }

    private static JsonNode deepMerge(JsonNode mainNode, JsonNode updateNode) {
        // If they aren't both objects, the update simply replaces the main
        if (!mainNode.isObject() || !updateNode.isObject()) {
            return updateNode;
        }
        var mainObject = (ObjectNode) mainNode;
        updateNode.properties().forEach(entry -> {
            String key = entry.getKey();
            JsonNode value = entry.getValue();

            if (mainObject.has(key)) {
                mainObject.set(key, deepMerge(mainObject.get(key), value));
            } else {
                mainObject.set(key, value);
            }
        });
        return mainObject;
    }

    /**
     * Populate checksum annotation in the ConfigMap.
     *
     * @param configMap the ConfigMap to populate checksum for
     * @return true if the checksum was updated, false otherwise
     */
    public static boolean populateChecksum(ConfigMap configMap) {
        var toHash =
                Optional.ofNullable(configMap.getData()).map(Objects::toString).orElse("");
        var checksum =
                Hashing.sha256().hashString(toHash, StandardCharsets.UTF_8).toString();
        var metadata = configMap.getMetadata();
        var notChanged = Optional.ofNullable(metadata.getAnnotations())
                .map(annotations -> annotations.get(Constant.CHECKSUM_CONFIG_ANNO))
                .stream()
                .anyMatch(existingChecksum -> Objects.equals(checksum, existingChecksum));
        if (notChanged) {
            log.debug("ConfigMap '{}' has not changed.", configMap.getMetadata().getName());
            return false;
        }
        log.debug(
                "ConfigMap '{}' has changed, updating checksum {}.",
                configMap.getMetadata().getName(),
                checksum);
        if (metadata.getAnnotations() == null) {
            metadata.setAnnotations(new HashMap<>());
        }
        metadata.getAnnotations().put(Constant.CHECKSUM_CONFIG_ANNO, checksum);
        return true;
    }

    /**
     * Update data snapshot annotation in the ConfigMap.
     *
     * @param configMap the ConfigMap
     */
    public static void updateDataSnapshot(ConfigMap configMap) {
        Optional.ofNullable(configMap.getData())
                .map(JacksonUtils.MAPPER::writeValueAsString)
                .ifPresent(dataJson -> {
                    var metadata = configMap.getMetadata();
                    if (metadata.getAnnotations() == null) {
                        metadata.setAnnotations(new HashMap<>());
                    }
                    metadata.getAnnotations().put(DATA_SNAPSHOT_ANNO, dataJson);
                });
    }

    /**
     * Get data snapshot from ConfigMap annotations.
     *
     * @param configMap the ConfigMap
     * @return the data snapshot
     */
    public static Map<String, String> getDataSnapshot(ConfigMap configMap) {
        return Optional.ofNullable(configMap.getMetadata().getAnnotations())
                .map(annotations -> annotations.get(DATA_SNAPSHOT_ANNO))
                .map(dataJson -> JacksonUtils.MAPPER.readValue(dataJson, new TypeReference<Map<String, String>>() {}))
                .orElse(Map.of());
    }
}
