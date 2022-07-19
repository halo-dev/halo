package run.halo.app.plugin;

import org.pf4j.PluginWrapper;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.plugin.event.HaloPluginBeforeStopEvent;

/**
 * Synchronization listener executed by the plugin before it is stopped.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class PluginBeforeStopSyncListener
    implements ApplicationListener<HaloPluginBeforeStopEvent> {

    private final ExtensionClient client;

    public PluginBeforeStopSyncListener(ExtensionClient client) {
        this.client = client;
    }

    @Override
    public void onApplicationEvent(@NonNull HaloPluginBeforeStopEvent event) {
        PluginWrapper pluginWrapper = event.getPlugin();
        PluginApplicationContext pluginContext = ExtensionContextRegistry.getInstance()
            .getByPluginId(pluginWrapper.getPluginId());

        cleanUpPluginExtensionResources(pluginContext);
    }

    private void cleanUpPluginExtensionResources(PluginApplicationContext context) {
        MultiValueMap<GroupVersionKind, String> gvkExtensionNames =
            context.extensionNamesMapping();
        gvkExtensionNames.forEach((gvk, extensionNames) ->
            extensionNames.forEach(extensionName -> client.fetch(gvk, extensionName)
                .ifPresent(client::delete)));
    }
}
