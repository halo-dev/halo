package run.halo.app.core.extension.reconciler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.PluginRuntimeException;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.pf4j.RuntimeMode;
import run.halo.app.core.extension.Plugin;
import run.halo.app.core.extension.ReverseProxy;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.Reconciler.Request;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.infra.utils.PathUtils;
import run.halo.app.plugin.HaloPluginManager;
import run.halo.app.plugin.PluginConst;
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
    private static final String FINALIZER_NAME = "plugin-protection";
    private final ExtensionClient client;
    private final HaloPluginManager haloPluginManager;

    public PluginReconciler(ExtensionClient client,
        HaloPluginManager haloPluginManager) {
        this.client = client;
        this.haloPluginManager = haloPluginManager;
    }

    @Override
    public Result reconcile(Request request) {
        client.fetch(Plugin.class, request.name())
            .ifPresent(plugin -> {
                if (plugin.getMetadata().getDeletionTimestamp() != null) {
                    cleanUpResourcesAndRemoveFinalizer(request.name());
                    return;
                }
                addFinalizerIfNecessary(plugin);
                reconcilePluginState(plugin.getMetadata().getName());
                createInitialReverseProxyIfNotPresent(plugin);
            });
        return new Result(false, null);
    }

    private void reconcilePluginState(String name) {
        if (haloPluginManager.getPlugin(name) == null) {
            ensurePluginLoaded();
        }

        client.fetch(Plugin.class, name).ifPresent(plugin -> {
            Plugin oldPlugin = JsonUtils.deepCopy(plugin);
            Plugin.PluginStatus pluginStatus = plugin.statusNonNull();
            PluginWrapper pluginWrapper = haloPluginManager.getPlugin(name);
            if (pluginWrapper == null) {
                pluginStatus.setPhase(PluginState.FAILED);
                pluginStatus.setReason("PluginNotFound");
                pluginStatus.setMessage("Plugin " + name + " not found in plugin manager");
            } else {
                // Set to the correct state
                pluginStatus.setPhase(pluginWrapper.getPluginState());

                if (haloPluginManager.getUnresolvedPlugins().contains(pluginWrapper)) {
                    // load and resolve plugin
                    haloPluginManager.loadPlugin(pluginWrapper.getPluginPath());
                }
            }

            String logo = plugin.getSpec().getLogo();
            if (PathUtils.isAbsoluteUri(logo)) {
                pluginStatus.setLogo(logo);
            } else {
                String assetsPrefix =
                    PluginConst.assertsRoutePrefix(plugin.getMetadata().getName());
                pluginStatus.setLogo(PathUtils.combinePath(assetsPrefix, logo));
            }

            if (!plugin.equals(oldPlugin)) {
                client.update(plugin);
            }
        });

        startPlugin(name);

        stopPlugin(name);
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

    private void startPlugin(String pluginName) {
        client.fetch(Plugin.class, pluginName).ifPresent(plugin -> {
            final Plugin oldPlugin = JsonUtils.deepCopy(plugin);
            if (shouldReconcileStartState(plugin)) {
                PluginState currentState = haloPluginManager.startPlugin(pluginName);
                handleStatus(plugin, currentState, PluginState.STARTED);
                plugin.statusNonNull().setLastStartTime(Instant.now());
            }

            Plugin.PluginStatus status = plugin.statusNonNull();
            String jsBundlePath =
                BundleResourceUtils.getJsBundlePath(haloPluginManager, pluginName);
            status.setEntry(jsBundlePath);

            String cssBundlePath =
                BundleResourceUtils.getCssBundlePath(haloPluginManager, pluginName);
            status.setStylesheet(cssBundlePath);

            if (!plugin.equals(oldPlugin)) {
                client.update(plugin);
            }
        });
    }

    private boolean shouldReconcileStopState(Plugin plugin) {
        return !plugin.getSpec().getEnabled()
            && plugin.statusNonNull().getPhase() == PluginState.STARTED;
    }

    private void stopPlugin(String pluginName) {
        client.fetch(Plugin.class, pluginName).ifPresent(plugin -> {
            Plugin oldPlugin = JsonUtils.deepCopy(plugin);

            if (shouldReconcileStopState(plugin)) {
                PluginState currentState = haloPluginManager.stopPlugin(pluginName);
                handleStatus(plugin, currentState, PluginState.STOPPED);
            }

            if (!plugin.equals(oldPlugin)) {
                client.update(plugin);
            }
        });
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

    private void addFinalizerIfNecessary(Plugin oldPlugin) {
        Set<String> finalizers = oldPlugin.getMetadata().getFinalizers();
        if (finalizers != null && finalizers.contains(FINALIZER_NAME)) {
            return;
        }
        client.fetch(Plugin.class, oldPlugin.getMetadata().getName())
            .ifPresent(plugin -> {
                Set<String> newFinalizers = plugin.getMetadata().getFinalizers();
                if (newFinalizers == null) {
                    newFinalizers = new HashSet<>();
                    plugin.getMetadata().setFinalizers(newFinalizers);
                }
                newFinalizers.add(FINALIZER_NAME);
                client.update(plugin);
            });
    }

    private void cleanUpResourcesAndRemoveFinalizer(String pluginName) {
        client.fetch(Plugin.class, pluginName).ifPresent(plugin -> {
            cleanUpResources(plugin);
            if (plugin.getMetadata().getFinalizers() != null) {
                plugin.getMetadata().getFinalizers().remove(FINALIZER_NAME);
            }
            client.update(plugin);
        });
    }

    private void cleanUpResources(Plugin plugin) {
        PluginWrapper pluginWrapper = haloPluginManager.getPlugin(plugin.getMetadata().getName());
        if (pluginWrapper == null) {
            return;
        }
        // stop and unload plugin, see also PluginBeforeStopSyncListener
        haloPluginManager.stopPlugin(pluginWrapper.getPluginId());
        haloPluginManager.unloadPlugin(pluginWrapper.getPluginId());
        // delete plugin resources
        if (RuntimeMode.DEPLOYMENT.equals(pluginWrapper.getRuntimeMode())) {
            // delete plugin file
            try {
                Files.deleteIfExists(pluginWrapper.getPluginPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    void createInitialReverseProxyIfNotPresent(Plugin plugin) {
        String pluginName = plugin.getMetadata().getName();
        String reverseProxyName = initialReverseProxyName(pluginName);
        ReverseProxy reverseProxy = new ReverseProxy();
        reverseProxy.setMetadata(new Metadata());
        reverseProxy.getMetadata().setName(reverseProxyName);
        // put label to identify this reverse
        reverseProxy.getMetadata().setLabels(new HashMap<>());
        reverseProxy.getMetadata().getLabels().put(PluginConst.PLUGIN_NAME_LABEL_NAME, pluginName);

        reverseProxy.setRules(new ArrayList<>());

        String logo = plugin.getSpec().getLogo();
        if (StringUtils.isNotBlank(logo) && !PathUtils.isAbsoluteUri(logo)) {
            ReverseProxy.ReverseProxyRule logoRule = new ReverseProxy.ReverseProxyRule(logo,
                new ReverseProxy.FileReverseProxyProvider(null, logo));
            reverseProxy.getRules().add(logoRule);
        }

        client.fetch(ReverseProxy.class, reverseProxyName)
            .ifPresentOrElse(persisted -> {
                if (isDevelopmentMode(pluginName)) {
                    reverseProxy.getMetadata()
                        .setVersion(persisted.getMetadata().getVersion());
                    client.update(reverseProxy);
                }
            }, () -> client.create(reverseProxy));
    }

    static String initialReverseProxyName(String pluginName) {
        return pluginName + "-system-generated-reverse-proxy";
    }

    private boolean isDevelopmentMode(String name) {
        PluginWrapper pluginWrapper = haloPluginManager.getPlugin(name);
        if (pluginWrapper == null) {
            return false;
        }
        return RuntimeMode.DEVELOPMENT.equals(pluginWrapper.getRuntimeMode());
    }
}
