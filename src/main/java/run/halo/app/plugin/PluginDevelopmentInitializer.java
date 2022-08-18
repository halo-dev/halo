package run.halo.app.plugin;

import java.nio.file.Path;
import org.pf4j.PluginWrapper;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.Plugin;
import run.halo.app.extension.ExtensionClient;

/**
 * @author guqing
 * @since 2.0.0
 */
@Component
public class PluginDevelopmentInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final HaloPluginManager haloPluginManager;

    private final PluginProperties pluginProperties;

    private final ExtensionClient extensionClient;

    public PluginDevelopmentInitializer(HaloPluginManager haloPluginManager,
        PluginProperties pluginProperties, ExtensionClient extensionClient) {
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
            String pluginId = pluginManager.loadPlugin(path);
            PluginWrapper pluginWrapper = pluginManager.getPlugin(pluginId);
            if (pluginWrapper == null) {
                continue;
            }
            Plugin plugin = new YamlPluginFinder().find(pluginWrapper.getPluginPath());
            extensionClient.fetch(Plugin.class, plugin.getMetadata().getName())
                .ifPresentOrElse(persistent -> {
                    plugin.getMetadata().setVersion(persistent.getMetadata().getVersion());
                    extensionClient.update(plugin);
                }, () -> extensionClient.create(plugin));
        }
    }
}
