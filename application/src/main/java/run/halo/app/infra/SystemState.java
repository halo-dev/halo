package run.halo.app.infra;

import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import lombok.Data;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.utils.JsonUtils;
import tools.jackson.databind.JsonNode;

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
        var mapper = JsonUtils.jsonMapper();
        JsonNode modifiedJson = mapper
            .convertValue(systemState, JsonNode.class);
        // original
        JsonNode sourceJson =
            JsonUtils.jsonToObject(data.getOrDefault(GROUP, emptyJsonObject()), JsonNode.class);
        var patched = mapper.readerForUpdating(sourceJson)
            .readValue(modifiedJson);
        data.put(GROUP, mapper.writeValueAsString(patched));
    }

    /**
     * <p>Update system state by the given {@link Consumer}.</p>
     * <p>if the system state config map does not exist, it will create a new one.</p>
     */
    public static Mono<Void> upsetSystemState(ReactiveExtensionClient client,
        Consumer<SystemState> consumer) {
        return Mono.defer(() -> client.fetch(ConfigMap.class, SYSTEM_STATES_CONFIGMAP)
                .switchIfEmpty(Mono.defer(() -> {
                    ConfigMap configMap = new ConfigMap();
                    configMap.setMetadata(new Metadata());
                    configMap.getMetadata().setName(SYSTEM_STATES_CONFIGMAP);
                    configMap.setData(new HashMap<>());
                    return client.create(configMap);
                }))
                .flatMap(configMap -> {
                    SystemState systemState = deserialize(configMap);
                    consumer.accept(systemState);
                    update(systemState, configMap);
                    return client.update(configMap);
                })
            )
            .retryWhen(Retry.backoff(5, Duration.ofMillis(100))
                .filter(OptimisticLockingFailureException.class::isInstance)
            )
            .then();
    }

    private static String emptyJsonObject() {
        return "{}";
    }
}
