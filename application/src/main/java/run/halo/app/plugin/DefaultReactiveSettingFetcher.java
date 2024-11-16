package run.halo.app.plugin;

import static run.halo.app.extension.index.query.QueryFactory.equal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ExtensionMatcher;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.infra.utils.JsonUtils;

/**
 * A default implementation of {@link ReactiveSettingFetcher}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
public class DefaultReactiveSettingFetcher
    implements ReactiveSettingFetcher, Reconciler<Reconciler.Request>, DisposableBean,
    ApplicationContextAware {

    private final ReactiveExtensionClient client;

    private final ExtensionClient blockingClient;

    private final CacheManager cacheManager;

    /**
     * The application context of the plugin.
     */
    private ApplicationContext applicationContext;

    private final String pluginName;

    private final String configMapName;

    private final String cacheName;

    public DefaultReactiveSettingFetcher(PluginContext pluginContext,
        ReactiveExtensionClient client, ExtensionClient blockingClient,
        CacheManager cacheManager) {
        this.client = client;
        this.pluginName = pluginContext.getName();
        this.configMapName = pluginContext.getConfigMapName();
        this.blockingClient = blockingClient;
        this.cacheManager = cacheManager;
        this.cacheName = buildCacheKey(pluginName);
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

    Mono<Map<String, JsonNode>> getValuesInternal() {
        var cache = getCache();
        var cachedValue = getCachedConfigData(cache);
        if (cachedValue != null) {
            return Mono.justOrEmpty(cachedValue);
        }
        return Mono.defer(() -> {
            // double check
            var newCachedValue = getCachedConfigData(cache);
            if (newCachedValue != null) {
                return Mono.justOrEmpty(newCachedValue);
            }
            if (StringUtils.isBlank(configMapName)) {
                return Mono.empty();
            }
            return client.fetch(ConfigMap.class, configMapName)
                .mapNotNull(ConfigMap::getData)
                .map(data -> {
                    Map<String, JsonNode> result = new LinkedHashMap<>();
                    data.forEach((key, value) -> result.put(key, readTree(value)));
                    return result;
                })
                .defaultIfEmpty(Map.of())
                .doOnNext(values -> cache.put(pluginName, values));
        });
    }

    private JsonNode readTree(String json) {
        if (StringUtils.isBlank(json)) {
            return JsonNodeFactory.instance.missingNode();
        }
        try {
            return JsonUtils.DEFAULT_JSON_MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            // ignore
            log.error("Failed to parse plugin [{}] config json: [{}]", pluginName, json, e);
        }
        return JsonNodeFactory.instance.missingNode();
    }

    private <T> T convertValue(JsonNode jsonNode, Class<T> clazz) {
        try {
            return JsonUtils.DEFAULT_JSON_MAPPER.convertValue(jsonNode, clazz);
        } catch (IllegalArgumentException e) {
            // ignore
            log.error("Failed to convert plugin [{}] configMap [{}] to class [{}]",
                pluginName, configMapName, clazz, e);
        }
        return null;
    }

    @NonNull
    private Cache getCache() {
        var cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            // should never happen
            throw new IllegalStateException("Cache [" + cacheName + "] not found.");
        }
        return cache;
    }

    static String buildCacheKey(String pluginName) {
        return "plugin-" + pluginName + "-configmap";
    }

    @Override
    public Result reconcile(Request request) {
        blockingClient.fetch(ConfigMap.class, configMapName)
            .ifPresent(configMap -> {
                var cache = getCache();
                var existData = getCachedConfigData(cache);
                var configMapData = configMap.getData();
                Map<String, JsonNode> result = new LinkedHashMap<>();
                if (configMapData != null) {
                    configMapData.forEach((key, value) -> result.put(key, readTree(value)));
                }
                // update cache
                cache.put(pluginName, result);
                applicationContext.publishEvent(PluginConfigUpdatedEvent.builder()
                    .source(this)
                    .oldConfig(existData)
                    .newConfig(result)
                    .build());
            });
        return Result.doNotRetry();
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private Map<String, JsonNode> getCachedConfigData(@NonNull Cache cache) {
        var existData = cache.get(pluginName);
        if (existData == null) {
            return null;
        }
        return (Map<String, JsonNode>) existData.get();
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        ExtensionMatcher matcher =
            extension -> Objects.equals(extension.getMetadata().getName(), configMapName);
        return builder
            .extension(new ConfigMap())
            .syncAllOnStart(true)
            .syncAllListOptions(ListOptions.builder()
                .fieldQuery(equal("metadata.name", configMapName))
                .build())
            .onAddMatcher(matcher)
            .onUpdateMatcher(matcher)
            .onDeleteMatcher(matcher)
            .build();
    }

    @Override
    public void destroy() {
        getCache().invalidate();
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext)
        throws BeansException {
        this.applicationContext = applicationContext;
    }
}
