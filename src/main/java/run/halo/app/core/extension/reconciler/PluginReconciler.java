package run.halo.app.core.extension.reconciler;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginRuntimeException;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import run.halo.app.core.extension.Plugin;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.Reconciler.Request;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.plugin.HaloPluginManager;
import run.halo.app.plugin.PluginStartingError;
import run.halo.app.plugin.resources.BundleResourceUtils;

/**
 * Plugin reconciler.
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
public class PluginReconciler implements Reconciler<Request> {

    private final ExtensionClient client;
    private final HaloPluginManager haloPluginManager;

    public PluginReconciler(ExtensionClient client,
        HaloPluginManager haloPluginManager) {
        this.client = client;
        this.haloPluginManager = haloPluginManager;
    }

    @Override
    public Result reconcile(Request request) {
        return client.fetch(Plugin.class, request.name())
            .map(plugin -> {
                final Plugin oldPlugin = deepCopy(plugin);
                try {
                    reconcilePluginState(plugin);
                    // TODO: reconcile other plugin resources

                    if (!Objects.equals(oldPlugin, plugin)) {
                        // update plugin when attributes changed
                        client.update(plugin);
                    }
                } catch (Exception e) {
                    // update plugin and requeue
                    client.update(plugin);
                    log.error(e.getMessage(), e);
                    return new Result(true, null);
                }
                return new Result(false, null);
            })
            .orElse(new Result(false, null));
    }

    private void reconcilePluginState(Plugin plugin) {
        Plugin.PluginStatus pluginStatus = plugin.statusNonNull();
        String name = plugin.getMetadata().getName();
        PluginWrapper pluginWrapper = haloPluginManager.getPlugin(name);
        if (pluginWrapper == null) {
            ensurePluginLoaded();
            pluginWrapper = haloPluginManager.getPlugin(name);
        }

        if (pluginWrapper == null) {
            pluginStatus.setPhase(PluginState.FAILED);
            pluginStatus.setReason("PluginNotFound");
            pluginStatus.setMessage("Plugin " + name + " not found in plugin manager");
            return;
        }

        if (!Objects.equals(pluginStatus.getPhase(), pluginWrapper.getPluginState())) {
            // Set to the correct state
            pluginStatus.setPhase(pluginWrapper.getPluginState());
        }

        if (haloPluginManager.getUnresolvedPlugins().contains(pluginWrapper)) {
            // load and resolve plugin
            haloPluginManager.loadPlugin(pluginWrapper.getPluginPath());
        }

        if (shouldReconcileStartState(plugin)) {
            startPlugin(plugin);
        }

        if (shouldReconcileStopState(plugin)) {
            stopPlugin(plugin);
        }
    }

    private Plugin deepCopy(Plugin plugin) {
        return JsonUtils.jsonToObject(JsonUtils.objectToJson(plugin), Plugin.class);
    }

    private void ensurePluginLoaded() {
        // load plugin if exists in plugin root paths.
        List<PluginWrapper> loadedPlugins = haloPluginManager.getPlugins();
        Map<Path, PluginWrapper> loadedPluginWrapperMap = loadedPlugins.stream()
            .collect(Collectors.toMap(PluginWrapper::getPluginPath, item -> item));
        haloPluginManager.getPluginRepository()
            .getPluginPaths()
            .forEach(path -> {
                if (!loadedPluginWrapperMap.containsKey(path)) {
                    haloPluginManager.loadPlugin(path);
                }
            });
    }

    private boolean shouldReconcileStartState(Plugin plugin) {
        return plugin.getSpec().getEnabled()
            && plugin.statusNonNull().getPhase() != PluginState.STARTED;
    }

    private void startPlugin(Plugin plugin) {
        String pluginName = plugin.getMetadata().getName();
        PluginState currentState = haloPluginManager.startPlugin(pluginName);
        handleStatus(plugin, currentState, PluginState.STARTED);
        Plugin.PluginStatus status = plugin.statusNonNull();

        String jsBundlePath = BundleResourceUtils.getJsBundlePath(haloPluginManager, pluginName);
        status.setEntry(jsBundlePath);

        String cssBundlePath = BundleResourceUtils.getCssBundlePath(haloPluginManager, pluginName);
        status.setStylesheet(cssBundlePath);

        status.setLastStartTime(Instant.now());
    }

    private boolean shouldReconcileStopState(Plugin plugin) {
        return !plugin.getSpec().getEnabled()
            && plugin.statusNonNull().getPhase() == PluginState.STARTED;
    }

    private void stopPlugin(Plugin plugin) {
        String pluginName = plugin.getMetadata().getName();
        PluginState currentState = haloPluginManager.stopPlugin(pluginName);
        handleStatus(plugin, currentState, PluginState.STOPPED);
    }

    private void handleStatus(Plugin plugin, PluginState currentState,
        PluginState desiredState) {
        Plugin.PluginStatus status = plugin.statusNonNull();
        status.setPhase(currentState);
        status.setLastTransitionTime(Instant.now());
        if (desiredState.equals(currentState)) {
            plugin.getSpec().setEnabled(PluginState.STARTED.equals(currentState));
        } else {
            PluginStartingError startingError =
                haloPluginManager.getPluginStartingError(plugin.getMetadata().getName());
            status.setReason(startingError.getMessage());
            status.setMessage(startingError.getDevMessage());
            // requeue the plugin for reconciliation
            throw new PluginRuntimeException(startingError.getMessage());
        }
    }
}
