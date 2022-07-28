package run.halo.app.infra;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.infra.utils.JsonParseException;
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

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T get(SystemSetting systemSetting) {
        Class<?> valueType = systemSetting.getValueType();
        String value = getInternal(systemSetting.getGroup());
        if (value == null) {
            return null;
        }
        if (valueType.isPrimitive()) {
            return (T) conversionService.convert(value, valueType);
        }
        return (T) JsonUtils.jsonToObject(value, valueType);
    }

    private String getInternal(String group) {
        return getValuesInternal().get(group);
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

    @NonNull
    private Map<String, String> getValuesInternal() {
        return extensionClient.fetch(ConfigMap.class, SYSTEM_CONFIGMAP_NAME)
            .filter(configMap -> configMap.getData() != null)
            .map(ConfigMap::getData)
            .orElse(Map.of());
    }
}
