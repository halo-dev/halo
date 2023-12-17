package run.halo.app.infra;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.utils.JsonParseException;
import run.halo.app.infra.utils.JsonUtils;

/**
 * A fetcher that fetches the system configuration from the extension client.
 * If there are {@link ConfigMap}s named <code>system-default</code> and <code>system</code> at
 * the same time, the {@link ConfigMap} named system will be json merge patch to
 * {@link ConfigMap} named <code>system-default</code>
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class SystemConfigurableEnvironmentFetcher {
    private final ReactiveExtensionClient extensionClient;
    private final ConversionService conversionService;

    public SystemConfigurableEnvironmentFetcher(ReactiveExtensionClient extensionClient,
        ConversionService conversionService) {
        this.extensionClient = extensionClient;
        this.conversionService = conversionService;
    }

    public <T> Mono<T> fetch(String key, Class<T> type) {
        return getValuesInternal()
            .filter(map -> map.containsKey(key))
            .map(map -> map.get(key))
            .mapNotNull(stringValue -> {
                if (conversionService.canConvert(String.class, type)) {
                    return conversionService.convert(stringValue, type);
                }
                return JsonUtils.jsonToObject(stringValue, type);
            });
    }

    public Mono<SystemSetting.Comment> fetchComment() {
        return fetch(SystemSetting.Comment.GROUP, SystemSetting.Comment.class)
            .switchIfEmpty(Mono.just(new SystemSetting.Comment()));
    }

    public Mono<SystemSetting.Post> fetchPost() {
        return fetch(SystemSetting.Post.GROUP, SystemSetting.Post.class)
            .switchIfEmpty(Mono.just(new SystemSetting.Post()));
    }

    public Mono<SystemSetting.ThemeRouteRules> fetchRouteRules() {
        return fetch(SystemSetting.ThemeRouteRules.GROUP, SystemSetting.ThemeRouteRules.class);
    }

    @NonNull
    private Mono<Map<String, String>> getValuesInternal() {
        return getConfigMap()
            .filter(configMap -> configMap.getData() != null)
            .map(ConfigMap::getData)
            .defaultIfEmpty(Map.of());
    }

    /**
     * Gets config map.
     *
     * @return a new {@link ConfigMap} named <code>system</code> by json merge patch.
     */
    public Mono<ConfigMap> getConfigMap() {
        Mono<ConfigMap> mapMono =
            extensionClient.fetch(ConfigMap.class, SystemSetting.SYSTEM_CONFIG_DEFAULT);
        if (mapMono == null) {
            return Mono.empty();
        }
        return mapMono.flatMap(systemDefault ->
                extensionClient.fetch(ConfigMap.class, SystemSetting.SYSTEM_CONFIG)
                    .map(system -> {
                        Map<String, String> defaultData = systemDefault.getData();
                        Map<String, String> data = system.getData();
                        Map<String, String> mergedData = mergeData(defaultData, data);
                        system.setData(mergedData);
                        return system;
                    })
                    .switchIfEmpty(Mono.just(systemDefault)));
    }

    public Optional<ConfigMap> getConfigMapBlocking() {
        return getConfigMap().blockOptional();
    }

    private Map<String, String> mergeData(Map<String, String> defaultData,
        Map<String, String> data) {
        if (defaultData == null) {
            return data;
        }
        if (data == null) {
            return defaultData;
        }

        Map<String, String> copiedDefault = new LinkedHashMap<>(defaultData);
        // // merge the data map entries into the default map
        data.forEach((group, dataValue) -> {
            // https://www.rfc-editor.org/rfc/rfc7386
            String defaultV = copiedDefault.get(group);
            String newValue;
            if (dataValue == null) {
                if (copiedDefault.containsKey(group)) {
                    newValue = null;
                } else {
                    newValue = defaultV;
                }
            } else {
                newValue = mergeRemappingFunction(dataValue, defaultV);
            }

            if (newValue == null) {
                copiedDefault.remove(group);
            } else {
                copiedDefault.put(group, newValue);
            }
        });
        return copiedDefault;
    }

    String mergeRemappingFunction(String dataV, String defaultV) {
        JsonNode dataJsonValue = nullSafeToJsonNode(dataV);
        // original
        JsonNode defaultJsonValue = nullSafeToJsonNode(defaultV);
        try {
            // patch
            JsonMergePatch jsonMergePatch = JsonMergePatch.fromJson(dataJsonValue);
            // apply patch to original
            JsonNode patchedNode = jsonMergePatch.apply(defaultJsonValue);
            return JsonUtils.objectToJson(patchedNode);
        } catch (JsonPatchException e) {
            throw new JsonParseException(e);
        }
    }

    JsonNode nullSafeToJsonNode(String json) {
        return StringUtils.isBlank(json) ? JsonNodeFactory.instance.nullNode()
            : JsonUtils.jsonToObject(json, JsonNode.class);
    }
}
