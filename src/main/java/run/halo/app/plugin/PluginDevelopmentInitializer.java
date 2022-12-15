package run.halo.app.plugin;

import java.nio.file.Path;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginWrapper;
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

    private final HaloPluginManager haloPluginManager;

    private final PluginProperties pluginProperties;

    private final ReactiveExtensionClient extensionClient;

    public PluginDevelopmentInitializer(HaloPluginManager haloPluginManager,
        PluginProperties pluginProperties, ReactiveExtensionClient extensionClient) {
        this.haloPluginManager = haloPluginManager;
        this.pluginProperties = pluginProperties;
        this.extensionClient = extensionClient;
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        if (!haloPluginManager.isDevelopment()) {
            return;
        }
        createFixedPluginIfNecessary(haloPluginManager);
    }

    private void createFixedPluginIfNecessary(HaloPluginManager pluginManager) {
        for (Path path : pluginProperties.getFixedPluginPath()) {

            // Already loaded do not load again
            String pluginId = idForPath(path);

            // for issue #2901
            if (pluginId == null) {
                try {
                    pluginId = pluginManager.loadPlugin(path);
                } catch (Exception e) {
                    log.warn(e.getMessage(), e);
                    continue;
                }
            }

            PluginWrapper pluginWrapper = pluginManager.getPlugin(pluginId);
            if (pluginWrapper == null) {
                continue;
            }
            Plugin plugin = new YamlPluginFinder().find(pluginWrapper.getPluginPath());
            extensionClient.fetch(Plugin.class, plugin.getMetadata().getName())
                .flatMap(persistent -> {
                    plugin.getMetadata().setVersion(persistent.getMetadata().getVersion());
                    return extensionClient.update(plugin);
                })
                .switchIfEmpty(Mono.defer(() -> extensionClient.create(plugin)))
                .retryWhen(Retry.backoff(10, Duration.ofMillis(100))
                    .filter(t -> t instanceof OptimisticLockingFailureException))
                .block();
        }
    }

    protected String idForPath(Path pluginPath) {
        for (PluginWrapper plugin : haloPluginManager.getPlugins()) {
            if (plugin.getPluginPath().equals(pluginPath)) {
                return plugin.getPluginId();
            }
        }
        return null;
    }
}