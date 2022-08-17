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
    private static final String SYSTEM_CONFIGMAP_NAME = "system";

    private final ReactiveExtensionClient extensionClient;
    private final ConversionService conversionService;

    public SystemConfigurableEnvironmentFetcher(ReactiveExtensionClient extensionClient,
        ConversionService conversionService) {
        this.extensionClient = extensionClient;
        this.conversionService = conversionService;
    }

    public <T> Mono<T> fetch(String key, Class<T> type) {
        return getValuesInternal().map(map -> map.get(key))
            .map(stringValue -> {
                if (conversionService.canConvert(String.class, type)) {
                    return conversionService.convert(stringValue, type);
                }
                return JsonUtils.jsonToObject(stringValue, type);
            });
    }

    @NonNull
    private Mono<Map<String, String>> getValuesInternal() {
        return extensionClient.fetch(ConfigMap.class, SYSTEM_CONFIGMAP_NAME)
            .filter(configMap -> configMap.getData() != null)
            .map(ConfigMap::getData)
            .defaultIfEmpty(Map.of());
    }
}
