package run.halo.app.infra.utils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.core.extension.Setting;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Metadata;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

@UtilityClass
public class SettingUtils {
    private static final String VALUE_FIELD = "value";
    private static final String NAME_FIELD = "name";

    /**
     * Read setting default value from {@link Setting} forms.
     *
     * @param setting {@link Setting} extension
     * @return a map of setting default value
     */
    @NonNull
    public static Map<String, String> settingDefinedDefaultValueMap(Setting setting) {
        List<Setting.SettingForm> forms = setting.getSpec().getForms();
        if (CollectionUtils.isEmpty(forms)) {
            return Map.of();
        }
        Map<String, String> data = new LinkedHashMap<>();
        for (Setting.SettingForm form : forms) {
            String group = form.getGroup();
            Map<String, JsonNode> groupValue = form.getFormSchema().stream()
                .map(o -> JsonUtils.jsonMapper().convertValue(o, JsonNode.class))
                .filter(jsonNode -> jsonNode.isObject() && jsonNode.has(NAME_FIELD)
                    && jsonNode.has(VALUE_FIELD))
                .map(jsonNode -> {
                    String name = jsonNode.get(NAME_FIELD).asString();
                    JsonNode value = jsonNode.get(VALUE_FIELD);
                    return Map.entry(name, value);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            data.put(group, JsonUtils.objectToJson(groupValue));
        }
        return data;
    }

    /**
     * Create or update config map by provided setting name and configMapName.
     *
     * @param client extension client
     * @param settingName a name for {@link Setting}
     * @param configMapName a name for {@link ConfigMap}
     */
    public static void createOrUpdateConfigMap(ExtensionClient client, String settingName,
        String configMapName) {
        Assert.notNull(client, "Extension client must not be null");
        Assert.hasText(settingName, "Setting name must not be blank");
        Assert.hasText(configMapName, "Config map name must not be blank");

        client.fetch(Setting.class, settingName)
            .ifPresent(setting -> {
                final var source = SettingUtils.settingDefinedDefaultValueMap(setting);
                client.fetch(ConfigMap.class, configMapName)
                    .ifPresentOrElse(configMap -> {
                        Map<String, String> modified =
                            Objects.requireNonNullElse(configMap.getData(), Map.of());
                        final var oldData = JsonUtils.deepCopy(modified);

                        Map<String, String> merged = SettingUtils.mergePatch(modified, source);
                        configMap.setData(merged);

                        if (!Objects.equals(oldData, configMap.getData())) {
                            client.update(configMap);
                        }
                    }, () -> {
                        ConfigMap configMap = new ConfigMap();
                        configMap.setMetadata(new Metadata());
                        configMap.getMetadata().setName(configMapName);
                        configMap.setData(source);
                        client.create(configMap);
                    });
            });
    }

    public static ConfigMap populateDefaultConfig(Setting setting, String configMapName) {
        var data = settingDefinedDefaultValueMap(setting);
        ConfigMap configMap = new ConfigMap();
        configMap.setMetadata(new Metadata());
        configMap.getMetadata().setName(configMapName);
        configMap.setData(data);
        return configMap;
    }

    /**
     * Construct a JsonMergePatch from a difference between two Maps and apply patch to
     * {@code source}.
     *
     * @param modified the modified object
     * @param source the source object
     * @return patched map object
     */
    public static Map<String, String> mergePatch(Map<String, String> modified,
        Map<String, String> source) {
        var existingNode = mapToJsonNode(source);
        var modifiedNode = mapToJsonNode(modified);
        var or = JsonUtils.jsonMapper().readerForUpdating(existingNode);
        return jsonNodeToStringMap(or.readValue(modifiedNode));
    }

    /**
     * Convert {@link Setting} related configMap data to JsonNode.
     *
     * @param configMap {@link ConfigMap} instance
     * @return JsonNode
     */
    public static ObjectNode settingConfigToJson(ConfigMap configMap) {
        if (configMap.getData() == null) {
            return JsonNodeFactory.instance.objectNode();
        }
        return mapToJsonNode(configMap.getData());
    }

    /**
     * Convert the result of {@link #settingConfigToJson(ConfigMap)} in reverse to Map.
     *
     * @param node JsonNode object
     * @return {@link ConfigMap#getData()}
     */
    public static Map<String, String> settingConfigJsonToMap(
        tools.jackson.databind.node.ObjectNode node) {
        return jsonNodeToStringMap(node);
    }

    ObjectNode mapToJsonNode(Map<String, String> map) {
        var objectNode = JsonUtils.jsonMapper().createObjectNode();
        map.forEach((k, v) -> {
            try {
                var value = JsonUtils.jsonMapper().readTree(v);
                objectNode.set(k, value);
                return;
            } catch (JacksonException ignored) {
                // ignored this error
            }
            objectNode.put(k, v);
        });
        return objectNode;
    }

    Map<String, String> jsonNodeToStringMap(JsonNode node) {
        Map<String, String> stringMap = new LinkedHashMap<>();
        node.forEachEntry((k, v) -> {
            if (v == null || v.isNull() || v.isMissingNode()) {
                stringMap.put(k, null);
                return;
            }
            if (v.isString()) {
                stringMap.put(k, v.asString());
                return;
            }
            if (v.isContainer()) {
                stringMap.put(k, JsonUtils.objectToJson(v));
                return;
            }
            stringMap.put(k, v.asString());
        });
        return stringMap;
    }

    boolean isJson(String jsonString) {
        try {
            JsonUtils.jsonMapper().readTree(jsonString);
            return true;
        } catch (JacksonException e) {
            return false;
        }
    }
}
