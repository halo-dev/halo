package run.halo.app.plugin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import run.halo.app.core.extension.Plugin;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.infra.utils.JsonParseException;
import run.halo.app.infra.utils.JsonUtils;

/**
 * <p>A value fetcher for pPlugin form configuration.</p>
 * <p>The method of obtaining values is lazy,only when it is used for the first time can the real
 * query value be obtained from {@link ConfigMap}.</p>
 * <p>It is thread safe.</p>
 *
 * @author guqing
 * @since 2.0.0
 */
public class SettingFetcher {

    private static final String PLUGIN_SETTING_VALUE = "setting";

    private final AtomicReference<JsonNode> valueRef = new AtomicReference<>(null);

    private final ExtensionClient extensionClient;

    private final String pluginName;

    public SettingFetcher(String pluginName,
        ExtensionClient extensionClient) {
        this.extensionClient = extensionClient;
        this.pluginName = pluginName;
    }

    @Nullable
    public <T> T getGroupForObject(String group, Class<T> clazz) {
        return convertValue(getInternal(group), clazz);
    }

    @NonNull
    public JsonNode getGroup(String group) {
        return getInternal(group);
    }

    @NonNull
    public JsonNode getValues() {
        return valueRef.updateAndGet(m -> m != null ? m : getValuesInternal());
    }

    private JsonNode getInternal(String group) {
        return Optional.ofNullable(getValues().get(group))
            .orElse(JsonNodeFactory.instance.missingNode());
    }

    private JsonNode getValuesInternal() {
        return configMap(pluginName)
            .filter(configMap -> configMap.getData() != null
                && configMap.getData().containsKey(PLUGIN_SETTING_VALUE))
            .map(configMap -> configMap.getData().get(PLUGIN_SETTING_VALUE))
            .map(this::readTree)
            .orElse(JsonNodeFactory.instance.missingNode());
    }

    private Optional<ConfigMap> configMap(String pluginName) {
        return extensionClient.fetch(Plugin.class, pluginName)
            .flatMap(plugin -> {
                String configMapName = plugin.getSpec().getConfigmapName();
                if (StringUtils.isBlank(configMapName)) {
                    return Optional.empty();
                }
                return extensionClient.fetch(ConfigMap.class, plugin.getSpec().getConfigmapName());
            });
    }

    private JsonNode readTree(String json) {
        try {
            return JsonUtils.DEFAULT_JSON_MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            throw new JsonParseException(e);
        }
    }

    private <T> T convertValue(JsonNode jsonNode, Class<T> clazz) {
        return JsonUtils.DEFAULT_JSON_MAPPER.convertValue(jsonNode, clazz);
    }
}
