package run.halo.app.core.user.service;

import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Setting;
import run.halo.app.extension.ConfigMap;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@link Setting} related {@link ConfigMap} service.
 *
 * @author guqing
 * @since 2.20.0
 */
public interface SettingConfigService {

    Mono<Void> upsertConfig(String configMapName, ObjectNode configJsonData);

    Mono<ObjectNode> fetchConfig(String configMapName);
}
