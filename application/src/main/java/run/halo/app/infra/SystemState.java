package run.halo.app.infra;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;
import org.springframework.lang.NonNull;
import run.halo.app.extension.ConfigMap;
import run.halo.app.infra.utils.JsonParseException;
import run.halo.app.infra.utils.JsonUtils;

/**
 * A model for system state deserialize from {@link run.halo.app.extension.ConfigMap}
 * named {@code system-states}.
 *
 * @author guqing
 * @since 2.8.0
 */
@Data
public class SystemState {
    public static final String SYSTEM_STATES_CONFIGMAP = "system-states";

    static final String GROUP = "states";

    private Boolean isSetup;

    /**
     * Deserialize from {@link ConfigMap}.
     *
     * @return config map
     */
    public static SystemState deserialize(@NonNull ConfigMap configMap) {
        Map<String, String> data = configMap.getData();
        if (data == null) {
            return new SystemState();
        }
        return JsonUtils.jsonToObject(data.getOrDefault(GROUP, emptyJsonObject()),
            SystemState.class);
    }

    /**
     * Update modified system state to config map.
     *
     * @param systemState modified system state
     * @param configMap config map
     */
    public static void update(@NonNull SystemState systemState, @NonNull ConfigMap configMap) {
        Map<String, String> data = configMap.getData();
        if (data == null) {
            data = new LinkedHashMap<>();
            configMap.setData(data);
        }
        JsonNode modifiedJson = JsonUtils.mapper()
            .convertValue(systemState, JsonNode.class);
        // original
        JsonNode sourceJson =
            JsonUtils.jsonToObject(data.getOrDefault(GROUP, emptyJsonObject()), JsonNode.class);
        try {
            // patch
            JsonMergePatch jsonMergePatch = JsonMergePatch.fromJson(modifiedJson);
            // apply patch to original
            JsonNode patchedNode = jsonMergePatch.apply(sourceJson);
            data.put(GROUP, JsonUtils.objectToJson(patchedNode));
        } catch (JsonPatchException e) {
            throw new JsonParseException(e);
        }
    }

    private static String emptyJsonObject() {
        return "{}";
    }
}
