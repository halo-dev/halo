package run.halo.app.plugin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Plugin;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.utils.JsonParseException;
import run.halo.app.infra.utils.JsonUtils;

/**
 * A default implementation of {@link ReactiveSettingFetcher}.
 *
 * @author guqing
 * @since 2.0.0
 */
public class DefaultReactiveSettingFetcher implements ReactiveSettingFetcher {

    private final ReactiveExtensionClient client;

    private final String pluginName;

    public DefaultReactiveSettingFetcher(ReactiveExtensionClient client, String pluginName) {
        this.client = client;
        this.pluginName = pluginName;
    }

    @Override
    public <T> Mono<T> fetch(String group, Class<T> clazz) {
        return getInternal(group)
            .mapNotNull(jsonNode -> convertValue(jsonNode, clazz));
    }

    @Override
    @NonNull
    public Mono<JsonNode> get(String group) {
        return getInternal(group)
            .switchIfEmpty(
                Mono.error(new IllegalArgumentException("Group [" + group + "] does not exist."))
            );
    }

    @Override
    @NonNull
    public Mono<Map<String, JsonNode>> getValues() {
        return getValuesInternal()
            .map(Map::copyOf)
            .defaultIfEmpty(Map.of());
    }

    private Mono<JsonNode> getInternal(String group) {
        return getValuesInternal()
            .mapNotNull(values -> values.get(group))
            .defaultIfEmpty(JsonNodeFactory.instance.missingNode());
    }

    private Mono<Map<String, JsonNode>> getValuesInternal() {
        return configMap(pluginName)
            .mapNotNull(ConfigMap::getData)
            .map(data -> {
                Map<String, JsonNode> result = new LinkedHashMap<>();
                data.forEach((key, value) -> result.put(key, readTree(value)));
                return result;
            })
            .defaultIfEmpty(Map.of());
    }

    private Mono<ConfigMap> configMap(String pluginName) {
        return client.fetch(Plugin.class, pluginName)
            .flatMap(plugin -> {
                String configMapName = plugin.getSpec().getConfigMapName();
                if (StringUtils.isBlank(configMapName)) {
                    return Mono.empty();
                }
                return client.fetch(ConfigMap.class, plugin.getSpec().getConfigMapName());
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
