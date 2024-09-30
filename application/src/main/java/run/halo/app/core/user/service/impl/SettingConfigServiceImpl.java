package run.halo.app.core.user.service.impl;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import run.halo.app.core.extension.Setting;
import run.halo.app.core.user.service.SettingConfigService;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.utils.SettingUtils;

/**
 * {@link Setting} related {@link ConfigMap} service implementation.
 *
 * @author guqing
 * @since 2.20.0
 */
@Component
@RequiredArgsConstructor
public class SettingConfigServiceImpl implements SettingConfigService {
    private final ReactiveExtensionClient client;

    @Override
    public Mono<Void> upsertConfig(String configMapName, ObjectNode configJsonData) {
        Assert.notNull(configMapName, "Config map name must not be null");
        Assert.notNull(configJsonData, "Config json data must not be null");
        var data = SettingUtils.settingConfigJsonToMap(configJsonData);
        return Mono.defer(() -> client.fetch(ConfigMap.class, configMapName)
                .flatMap(persisted -> {
                    persisted.setData(data);
                    return client.update(persisted);
                }))
            .retryWhen(Retry.backoff(5, Duration.ofMillis(100))
                .filter(OptimisticLockingFailureException.class::isInstance)
            )
            .switchIfEmpty(Mono.defer(() -> {
                var configMap = new ConfigMap();
                configMap.setMetadata(new Metadata());
                configMap.getMetadata().setName(configMapName);
                configMap.setData(data);
                return client.create(configMap);
            }))
            .then();
    }

    @Override
    public Mono<ObjectNode> fetchConfig(String configMapName) {
        return client.fetch(ConfigMap.class, configMapName)
            .map(SettingUtils::settingConfigToJson);
    }
}
