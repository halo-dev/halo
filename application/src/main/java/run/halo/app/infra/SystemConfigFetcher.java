package run.halo.app.infra;

import java.util.Map;
import java.util.Optional;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ReactiveExtensionClient;

/**
 * A fetcher that fetches the system configuration from the extension client.
 * If there are {@link ConfigMap}s named <code>system-default</code> and <code>system</code> at
 * the same time, the {@link ConfigMap} named system will be json merge patch to
 * {@link ConfigMap} named <code>system-default</code>
 *
 * @author guqing
 * @since 2.0.0
 */
public interface SystemConfigFetcher {

    <T> Mono<T> fetch(String key, Class<T> type);

    Mono<SystemSetting.Basic> getBasic();

    Mono<SystemSetting.Comment> fetchComment();

    Mono<SystemSetting.Post> fetchPost();

    Mono<SystemSetting.ThemeRouteRules> fetchRouteRules();

    /**
     * Gets the system config values as a map(merged). Do not update this map directly.
     *
     * @return system config values map, cached, unmodifiable
     */
    Mono<Map<String, String>> getConfig();

    /**
     * Load the system config map from the extension client.
     *
     * @return latest configMap from {@link ReactiveExtensionClient} without any cache.
     */
    Mono<ConfigMap> getConfigMap();

    /**
     * Load the system config map from the extension client in a blocking way.
     *
     * @return latest configMap from {@link ReactiveExtensionClient} without any cache.
     */
    Optional<ConfigMap> getConfigMapBlocking();

}
