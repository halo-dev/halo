package run.halo.app.core.extension.reconciler;

import static run.halo.app.core.extension.Plugin.PluginStatus.nullSafeConditions;
import static run.halo.app.extension.ExtensionUtil.addFinalizers;
import static run.halo.app.extension.ExtensionUtil.removeFinalizers;
import static run.halo.app.extension.MetadataUtil.nullSafeAnnotations;
import static run.halo.app.plugin.PluginConst.PLUGIN_PATH;
import static run.halo.app.plugin.PluginConst.RELOAD_ANNO;
import static run.halo.app.plugin.PluginConst.RUNTIME_MODE_ANNO;
import static run.halo.app.plugin.PluginExtensionLoaderUtils.isSetting;
import static run.halo.app.plugin.PluginExtensionLoaderUtils.lookupExtensions;
import static run.halo.app.plugin.PluginUtils.generateFileName;
import static run.halo.app.plugin.PluginUtils.isDevelopmentMode;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.PluginManager;
import org.pf4j.PluginState;
import org.pf4j.RuntimeMode;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.springframework.web.util.UriComponentsBuilder;
import run.halo.app.core.extension.Plugin;
import run.halo.app.core.extension.ReverseProxy;
import run.halo.app.core.extension.Setting;
import run.halo.app.core.extension.theme.SettingUtils;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.Unstructured;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.Reconciler.Request;
import run.halo.app.extension.controller.RequeueException;
import run.halo.app.infra.Condition;
import run.halo.app.infra.ConditionStatus;
import run.halo.app.infra.utils.PathUtils;
import run.halo.app.infra.utils.YamlUnstructuredLoader;
import run.halo.app.plugin.PluginConst;
import run.halo.app.plugin.PluginProperties;

/**
 * Plugin reconciler.
 *
 * @author guqing
 * @author johnniang
 * @since 2.0.0
 */
@Slf4j
@Component
public class PluginReconciler implements Reconciler<Request> {
    private static final String FINALIZER_NAME = "plugin-protection";
    private final ExtensionClient client;
    private final PluginManager pluginManager;

    private final PluginProperties pluginProperties;

    private Clock clock;

    public PluginReconciler(ExtensionClient client, PluginManager pluginManager,
        PluginProperties pluginProperties) {
        this.client = client;
        this.pluginManager = pluginManager;
        this.pluginProperties = pluginProperties;
        this.clock = Clock.systemUTC();
    }

    /**
     * Only for testing.
     *
     * @param clock new clock.
     */
    void setClock(Clock clock) {
        this.clock = clock;
    }

    @Override
    public Result reconcile(Request request) {
        return client.fetch(Plugin.class, request.name())
            .map(plugin -> {
                if (ExtensionUtil.isDeleted(plugin)) {
                    // CleanUp resources and remove finalizer.
                    if (removeFinalizers(plugin.getMetadata(), Set.of(FINALIZER_NAME))) {
                        cleanupResources(plugin);
                        syncPluginState(plugin);
                        client.update(plugin);
                    }
                    return Result.doNotRetry();
                }
                addFinalizers(plugin.getMetadata(), Set.of(FINALIZER_NAME));
                plugin.statusNonNull().setPhase(Plugin.Phase.PENDING);

                // Prepare
                try {
                    resolveLoadLocation(plugin);

                    loadOrReload(plugin);
                    createOrUpdateSetting(plugin);
                    createOrUpdateReverseProxy(plugin);
                    resolveStaticResources(plugin);

                    if (requestToEnable(plugin)) {
                        // Start
                        startPlugin(plugin);
                    } else {
                        // stop the plugin and disable it
                        stopAndDisablePlugin(plugin);
                    }
                } catch (Throwable t) {
                    // populate condition
                    var condition = Condition.builder()
                        .type(PluginState.FAILED.toString())
                        .reason("UnexpectedState")
                        .message(t.getMessage())
                        .status(ConditionStatus.FALSE)
                        .lastTransitionTime(clock.instant())
                        .build();
                    var status = plugin.statusNonNull();
                    nullSafeConditions(status).addAndEvictFIFO(condition);
                    status.setPhase(Plugin.Phase.FAILED);
                    throw t;
                } finally {
                    syncPluginState(plugin);
                    client.update(plugin);
                }

                return Result.doNotRetry();
            })
            .orElseGet(Result::doNotRetry);
    }

