package run.halo.app.plugin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import run.halo.app.core.extension.Plugin;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.infra.utils.JsonParseException;
import run.halo.app.infra.utils.JsonUtils;

/**
 * <p>A value fetcher for plugin form configuration.</p>
 *
 * @author guqing
 * @since 2.0.0
 */
public class DefaultSettingFetcher extends SettingFetcher {

    private final ExtensionClient extensionClient;

    private final String pluginName;

    public DefaultSettingFetcher(String pluginName,
        ExtensionClient extensionClient) {
        this.extensionClient = extensionClient;
        this.pluginName = pluginName;
    }

    @NonNull
    @Override
    public <T> Optional<T> fetch(String group, Class<T> clazz) {
        return Optional.ofNullable(convertValue(getInternal(group), clazz));
    }

    @NonNull
    @Override
    public JsonNode get(String group) {
        return getInternal(group);
    }

    /**
     * Get values from {@link ConfigMap}.
     *
     * @return a unmodifiable map of values(non-null).
     */
    @NonNull
    @Override
    public Map<String, JsonNode> getValues() {
        return Map.copyOf(getValuesInternal());
    }

    private JsonNode getInternal(String group) {
        return Optional.ofNullable(getValuesInternal().get(group))
            .orElse(JsonNodeFactory.instance.missingNode());
    }

    private Map<String, JsonNode> getValuesInternal() {
        return configMap(pluginName)
            .filter(configMap -> configMap.getData() != null)
            .map(ConfigMap::getData)
            .map(Map::entrySet)
            .stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> readTree(entry.getValue())));
    }

    private Optional<ConfigMap> configMap(String pluginName) {
        return extensionClient.fetch(Plugin.class, pluginName)
            .flatMap(plugin -> {
                String configMapName = plugin.getSpec().getConfigMapName();
                if (StringUtils.isBlank(configMapName)) {
                    return Optional.empty();
                }
                return extensionClient.fetch(ConfigMap.class, plugin.getSpec().getConfigMapName());
            });
    }

    private JsonNode readTree(String json) {
        if (StringUtils.isBlank(json)) {
            return JsonNodeFactory.instance.missingNode();
        }
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
