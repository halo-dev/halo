package run.halo.app.core.extension.theme;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;
import run.halo.app.core.extension.Setting;
import run.halo.app.infra.utils.JsonUtils;

@UtilityClass
public class SettingUtils {
    private static final String VALUE_FIELD = "value";
    private static final String NAME_FIELD = "name";

    /**
     * Read setting default value from {@link Setting} forms.
     *
     * @param setting {@link Setting} extension
     * @return a map of setting default value
     */
    @NonNull
    public static Map<String, String> settingDefinedDefaultValueMap(Setting setting) {
        List<Setting.SettingForm> forms = setting.getSpec().getForms();
        if (CollectionUtils.isEmpty(forms)) {
            return Map.of();
        }
        Map<String, String> data = new LinkedHashMap<>();
        for (Setting.SettingForm form : forms) {
            String group = form.getGroup();
            Map<String, JsonNode> groupValue = form.getFormSchema().stream()
                .map(o -> JsonUtils.DEFAULT_JSON_MAPPER.convertValue(o, JsonNode.class))
                .filter(jsonNode -> jsonNode.isObject() && jsonNode.has(NAME_FIELD)
                    && jsonNode.has(VALUE_FIELD))
                .map(jsonNode -> {
                    String name = jsonNode.get(NAME_FIELD).asText();
                    JsonNode value = jsonNode.get(VALUE_FIELD);
                    return Map.entry(name, value);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            data.put(group, JsonUtils.objectToJson(groupValue));
        }
        return data;
    }
}
