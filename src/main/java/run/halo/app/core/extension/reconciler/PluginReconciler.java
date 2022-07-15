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
import org.pf4j.RuntimeMode;
import run.halo.app.core.extension.Plugin;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.plugin.HaloPluginManager;
import run.halo.app.plugin.PluginStartingError;
import run.halo.app.plugin.YamlPluginFinder;
import run.halo.app.plugin.resources.JsBundleRuleProvider;
import run.halo.app.plugin.resources.ReverseProxyRouterFunctionFactory;

/**
 * Plugin reconciler.
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
public class PluginReconciler implements Reconciler {

    private final ExtensionClient client;
    private final HaloPluginManager haloPluginManager;

    private final JsBundleRuleProvider jsBundleRule;

    public PluginReconciler(ExtensionClient client,
        HaloPluginManager haloPluginManager,
        JsBundleRuleProvider jsBundleRule) {
        this.client = client;
        this.haloPluginManager = haloPluginManager;
        this.jsBundleRule = jsBundleRule;
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

        ensureSpecUpToDateWhenDevelopmentMode(pluginWrapper, plugin);

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
        // TODO Check whether the JS bundle rule exists. If it does not exist, do not populate
        // populate stylesheet path
        jsBundleRule.jsRule(pluginName)
            .map(jsRule -> ReverseProxyRouterFunctionFactory.buildRoutePath(pluginName, jsRule))
            .ifPresent(status::setEntry);

        jsBundleRule.cssRule(pluginName)
            .map(cssRule -> ReverseProxyRouterFunctionFactory.buildRoutePath(pluginName, cssRule))
            .ifPresent(status::setStylesheet);

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

    private void ensureSpecUpToDateWhenDevelopmentMode(PluginWrapper pluginWrapper,
        Plugin oldPlugin) {
        if (!RuntimeMode.DEPLOYMENT.equals(pluginWrapper.getRuntimeMode())) {
            return;
        }
        YamlPluginFinder yamlPluginFinder = new YamlPluginFinder();
        Plugin pluginFromPath = yamlPluginFinder.find(pluginWrapper.getPluginPath());
        // ensure plugin spec is up to date
        Plugin.PluginSpec pluginSpec = JsonUtils.deepCopy(pluginFromPath.getSpec());
        if (!Objects.equals(oldPlugin.getSpec(), pluginSpec)) {
            oldPlugin.setSpec(pluginSpec);
        }
    }
}
