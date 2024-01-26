package run.halo.app.plugin;

import static run.halo.app.extension.MetadataUtil.nullSafeAnnotations;

import java.nio.file.Path;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginManager;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import run.halo.app.core.extension.Plugin;
import run.halo.app.extension.ReactiveExtensionClient;

/**
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
@Component
public class PluginDevelopmentInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final PluginManager pluginManager;

    private final PluginProperties pluginProperties;

    private final ReactiveExtensionClient extensionClient;

    public PluginDevelopmentInitializer(PluginManager pluginManager,
        PluginProperties pluginProperties, ReactiveExtensionClient extensionClient) {
        this.pluginManager = pluginManager;
        this.pluginProperties = pluginProperties;
        this.extensionClient = extensionClient;
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent ignored) {
        if (!pluginManager.isDevelopment()) {
            return;
        }
        createFixedPluginIfNecessary();
    }

    private void createFixedPluginIfNecessary() {
        for (Path path : pluginProperties.getFixedPluginPath()) {
            Plugin plugin = new YamlPluginFinder().find(path);
            extensionClient.fetch(Plugin.class, plugin.getMetadata().getName())
                .flatMap(persistent -> {
                    plugin.getMetadata().setVersion(persistent.getMetadata().getVersion());
                    nullSafeAnnotations(plugin).put(PluginConst.RUNTIME_MODE_ANNO, "dev");
                    return extensionClient.update(plugin);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    nullSafeAnnotations(plugin).put(PluginConst.RUNTIME_MODE_ANNO, "dev");
                    return extensionClient.create(plugin);
                }))
                .retryWhen(Retry.backoff(10, Duration.ofMillis(100))
                    .filter(t -> t instanceof OptimisticLockingFailureException))
                .block();
        }
    }
}
