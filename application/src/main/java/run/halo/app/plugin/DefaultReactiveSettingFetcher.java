package run.halo.app.plugin;

import static run.halo.app.extension.index.query.Queries.equal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ExtensionMatcher;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.infra.utils.ReactiveUtils;
import run.halo.app.infra.utils.SystemConfigUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * A default implementation of {@link ReactiveSettingFetcher}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
class DefaultReactiveSettingFetcher
    implements ReactiveSettingFetcher, Reconciler<Reconciler.Request>, ApplicationContextAware {

    private static final Duration TIMEOUT = ReactiveUtils.DEFAULT_TIMEOUT;

    private final JsonMapper mapper = JsonMapper.builder().build();

    private final ObjectMapper v2Mapper = JsonUtils.mapper();

    private final ReactiveExtensionClient client;

    private final Mono<ConfigMap> configMapCache;

    private final AtomicBoolean isCacheInvalidated = new AtomicBoolean(false);

    /**
     * The application context of the plugin.
     */
    private ApplicationContext applicationContext;

    private final String pluginName;

    private final String configMapName;

    public DefaultReactiveSettingFetcher(
        PluginContext pluginContext, ReactiveExtensionClient client
    ) {
        this.client = client;
        this.pluginName = pluginContext.getName();
        this.configMapName = pluginContext.getConfigMapName();
        this.configMapCache = Mono.defer(() -> {
            if (StringUtils.isBlank(configMapName)) {
                return Mono.empty();
            }
            return client.fetch(ConfigMap.class, configMapName);
        }).cacheInvalidateIf(cm -> isCacheInvalidated.getAndSet(false));
    }

    @Override
    public <T> Mono<T> fetch(String group, Class<T> clazz) {
        return getSettingValue(group)
            .map(n -> mapper.convertValue(n, clazz));
    }

    @Override
    public Mono<JsonNode> get(String group) {
        return getValues().mapNotNull(m -> m.get(group))
            .switchIfEmpty(Mono.fromSupplier(v2Mapper::createObjectNode));
    }

    @Override
    public Mono<tools.jackson.databind.JsonNode> getSettingValue(String group) {
        return getSettingValues().mapNotNull(m -> m.get(group));
    }

    @Override
    public Mono<Map<String, JsonNode>> getValues() {
        return configMapCache.mapNotNull(ConfigMap::getData)
            .map(this::toJackson2JsonNodeMap)
            .defaultIfEmpty(Map.of());
    }

    @Override
    public Mono<Map<String, tools.jackson.databind.JsonNode>> getSettingValues() {
        return configMapCache.mapNotNull(ConfigMap::getData)
            .map(this::toJackson3JsonNodeMap)
            .defaultIfEmpty(Map.of());
    }

    private Map<String, tools.jackson.databind.JsonNode> toJackson3JsonNodeMap(
        @Nullable Map<String, String> data
    ) {
        if (data == null) {
            return Map.of();
        }
        return data.entrySet().stream().collect(Collectors.toMap(
            Map.Entry::getKey,
            e -> toJackson3JsonNode(e.getValue())
        ));
    }

    private Map<String, JsonNode> toJackson2JsonNodeMap(@Nullable Map<String, String> data) {
        if (data == null) {
            return Map.of();
        }
        return data.entrySet().stream().collect(Collectors.toMap(
            Map.Entry::getKey,
            e -> toJackson2JsonNode(e.getValue())
        ));
    }

    private tools.jackson.databind.JsonNode toJackson3JsonNode(
        @Nullable String json) {
        if (StringUtils.isBlank(json)) {
            return mapper.missingNode();
        }
        try {
            return mapper.readTree(json);
        } catch (JacksonException ex) {
            log.error("Failed to parse plugin [{}] config json: [{}]", pluginName, json, ex);
            return mapper.missingNode();
        }
    }

    private JsonNode toJackson2JsonNode(String json) {
        if (StringUtils.isBlank(json)) {
            return v2Mapper.missingNode();
        }
        try {
            return v2Mapper.readTree(json);
        } catch (JsonProcessingException e) {
            // ignore
            log.error("Failed to parse plugin [{}] config json: [{}]", pluginName, json, e);
        }
        return JsonNodeFactory.instance.missingNode();
    }

    static String buildCacheKey(String pluginName) {
        return "plugin-" + pluginName + "-configmap";
    }

    @Override
    public Result reconcile(Request request) {
        return client.fetch(ConfigMap.class, configMapName)
            .filter(Predicate.not(ExtensionUtil::isDeleted))
            .flatMap(cm -> {
                // get data snapshot
                var snapshot = SystemConfigUtils.getDataSnapshot(cm);
                if (SystemConfigUtils.populateChecksum(cm)) {
                    // if config map is changed
                    SystemConfigUtils.updateDataSnapshot(cm);
                    return client.update(cm).then(Mono.fromCallable(() -> {
                        this.isCacheInvalidated.set(true);
                        applicationContext.publishEvent(PluginConfigUpdatedEvent.builder()
                            .source(this)
                            .oldConfig(toJackson2JsonNodeMap(snapshot))
                            .newConfig(toJackson2JsonNodeMap(cm.getData()))
                            .oldSettingValues(toJackson3JsonNodeMap(snapshot))
                            .newSettingValues(toJackson3JsonNodeMap(cm.getData()))
                            .build());
                        return null;
                    }));
                }
                return Mono.<Result>empty();
            }).blockOptional(TIMEOUT).orElse(null);
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        if (StringUtils.isBlank(configMapName)) {
            // Disable the controller if the config map name is not set
            return builder
                .extension(new ConfigMap())
                .syncAllOnStart(false)
                .onAddMatcher(extension -> false)
                .onUpdateMatcher(extension -> false)
                .onDeleteMatcher(extension -> false)
                .build();
        }
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
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
