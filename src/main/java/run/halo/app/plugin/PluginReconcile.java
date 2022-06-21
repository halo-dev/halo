package run.halo.app.plugin;

import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.Plugin;
import run.halo.app.extension.ExtensionClient;

/**
 * @author guqing
 * @since 2.0.0
 */
@Component
public class PluginReconcile {
    @Autowired
    private HaloPluginManager haloPluginManager;

    @Autowired
    private ExtensionClient extensionClient;

    public void reconcile(String pluginName) {
        PluginWrapper pluginWrapper = haloPluginManager.getPlugin(pluginName);
        // get current state
        PluginState currentPhase = pluginWrapper.getPluginState();
        boolean currentState = isEnabled(currentPhase);
        // get desired state
        Plugin plugin = extensionClient.fetch(Plugin.class, pluginName).orElseThrow();
        Boolean desiredState = plugin.getSpec().getEnabled();

        // make changes
        if (currentState == desiredState) {
            return;
        }
        // 调和
        if (desiredState) {
            PluginState pluginState = haloPluginManager.startPlugin(pluginName);
            if (isEnabled(pluginState)) {
                plugin.getSpec().setEnabled(true);
                extensionClient.update(plugin);
            }
        } else {
            PluginState pluginState = haloPluginManager.stopPlugin(pluginName);
            if (PluginState.STOPPED.equals(pluginState)) {
                plugin.getSpec().setEnabled(false);
                extensionClient.update(plugin);
            }
            plugin.getSpec().setEnabled(false);
        }
    }

    private boolean isEnabled(PluginState currentState) {
        return PluginState.STARTED.equals(currentState);
    }
}