    private void syncPluginState(Plugin plugin) {
        var pluginName = plugin.getMetadata().getName();
        var p = pluginManager.getPlugin(pluginName);
        if (p != null) {
            plugin.statusNonNull().setLastProbeState(p.getPluginState());
        } else {
            plugin.statusNonNull().setLastProbeState(null);
        }
    }

    private static boolean requestToReload(Plugin plugin) {
        var annotations = plugin.getMetadata().getAnnotations();
        return annotations != null && annotations.remove(RELOAD_ANNO) != null;
    }

    private void cleanupResources(Plugin plugin) {
        var pluginName = plugin.getMetadata().getName();
        var reverseProxyName = buildReverseProxyName(pluginName);
        log.info("Deleting reverse proxy {} for plugin {}", reverseProxyName, pluginName);
        client.fetch(ReverseProxy.class, reverseProxyName)
            .ifPresent(reverseProxy -> {
                client.delete(reverseProxy);
                throw new RequeueException(Result.requeue(null),
                    String.format("""
                        Waiting for reverse proxy %s to be deleted.""", reverseProxyName)
                );
            });
        var settingName = plugin.getSpec().getSettingName();
        if (StringUtils.isNotBlank(settingName)) {
            log.info("Deleting settings {} for plugin {}", settingName, pluginName);
            client.fetch(Setting.class, settingName)
                .ifPresent(setting -> {
                    client.delete(setting);
                    throw new RequeueException(Result.requeue(null), String.format("""
                        Waiting for setting %s to be deleted.""", settingName));
                });
        }
        if (pluginManager.getPlugin(pluginName) != null) {
            log.info("Deleting plugin {} in plugin manager.", pluginName);
            var deleted = pluginManager.deletePlugin(pluginName);
            if (!deleted) {
                log.warn("Failed to delete plugin {}", pluginName);
            }
        }
    }

    private void startPlugin(Plugin plugin) {
        // start the plugin
        var pluginName = plugin.getMetadata().getName();
        var wrapper = pluginManager.getPlugin(pluginName);
        plugin.statusNonNull().setPhase(Plugin.Phase.STARTING);
        if (!PluginState.STARTED.equals(wrapper.getPluginState())) {
            var pluginState = pluginManager.startPlugin(pluginName);
            if (!PluginState.STARTED.equals(pluginState)) {
                throw new IllegalStateException("Failed to start plugin " + pluginName);
            }
            plugin.statusNonNull().setLastStartTime(clock.instant());
            var condition = Condition.builder()
                .type(PluginState.STARTED.toString())
                .reason(PluginState.STARTED.toString())
                .message("Started successfully")
                .lastTransitionTime(clock.instant())
                .status(ConditionStatus.TRUE)
                .build();
            nullSafeConditions(plugin.statusNonNull()).addAndEvictFIFO(condition);
        }
        plugin.statusNonNull().setPhase(Plugin.Phase.STARTED);
    }

    private void stopAndDisablePlugin(Plugin plugin) {
        var pluginName = plugin.getMetadata().getName();
        if (pluginManager.getPlugin(pluginName) != null) {
            pluginManager.disablePlugin(pluginName);
        }
        plugin.statusNonNull().setPhase(Plugin.Phase.DISABLED);
    }

    private static boolean requestToEnable(Plugin plugin) {
        var enabled = plugin.getSpec().getEnabled();
        return enabled != null && enabled;
    }

