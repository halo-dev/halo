package run.halo.app.core.extension.theme;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
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
import run.halo.app.infra.utils.JsonParseException;
import run.halo.app.infra.utils.JsonUtils;

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
                .map(o -> JsonUtils.DEFAULT_JSON_MAPPER.convertValue(o, JsonNode.class))
                .filter(jsonNode -> jsonNode.isObject() && jsonNode.has(NAME_FIELD)
                    && jsonNode.has(VALUE_FIELD))
                .map(jsonNode -> {
                    String name = jsonNode.get(NAME_FIELD).asText();
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
                        Map<String, String> modified = defaultIfNull(configMap.getData(), Map.of());
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
        JsonNode modifiedJson = mapToJsonNode(modified);
        // original
        JsonNode sourceJson = mapToJsonNode(source);
        try {
            // patch
            JsonMergePatch jsonMergePatch = JsonMergePatch.fromJson(modifiedJson);
            // apply patch to original
            JsonNode patchedNode = jsonMergePatch.apply(sourceJson);
            return jsonNodeToStringMap(patchedNode);
        } catch (JsonPatchException e) {
            throw new JsonParseException(e);
        }
    }

    JsonNode mapToJsonNode(Map<String, String> map) {
        ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
        map.forEach((k, v) -> {
            if (isJson(v)) {
                JsonNode value = JsonUtils.jsonToObject(v, JsonNode.class);
                objectNode.set(k, value);
                return;
            }
            objectNode.put(k, v);
        });
        return objectNode;
    }

    Map<String, String> jsonNodeToStringMap(JsonNode node) {
        Map<String, String> stringMap = new LinkedHashMap<>();
        node.fields().forEachRemaining(entry -> {
            String k = entry.getKey();
            JsonNode v = entry.getValue();
            if (v == null || v.isNull() || v.isMissingNode()) {
                stringMap.put(k, null);
                return;
            }
            if (v.isTextual()) {
                stringMap.put(k, v.asText());
                return;
            }
            if (v.isContainerNode()) {
                stringMap.put(k, JsonUtils.objectToJson(v));
                return;
            }
            stringMap.put(k, v.asText());
        });
        return stringMap;
    }

    boolean isJson(String jsonString) {
        try {
            JsonUtils.DEFAULT_JSON_MAPPER.readTree(jsonString);
            return true;
        } catch (JacksonException e) {
            return false;
        }
    }
}
