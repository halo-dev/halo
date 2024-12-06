package run.halo.app.infra;

import static run.halo.app.extension.index.query.QueryFactory.equal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ExtensionMatcher;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
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
public class SystemConfigurableEnvironmentFetcher implements Reconciler<Reconciler.Request> {
    private final ReactiveExtensionClient extensionClient;
    private final ConversionService conversionService;
    private final AtomicReference<ConfigMap> configMapCache = new AtomicReference<>();

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

    public Mono<ConfigMap> getConfigMap() {
        return Mono.fromSupplier(configMapCache::get)
            .switchIfEmpty(Mono.defer(this::loadConfigMapInternal));
    }

    /**
     * Load the system config map from the extension client.
     *
     * @return latest configMap from {@link ReactiveExtensionClient} without any cache.
     */
    public Mono<ConfigMap> loadConfigMap() {
        return loadConfigMapInternal();
    }

    /**
     * Gets the system config map without any cache.
     *
     * @return load configMap from {@link ReactiveExtensionClient}
     */
    public Optional<ConfigMap> loadConfigMapBlocking() {
        return loadConfigMapInternal().blockOptional();
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

    private String mergeRemappingFunction(String dataV, String defaultV) {
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

    private JsonNode nullSafeToJsonNode(String json) {
        return StringUtils.isBlank(json) ? JsonNodeFactory.instance.nullNode()
            : JsonUtils.jsonToObject(json, JsonNode.class);
    }

    @Override
    public Result reconcile(Request request) {
        loadConfigMapInternal()
            // should never happen
            .switchIfEmpty(Mono.error(new IllegalStateException("System configMap not found.")))
            .doOnNext(configMapCache::set)
            .block();
        return Result.doNotRetry();
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        ExtensionMatcher matcher = extension -> Objects.equals(extension.getMetadata().getName(),
            SystemSetting.SYSTEM_CONFIG);
        return builder
            .extension(new ConfigMap())
            .syncAllOnStart(true)
            .syncAllListOptions(ListOptions.builder()
                .fieldQuery(equal("metadata.name", SystemSetting.SYSTEM_CONFIG))
                .build())
            .onAddMatcher(matcher)
            .onUpdateMatcher(matcher)
            .onDeleteMatcher(matcher)
            .build();
    }

    /**
     * Gets config map.
     *
     * @return a new {@link ConfigMap} named <code>system</code> by json merge patch.
     */
    private Mono<ConfigMap> loadConfigMapInternal() {
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
}
