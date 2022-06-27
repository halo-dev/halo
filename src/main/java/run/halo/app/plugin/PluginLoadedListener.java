package run.halo.app.plugin;

import org.pf4j.PluginWrapper;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.Plugin;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.plugin.event.HaloPluginLoadedEvent;

/**
 * @author guqing
 * @since 2.0.0
 */
@Component
public class PluginLoadedListener implements ApplicationListener<HaloPluginLoadedEvent> {
    private final ExtensionClient extensionClient;

    public PluginLoadedListener(ExtensionClient extensionClient) {
        this.extensionClient = extensionClient;
    }

    @Override
    public void onApplicationEvent(HaloPluginLoadedEvent event) {
        PluginWrapper pluginWrapper = event.getPluginWrapper();
        // TODO: Optimize plugin custom resource loading method
        // load plugin.yaml
        YamlPluginFinder yamlPluginFinder = new YamlPluginFinder();
        Plugin plugin = yamlPluginFinder.find(pluginWrapper.getPluginPath());
        extensionClient.fetch(Plugin.class, plugin.getMetadata().getName())
            .ifPresentOrElse(persisted -> {
                plugin.getMetadata().setVersion(persisted.getMetadata().getVersion());
                extensionClient.update(plugin);
            }, () -> extensionClient.create(plugin));
    }
}