    private void resolveStaticResources(Plugin plugin) {
        var pluginName = plugin.getMetadata().getName();
        var pluginVersion = plugin.getSpec().getVersion();
        if (isDevelopmentMode(plugin)) {
            // when we are in dev mode, the plugin version is not always changed.
            pluginVersion = String.valueOf(clock.instant().toEpochMilli());
        }
        var status = plugin.statusNonNull();
        var specLogo = plugin.getSpec().getLogo();
        if (StringUtils.isNotBlank(specLogo)) {
            log.info("Resolving logo resource for plugin {}", pluginName);
            // the logo might be:
            // 1. URL
            // 2. relative path to "resources" folder
            // 3. base64 format data image
            var logo = specLogo;
            if (!specLogo.startsWith("data:image")) {
                try {
                    logo = new URL(specLogo).toString();
                } catch (MalformedURLException ignored) {
                    // indicate the logo is a path
                    logo = UriComponentsBuilder.newInstance()
                        .pathSegment("plugins", pluginName, "assets")
                        .path(specLogo)
                        .queryParam("version", pluginVersion)
                        .build(true)
                        .toString();
                }
            }
            status.setLogo(logo);
        }

        log.info("Resolving main.js and style.css for plugin {}", pluginName);
        var p = pluginManager.getPlugin(pluginName);
        var classLoader = p.getPluginClassLoader();
        var resLoader = new DefaultResourceLoader(classLoader);
        var entryRes = resLoader.getResource("classpath:console/main.js");
        var cssRes = resLoader.getResource("classpath:console/style.css");
        if (entryRes.exists()) {
            var entry = UriComponentsBuilder.newInstance()
                .pathSegment("plugins", pluginName, "assets", "console", "main.js")
                .queryParam("version", pluginVersion)
                .build(true)
                .toString();
            status.setEntry(entry);
        }
        if (cssRes.exists()) {
            var stylesheet = UriComponentsBuilder.newInstance()
                .pathSegment("plugins", pluginName, "assets", "console", "style.css")
                .queryParam("version", pluginVersion)
                .build(true)
                .toString();
            status.setStylesheet(stylesheet);
        }
    }

    private void loadOrReload(Plugin plugin) {
        // TODO Try to check dependencies before.
        var pluginName = plugin.getMetadata().getName();
        try {
            var p = pluginManager.getPlugin(pluginName);
            var requestToReload = requestToReload(plugin);
            if (requestToReload) {
                log.info("Unloading plugin {}", pluginName);
                if (p != null) {
                    pluginManager.unloadPlugin(pluginName);
                }
            }
            if (p == null || requestToReload) {
                log.info("Loading plugin {}", pluginName);
                var loadLocation = plugin.getStatus().getLoadLocation();
                pluginManager.loadPlugin(Paths.get(loadLocation));
                log.info("Loaded plugin {}", pluginName);
            }
        } catch (Throwable t) {
            // unload the plugin
            if (pluginManager.getPlugin(pluginName) != null) {
                pluginManager.unloadPlugin(pluginName);
            }
            throw t;
        }
    }

    private void createOrUpdateSetting(Plugin plugin) {
        log.info("Initializing setting and config map for plugin {}",
            plugin.getMetadata().getName());
        var settingName = plugin.getSpec().getSettingName();
        if (StringUtils.isBlank(settingName)) {
            // do nothing if no setting name provided.
            return;
        }

        var pluginName = plugin.getMetadata().getName();
        var p = pluginManager.getPlugin(pluginName);
        var resources = lookupExtensions(p.getPluginClassLoader());
        var loader = new YamlUnstructuredLoader(resources);
        var setting = loader.load().stream()
            .filter(isSetting(settingName))
            .findFirst()
            .map(u -> Unstructured.OBJECT_MAPPER.convertValue(u, Setting.class))
            .orElseThrow(() -> new IllegalStateException(String.format("""
                    Setting name %s was provided but setting extension \
                    was not found in plugin %s.""",
                settingName, pluginName)));

        client.fetch(Setting.class, settingName)
            .ifPresentOrElse(oldSetting -> {
                // overwrite the setting
                var version = oldSetting.getMetadata().getVersion();
                setting.getMetadata().setVersion(version);
                // TODO Remove this line in the future
                removeFinalizers(setting.getMetadata(), Set.of("plugin-protector"));
                client.update(setting);
            }, () -> client.create(setting));

        log.info("Initialized setting {} for plugin {}", settingName, pluginName);

        // create default config map
        var configMapName = plugin.getSpec().getConfigMapName();
        if (StringUtils.isBlank(configMapName)) {
            return;
        }

        var defaultConfigMap = SettingUtils.populateDefaultConfig(setting, configMapName);

        client.fetch(ConfigMap.class, configMapName)
            .ifPresentOrElse(configMap -> {
                // merge data
                var oldData = configMap.getData();
                var defaultData = defaultConfigMap.getData();
                var mergedData = SettingUtils.mergePatch(oldData, defaultData);
                configMap.setData(mergedData);
                client.update(configMap);
            }, () -> client.create(defaultConfigMap));
        log.info("Initialized config map {} for plugin {}", configMapName, pluginName);
    }

