package run.halo.app.infra;

import java.util.Map;
import java.util.Optional;
import org.springframework.core.convert.ConversionService;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.infra.utils.JsonUtils;

/**
 * @author guqing
 * @since 2.0.0
 */
@Component
public class SystemConfigurableEnvironmentFetcher {
    private static final String SYSTEM_CONFIGMAP_NAME = "system";

    private final ExtensionClient extensionClient;
    private final ConversionService conversionService;

    public SystemConfigurableEnvironmentFetcher(ExtensionClient extensionClient,
        ConversionService conversionService) {
        this.extensionClient = extensionClient;
        this.conversionService = conversionService;
    }

    public <T> Optional<T> fetch(String key, Class<T> type) {
        var stringValue = getInternal(key);
        if (stringValue == null) {
            return Optional.empty();
        }
        if (conversionService.canConvert(String.class, type)) {
            return Optional.ofNullable(conversionService.convert(stringValue, type));
        }
        return Optional.of(JsonUtils.jsonToObject(stringValue, type));
    }

    private String getInternal(String group) {
        return getValuesInternal().get(group);
    }

    @NonNull
    private Map<String, String> getValuesInternal() {
        return extensionClient.fetch(ConfigMap.class, SYSTEM_CONFIGMAP_NAME)
            .filter(configMap -> configMap.getData() != null)
            .map(ConfigMap::getData)
            .orElse(Map.of());
    }
}
