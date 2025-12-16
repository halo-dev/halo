package run.halo.app.infra.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.HashMap;
import java.util.Map;
import org.springframework.util.CollectionUtils;
import run.halo.app.extension.ConfigMap;

/**
 * Utility class for merging configuration maps containing JSON strings.
 *
 * @author johnniang
 * @since 2.22.0
 */
public enum SystemConfigUtils {
    ;

    private static final ObjectMapper mapper = JsonUtils.mapper();

    /**
     * Merge two configuration maps containing JSON strings.
     *
     * @param defaultMap the default configuration map
     * @param overrideMap the override configuration map
     * @return the merged configuration map
     * @throws JsonProcessingException if JSON processing fails
     */
    public static Map<String, String> mergeMap(
        Map<String, String> defaultMap, Map<String, String> overrideMap
    ) throws JsonProcessingException {
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
     * @throws JsonProcessingException if JSON processing fails
     */
    public static ConfigMap mergeConfigMap(
        ConfigMap defaultConfigMap,
        ConfigMap overrideConfigMap
    ) throws JsonProcessingException {
        var mergedData = mergeMap(
            defaultConfigMap.getData() != null ? defaultConfigMap.getData() : Map.of(),
            overrideConfigMap.getData() != null ? overrideConfigMap.getData() : Map.of()
        );
        var merged = new ConfigMap();
        merged.setMetadata(overrideConfigMap.getMetadata());
        merged.setData(mergedData);
        return merged;
    }

    private static String mergeJsonStrings(String mainJson, String updateJson)
        throws JsonProcessingException {
        var mainNode = mapper.readTree(mainJson);
        var updateNode = mapper.readTree(updateJson);

        // This performs a deep merge into the mainNode
        var mergedNode = deepMerge(mainNode, updateNode);

        return mapper.writeValueAsString(mergedNode);
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
}
