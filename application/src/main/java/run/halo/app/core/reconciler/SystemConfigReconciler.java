package run.halo.app.core.reconciler;

import static java.util.Objects.requireNonNullElse;
import static run.halo.app.extension.index.query.Queries.equal;
import static run.halo.app.infra.utils.SystemConfigUtils.mergeMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.hash.Hashing;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import run.halo.app.core.extension.content.Constant;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ExtensionMatcher;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.infra.SystemConfigChangedEvent;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.utils.JsonParseException;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.infra.utils.SystemConfigUtils;

@Slf4j
@Component
@RequiredArgsConstructor
class SystemConfigReconciler implements Reconciler<Reconciler.Request> {

    private static final String DATA_SNAPSHOT_ANNO = "halo.run/data-snapshot";

    private final ExtensionClient client;

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Result reconcile(Request request) {
        Assert.state(
            Objects.equals(SystemSetting.SYSTEM_CONFIG, request.name()),
            "Only system config reconciler is supported to reconcile system config."
        );
        client.fetch(ConfigMap.class, request.name())
            .ifPresent(configMap -> {
                if (ExtensionUtil.isDeleted(configMap)) {
                    log.warn("System config was attempted to be deleted");
                    return;
                }
                // calculate if the configMap has changed
                // and publish event if changed
                var dataSnapshot = getDataSnapshot(configMap);
                if (populateChecksum(configMap)) {
                    updateDataSnapshot(configMap);
                    client.update(configMap);
                    log.info("System config has been detected as changed");
                    eventPublisher.publishEvent(
                        computeChangedEvent(configMap, dataSnapshot)
                    );
                }
                // do nothing if not changed
            });
        return null;
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        ExtensionMatcher matcher = extension ->
            Objects.equals(extension.getMetadata().getName(), SystemSetting.SYSTEM_CONFIG);
        return builder.extension(new ConfigMap())
            .syncAllOnStart(true)
            .syncAllListOptions(ListOptions.builder()
                .fieldQuery(equal("metadata.name", SystemSetting.SYSTEM_CONFIG))
                .build()
            )
            .onAddMatcher(matcher)
            .onUpdateMatcher(matcher)
            .onDeleteMatcher(matcher)
            .build();
    }

    private SystemConfigChangedEvent computeChangedEvent(ConfigMap configMap,
        @Nullable Map<String, String> oldData) {
        return client.fetch(ConfigMap.class, SystemSetting.SYSTEM_CONFIG_DEFAULT)
            .map(defaultConfigMap -> {
                var defaultData =
                    requireNonNullElse(defaultConfigMap.getData(), Map.<String, String>of());
                try {
                    var mergedOldData = mergeMap(
                        defaultData, requireNonNullElse(oldData, Map.of())
                    );
                    var mergedNewData = mergeMap(
                        defaultData, requireNonNullElse(configMap.getData(), Map.of())
                    );
                    return new SystemConfigChangedEvent(this, mergedOldData, mergedNewData);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            })
            .orElseGet(() -> new SystemConfigChangedEvent(
                this, oldData, requireNonNullElse(configMap.getData(), Map.of())
            ));
    }

    private ConfigMap computeSystemConfig(ConfigMap configMap) {
        return client.fetch(ConfigMap.class, SystemSetting.SYSTEM_CONFIG_DEFAULT)
            .map(defaultConfigMap -> {
                try {
                    return SystemConfigUtils.mergeConfigMap(defaultConfigMap, configMap);
                } catch (JsonProcessingException e) {
                    throw new JsonParseException(e);
                }
            })
            .orElse(configMap);
    }

    private static boolean populateChecksum(ConfigMap configMap) {
        var toHash = Optional.ofNullable(configMap.getData())
            .map(Objects::toString)
            .orElse("");
        var checksum = Hashing.sha256().hashString(toHash, StandardCharsets.UTF_8)
            .toString();
        var metadata = configMap.getMetadata();
        var notChanged = Optional.ofNullable(metadata.getAnnotations())
            .map(annotations -> annotations.get(Constant.CHECKSUM_CONFIG_ANNO))
            .stream()
            .anyMatch(existingChecksum -> Objects.equals(checksum, existingChecksum));
        if (notChanged) {
            log.debug("ConfigMap '{}' has not changed.", configMap.getMetadata().getName());
            return false;
        }
        log.debug("ConfigMap '{}' has changed, updating checksum {}.",
            configMap.getMetadata().getName(), checksum);
        if (metadata.getAnnotations() == null) {
            metadata.setAnnotations(new HashMap<>());
        }
        metadata.getAnnotations().put(Constant.CHECKSUM_CONFIG_ANNO, checksum);
        return true;
    }

    private static void updateDataSnapshot(ConfigMap configMap) {
        Optional.ofNullable(configMap.getData())
            .map(JsonUtils::objectToJson)
            .ifPresent(dataJson -> {
                var metadata = configMap.getMetadata();
                if (metadata.getAnnotations() == null) {
                    metadata.setAnnotations(new HashMap<>());
                }
                metadata.getAnnotations().put(DATA_SNAPSHOT_ANNO, dataJson);
            });
    }

    private static Map<String, String> getDataSnapshot(ConfigMap configMap) {
        return Optional.ofNullable(configMap.getMetadata().getAnnotations())
            .map(annotations -> annotations.get(DATA_SNAPSHOT_ANNO))
            .map(dataJson -> JsonUtils.jsonToObject(
                dataJson,
                new TypeReference<Map<String, String>>() {
                }
            ))
            .orElse(Map.of());
    }
}
