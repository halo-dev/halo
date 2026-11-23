package run.halo.app.infra;

import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import lombok.Data;
import org.springframework.dao.OptimisticLockingFailureException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.exception.DuplicateNameException;
import run.halo.app.infra.utils.JacksonUtils;
import tools.jackson.databind.JsonNode;

/**
 * A model for system state deserialize from {@link run.halo.app.extension.ConfigMap} named {@code system-states}.
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
     * The username of the super administrator created during system setup.
     *
     * <p>It is used to resume an interrupted setup without creating another administrator.
     */
    private String setupUsername;

    /**
     * Deserialize from {@link ConfigMap}.
     *
     * @return config map
     */
    public static SystemState deserialize(ConfigMap configMap) {
        Map<String, String> data = configMap.getData();
        if (data == null) {
            return new SystemState();
        }
        return JacksonUtils.jsonToObject(data.getOrDefault(GROUP, emptyJsonObject()), SystemState.class);
    }

    /**
     * Update modified system state to config map.
     *
     * @param systemState modified system state
     * @param configMap config map
     */
    public static void update(SystemState systemState, ConfigMap configMap) {
        Map<String, String> data = configMap.getData();
        if (data == null) {
            data = new LinkedHashMap<>();
            configMap.setData(data);
        }
        JsonNode modifiedJson = JacksonUtils.MAPPER.valueToTree(systemState);
        // original
        JsonNode sourceJson = JacksonUtils.MAPPER.readTree(data.getOrDefault(GROUP, emptyJsonObject()));
        // apply patch to original
        JsonNode patchedNode = JacksonUtils.mergePatch(sourceJson, modifiedJson);
        data.put(GROUP, JacksonUtils.MAPPER.writeValueAsString(patchedNode));
    }

    /**
     * Update system state by the given {@link Consumer}.
     *
     * <p>if the system state config map does not exist, it will create a new one.
     */
    public static Mono<Void> upsetSystemState(ReactiveExtensionClient client, Consumer<SystemState> consumer) {
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
                        }))
                .retryWhen(Retry.backoff(5, Duration.ofMillis(100))
                        .filter(t -> t instanceof OptimisticLockingFailureException
                                // The config map may be created concurrently by another setup request.
                                || t instanceof DuplicateNameException))
                .then();
    }

    private static String emptyJsonObject() {
        return "{}";
    }
}
