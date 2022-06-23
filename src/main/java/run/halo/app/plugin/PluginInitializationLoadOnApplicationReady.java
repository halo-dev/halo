package run.halo.app.plugin;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.Plugin;
import run.halo.app.extension.ExtensionClient;

/**
 * Load plugins after application ready.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class PluginInitializationLoadOnApplicationReady
    implements ApplicationListener<ApplicationReadyEvent> {

    private final PluginService pluginService;

    private final HaloPluginManager haloPluginManager;

    private final ExtensionClient extensionClient;

    public PluginInitializationLoadOnApplicationReady(PluginService pluginService,
        HaloPluginManager haloPluginManager, ExtensionClient extensionClient) {
        this.pluginService = pluginService;
        this.haloPluginManager = haloPluginManager;
        this.extensionClient = extensionClient;
    }

    @Override
    public void onApplicationEvent(@NonNull ApplicationReadyEvent event) {
        haloPluginManager.loadPlugins();
        initStartupPlugins();
    }

    private void initStartupPlugins() {
        extensionClient.list(Plugin.class,
                predicate -> predicate.getSpec().getEnabled(),
                null)
            .forEach(plugin -> pluginService.startup(plugin.getMetadata().getName()));
    }
}