    private void resolveLoadLocation(Plugin plugin) {
        log.debug("Resolving load location for plugin {}", plugin.getMetadata().getName());

        // populate load location from annotations
        var pluginName = plugin.getMetadata().getName();
        var annotations = nullSafeAnnotations(plugin);
        var pluginPathAnno = annotations.get(PLUGIN_PATH);
        var status = plugin.statusNonNull();
        if (isDevelopmentMode(plugin)) {
            if (!isInDevEnvironment()) {
                throw new IllegalStateException(String.format("""
                    Cannot run the plugin %s with dev mode in non-development environment.\
                    """, pluginName));
            }
            log.debug("Plugin {} is in development mode", pluginName);
            if (StringUtils.isBlank(pluginPathAnno)) {
                // should never happen.
                throw new IllegalArgumentException(String.format("""
                        Please set plugin path annotation "%s" \
                        in development mode for plugin %s.""",
                    RUNTIME_MODE_ANNO, pluginName));
            }
            try {
                var loadLocation = ResourceUtils.getURL(pluginPathAnno).toURI();
                status.setLoadLocation(loadLocation);
            } catch (URISyntaxException | FileNotFoundException e) {
                throw new IllegalArgumentException(
                    "Invalid plugin path " + pluginPathAnno + " configured.", e);
            }
        } else {
            // reset annotation PLUGIN_PATH in non-dev mode
            pluginPathAnno = generateFileName(plugin);
            annotations.put(PLUGIN_PATH, pluginPathAnno);
            var pluginPath = Paths.get(pluginPathAnno);
            var pluginsRoot = getPluginsRoot();
            if (pluginPath.isAbsolute()) {
                if (pluginPath.startsWith(pluginsRoot)) {
                    // ensure the plugin path is a relative path.
                    annotations.put(PLUGIN_PATH, pluginsRoot.relativize(pluginPath).toString());
                }
            } else {
                pluginPath = pluginsRoot.resolve(pluginPath);
            }

            // delete old load location if changed.
            var oldLoadLocation = status.getLoadLocation();
            var newLoadLocation = pluginPath.toUri();
            if (oldLoadLocation != null && !Objects.equals(oldLoadLocation, newLoadLocation)) {
                // delete the old load location
                log.info("Deleting old plugin file {} for plugin {}, and new load location is {}.",
                    oldLoadLocation, pluginName, newLoadLocation);
                try {
                    var deleted = Files.deleteIfExists(Path.of(oldLoadLocation));
                    if (deleted) {
                        log.info("Deleted old plugin file {} for plugin {}.",
                            oldLoadLocation, pluginName);
                    }
                } catch (IOException e) {
                    log.warn("Failed to delete old plugin file {} for plugin {}",
                        oldLoadLocation, pluginName, e);
                }
            }
            status.setLoadLocation(newLoadLocation);
        }
        log.debug("Populated load location {} for plugin {}", status.getLoadLocation(), pluginName);
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new Plugin())
            .build();
    }

    void createOrUpdateReverseProxy(Plugin plugin) {
        String pluginName = plugin.getMetadata().getName();
        String reverseProxyName = buildReverseProxyName(pluginName);
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
                reverseProxy.getMetadata()
                    .setVersion(persisted.getMetadata().getVersion());
                client.update(reverseProxy);
            }, () -> client.create(reverseProxy));
    }

    private Path getPluginsRoot() {
        return pluginManager.getPluginsRoots().stream()
            .findFirst()
            .orElseThrow(
                () -> new IllegalStateException("pluginsRoots have not been initialized, yet."));
    }

    private boolean isInDevEnvironment() {
        return RuntimeMode.DEVELOPMENT.equals(pluginProperties.getRuntimeMode());
    }

    static String buildReverseProxyName(String pluginName) {
        return pluginName + "-system-generated-reverse-proxy";
    }

}
