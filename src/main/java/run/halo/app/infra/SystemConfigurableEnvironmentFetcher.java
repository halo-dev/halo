package run.halo.app.infra;

import java.util.Map;
import org.springframework.core.convert.ConversionService;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.utils.JsonUtils;

/**
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

    @NonNull
    private Mono<Map<String, String>> getValuesInternal() {
        return getConfigMap()
            .filter(configMap -> configMap.getData() != null)
            .map(ConfigMap::getData)
            .defaultIfEmpty(Map.of());
    }

    public Mono<ConfigMap> getConfigMap() {
        return extensionClient.fetch(ConfigMap.class, SystemSetting.SYSTEM_CONFIG);
    }
}
