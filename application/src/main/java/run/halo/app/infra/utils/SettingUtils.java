package run.halo.app.infra.utils;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import run.halo.app.core.extension.Setting;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.controller.Reconciler.Result;
import run.halo.app.extension.controller.RequeueException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

public enum SettingUtils {
    ;

    private static final JsonMapper MAPPER = JsonMapper.builder()
        .changeDefaultPropertyInclusion(v ->
            v.withValueInclusion(NON_NULL).withContentInclusion(NON_NULL)
        )
        .build();

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
                .map(o -> MAPPER.convertValue(o, JsonNode.class))
                .filter(jsonNode -> jsonNode.isObject() && jsonNode.has(NAME_FIELD)
                    && jsonNode.has(VALUE_FIELD))
                .map(jsonNode -> {
                    String name = jsonNode.get(NAME_FIELD).asString();
                    JsonNode value = jsonNode.get(VALUE_FIELD);
                    return Map.entry(name, value);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            data.put(group, MAPPER.writeValueAsString(groupValue));
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
            .ifPresentOrElse(setting -> {
                final var source = SettingUtils.settingDefinedDefaultValueMap(setting);
                client.fetch(ConfigMap.class, configMapName)
                    .ifPresentOrElse(configMap -> {
                        Map<String, String> modified =
                            Objects.requireNonNullElse(configMap.getData(), Map.of());
                        var copy = new HashMap<>(modified);

                        var merged = SettingUtils.mergePatch(modified, source);
                        configMap.setData(merged);

                        if (!Objects.equals(copy, configMap.getData())) {
                            client.update(configMap);
                        }
                    }, () -> {
                        ConfigMap configMap = new ConfigMap();
                        configMap.setMetadata(new Metadata());
                        configMap.getMetadata().setName(configMapName);
                        configMap.setData(source);
                        client.create(configMap);
                    });
            }, () -> {
                // requeue if setting was not found
                throw new RequeueException(Result.requeue(null),
                    "Theme setting %s was not found".formatted(settingName)
                );
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
        var modifiedJson = mapToJsonNode(modified);
        // original
        var sourceJson = mapToJsonNode(source);
        try {
            // patch
            var jsonMergePatch = JsonMergePatch.fromJson(modifiedJson);
            // apply patch to original
            var patchedNode = jsonMergePatch.apply(sourceJson);
            return jsonNodeToStringMap(patchedNode);
        } catch (JsonPatchException e) {
            throw new JsonParseException(e);
        }
    }

    /**
     * Convert {@link Setting} related configMap data to JsonNode.
     *
     * @param configMap {@link ConfigMap} instance
     * @return JsonNode
     */
    public static ObjectNode settingConfigToJson(ConfigMap configMap) {
        if (configMap.getData() == null) {
            return MAPPER.createObjectNode();
        }
        return mapToObjectNode(configMap.getData());
    }

    /**
     * Convert the result of {@link #settingConfigToJson(ConfigMap)} in reverse to Map.
     *
     * @param node JsonNode object
     * @return {@link ConfigMap#getData()}
     */
    public static Map<String, String> settingConfigJsonToMap(ObjectNode node) {
        return jsonNodeToStringMap(node);
    }

    /**
     * Convert {@code Map<String, String>} to
     * {@link com.fasterxml.jackson.databind.node.ObjectNode}.
     *
     * @param map source map
     * @return ObjectNode
     */
    private static com.fasterxml.jackson.databind.node.ObjectNode mapToJsonNode(
        Map<String, String> map) {
        var objectNode = JsonUtils.mapper().createObjectNode();
        map.forEach((k, v) -> {
            if (v == null) {
                objectNode.putNull(k);
                return;
            }
            try {
                var value = JsonUtils.mapper().readTree(v);
                objectNode.set(k, value);
            } catch (JsonProcessingException ignored) {
                // ignore exception and put as text
                objectNode.put(k, v);
            }
        });
        return objectNode;
    }

    private static ObjectNode mapToObjectNode(Map<String, String> map) {
        var objectNode = MAPPER.createObjectNode();
        map.forEach((k, v) -> {
            if (v == null) {
                objectNode.putNull(k);
                return;
            }
            try {
                var value = MAPPER.readTree(v);
                objectNode.set(k, value);
            } catch (JacksonException ignored) {
                // ignore exception and put as text
                objectNode.put(k, v);
            }
        });
        return objectNode;
    }

    private static Map<String, String> jsonNodeToStringMap(
        com.fasterxml.jackson.databind.JsonNode node
    ) {
        Map<String, String> stringMap = new LinkedHashMap<>();
        node.forEachEntry((k, v) -> {
            if (v == null || v.isNull() || v.isMissingNode()) {
                stringMap.put(k, null);
                return;
            }
            if (v.isTextual()) {
                stringMap.put(k, v.asText());
                return;
            }
            if (v.isContainerNode()) {
                stringMap.put(k, v.toString());
                return;
            }
            stringMap.put(k, v.asText());
        });
        return stringMap;
    }

    private static Map<String, String> jsonNodeToStringMap(JsonNode node) {
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
                stringMap.put(k, v.toString());
                return;
            }
            stringMap.put(k, v.asString());
        });
        return stringMap;
    }

}
