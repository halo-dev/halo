package run.halo.app.core.extension.reconciler;

import static run.halo.app.core.extension.Plugin.PluginStatus.nullSafeConditions;
import static run.halo.app.extension.ExtensionUtil.addFinalizers;
import static run.halo.app.extension.ExtensionUtil.removeFinalizers;
import static run.halo.app.extension.MetadataUtil.nullSafeAnnotations;
import static run.halo.app.plugin.PluginConst.PLUGIN_PATH;
import static run.halo.app.plugin.PluginConst.RELOAD_ANNO;
import static run.halo.app.plugin.PluginConst.REQUEST_TO_UNLOAD_LABEL;
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
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.PluginDependency;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.pf4j.RuntimeMode;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
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
import run.halo.app.infra.ConditionList;
import run.halo.app.infra.ConditionStatus;
import run.halo.app.infra.utils.PathUtils;
import run.halo.app.infra.utils.YamlUnstructuredLoader;
import run.halo.app.plugin.PluginConst;
import run.halo.app.plugin.PluginProperties;
import run.halo.app.plugin.SpringPluginManager;

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

    private static final Set<String> UNUSED_ANNOTATIONS =
        Set.of("plugin.halo.run/dependents-snapshot");

    private final ExtensionClient client;

    private final SpringPluginManager pluginManager;

    private final PluginProperties pluginProperties;

    private Clock clock;

    public PluginReconciler(ExtensionClient client, SpringPluginManager pluginManager,
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
                    if (!checkDependents(plugin)) {
                        client.update(plugin);
                        // Check dependents every 10 seconds
                        return Result.requeue(Duration.ofSeconds(10));
                    }
                    // CleanUp resources and remove finalizer.
                    if (removeFinalizers(plugin.getMetadata(), Set.of(FINALIZER_NAME))) {
                        cleanupResources(plugin);
                        syncPluginState(plugin);
                        client.update(plugin);
                    }
                    return Result.doNotRetry();
                }
                addFinalizers(plugin.getMetadata(), Set.of(FINALIZER_NAME));
                removeUnusedAnnotations(plugin);

                var status = plugin.getStatus();
                if (status == null) {
                    status = new Plugin.PluginStatus();
                    plugin.setStatus(status);
                }
                // reset phase to pending
                status.setPhase(Plugin.Phase.PENDING);
                // init condition list if not exists
                if (status.getConditions() == null) {
                    status.setConditions(new ConditionList());
                }

                var steps = new LinkedList<Supplier<Result>>();
                steps.add(() -> resolveLoadLocation(plugin));
                steps.add(() -> loadOrReload(plugin));
                steps.add(() -> createOrUpdateSetting(plugin));
                steps.add(() -> createOrUpdateReverseProxy(plugin));
                steps.add(() -> resolveStaticResources(plugin));
                if (requestToEnable(plugin)) {
                    steps.add(() -> enablePlugin(plugin));
                } else {
                    steps.add(() -> disablePlugin(plugin));
                }

                Result result = null;
                try {
                    for (var step : steps) {
                        result = step.get();
                        if (result != null) {
                            break;
                        }
                    }
                    return result;
                } catch (Exception e) {
                    status.getConditions().addAndEvictFIFO(Condition.builder()
                        .type(ConditionType.READY)
                        .status(ConditionStatus.FALSE)
                        .reason(ConditionReason.SYSTEM_ERROR)
                        .message(e.getMessage())
                        .lastTransitionTime(clock.instant())
                        .build());
                    status.setPhase(Plugin.Phase.UNKNOWN);
                    throw e;
                } finally {
                    var pw = pluginManager.getPlugin(plugin.getMetadata().getName());
                    if (pw != null) {
                        status.setLastProbeState(pw.getPluginState());
                    }
                    client.update(plugin);
                }
            })
            .orElseGet(Result::doNotRetry);
    }

    private void removeUnusedAnnotations(Plugin plugin) {
        var annotations = plugin.getMetadata().getAnnotations();
        if (annotations != null) {
            UNUSED_ANNOTATIONS.forEach(annotations::remove);
        }
    }

    private boolean checkDependents(Plugin plugin) {
        var pluginId = plugin.getMetadata().getName();
        var dependents = pluginManager.getDependents(pluginId);
        if (CollectionUtils.isEmpty(dependents)) {
            return true;
        }
        var status = plugin.statusNonNull();
        var condition = Condition.builder()
            .type(ConditionType.PROGRESSING)
            .status(ConditionStatus.UNKNOWN)
            .reason(ConditionReason.WAIT_FOR_DEPENDENTS_DELETED)
            .message(
                "The plugin has dependents %s, please delete them first."
                    .formatted(dependents.stream().map(PluginWrapper::getPluginId).toList())
            )
            .lastTransitionTime(clock.instant())
            .build();
        var conditions = nullSafeConditions(status);
        removeConditionBy(conditions, ConditionType.INITIALIZED);
        removeConditionBy(conditions, ConditionType.READY);
        conditions.addAndEvictFIFO(condition);
        status.setPhase(Plugin.Phase.UNKNOWN);
        return false;
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

    private static String requestToUnload(Plugin plugin) {
        var labels = plugin.getMetadata().getLabels();
        if (labels == null) {
            return null;
        }
        return labels.get(REQUEST_TO_UNLOAD_LABEL);
    }

    private static boolean requestToReload(Plugin plugin) {
        var annotations = plugin.getMetadata().getAnnotations();
        return annotations != null && annotations.get(RELOAD_ANNO) != null;
    }

    private static void removeRequestToReload(Plugin plugin) {
        var annotations = plugin.getMetadata().getAnnotations();
        if (annotations != null) {
            annotations.remove(RELOAD_ANNO);
        }
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

    private Result enablePlugin(Plugin plugin) {
        // start the plugin
        var pluginName = plugin.getMetadata().getName();
        log.info("Starting plugin {}", pluginName);
        var status = plugin.getStatus();
        status.setPhase(Plugin.Phase.STARTING);

        // check if the parent plugin is started
        var pw = pluginManager.getPlugin(pluginName);
        var unstartedDependencies = pw.getDescriptor().getDependencies()
            .stream()
            .filter(pd -> {
                if (pd.isOptional()) {
                    return false;
                }
                var parent = pluginManager.getPlugin(pd.getPluginId());
                return parent == null || !PluginState.STARTED.equals(parent.getPluginState());
            })
            .map(PluginDependency::getPluginId)
            .toList();
        var conditions = status.getConditions();
        if (!CollectionUtils.isEmpty(unstartedDependencies)) {
            removeConditionBy(conditions, ConditionType.READY);
            conditions.addAndEvictFIFO(Condition.builder()
                .type(ConditionType.PROGRESSING)
                .status(ConditionStatus.UNKNOWN)
                .reason(ConditionReason.WAIT_FOR_DEPENDENCIES_STARTED)
                .message("Wait for parent plugins " + unstartedDependencies + " to be started")
                .lastTransitionTime(clock.instant())
                .build());
            status.setPhase(Plugin.Phase.UNKNOWN);
            return Result.requeue(Duration.ofSeconds(1));
        }

        try {
            var pluginState = pluginManager.startPlugin(pluginName);
            if (!PluginState.STARTED.equals(pluginState)) {
                throw new IllegalStateException("""
                    Failed to start plugin %s(%s).\
                    """.formatted(pluginName, pluginState));
            }
        } catch (Exception e) {
            conditions.addAndEvictFIFO(Condition.builder()
                .type(ConditionType.READY)
                .status(ConditionStatus.FALSE)
                .reason(ConditionReason.START_ERROR)
                .message(e.getMessage())
                .lastTransitionTime(clock.instant())
                .build());
            status.setPhase(Plugin.Phase.FAILED);
            return Result.doNotRetry();
        }

        removeConditionBy(conditions, ConditionType.PROGRESSING);
        status.setLastStartTime(clock.instant());
        conditions.addAndEvictFIFO(Condition.builder()
            .type(ConditionType.READY)
            .status(ConditionStatus.TRUE)
            .reason(ConditionReason.STARTED)
            .message("Started successfully")
            .lastTransitionTime(clock.instant())
            .build());
        status.setPhase(Plugin.Phase.STARTED);

        log.info("Started plugin {}", pluginName);
        return null;
    }

    private Result disablePlugin(Plugin plugin) {
        var pluginName = plugin.getMetadata().getName();
        var status = plugin.getStatus();
        if (pluginManager.getPlugin(pluginName) != null) {
            // check if the plugin has children
            var dependents = pluginManager.getDependents(pluginName)
                .stream()
                .filter(pw -> PluginState.STARTED.equals(pw.getPluginState()))
                .map(PluginWrapper::getPluginId)
                .toList();
            var conditions = status.getConditions();
            if (!CollectionUtils.isEmpty(dependents)) {
                removeConditionBy(conditions, ConditionType.READY);
                conditions.addAndEvictFIFO(Condition.builder()
                    .type(ConditionType.PROGRESSING)
                    .status(ConditionStatus.UNKNOWN)
                    .reason(ConditionReason.WAIT_FOR_DEPENDENTS_DISABLED)
                    .message("Wait for children plugins " + dependents + " to be disabled")
                    .lastTransitionTime(clock.instant())
                    .build());
                status.setPhase(Plugin.Phase.DISABLING);
                return Result.requeue(Duration.ofSeconds(1));
            }
            try {
                pluginManager.disablePlugin(pluginName);
            } catch (Exception e) {
                conditions.addAndEvictFIFO(Condition.builder()
                    .type(ConditionType.READY)
                    .status(ConditionStatus.FALSE)
                    .reason(ConditionReason.DISABLE_ERROR)
                    .message(e.getMessage())
                    .lastTransitionTime(clock.instant())
                    .build());
                status.setPhase(Plugin.Phase.FAILED);
                return Result.doNotRetry();
            }
        }
        var conditions = plugin.getStatus().getConditions();
        removeConditionBy(conditions, ConditionType.PROGRESSING);
        conditions.addAndEvictFIFO(Condition.builder()
            .type(ConditionType.READY)
            .status(ConditionStatus.TRUE)
            .reason(ConditionReason.DISABLED)
            .lastTransitionTime(clock.instant())
            .build());
        plugin.statusNonNull().setPhase(Plugin.Phase.DISABLED);
        return null;
    }

    private static boolean requestToEnable(Plugin plugin) {
        var enabled = plugin.getSpec().getEnabled();
        return enabled != null && enabled;
    }

    private Result resolveStaticResources(Plugin plugin) {
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
        return null;
    }

    private Result loadOrReload(Plugin plugin) {
        var pluginName = plugin.getMetadata().getName();
        var p = pluginManager.getPlugin(pluginName);
        var conditions = plugin.getStatus().getConditions();

        var requestToUnloadBy = requestToUnload(plugin);
        var requestToUnload = requestToUnloadBy != null;
        var notFullyLoaded = p != null && pluginManager.getUnresolvedPlugins().contains(p);
        var alreadyLoaded = p != null && pluginManager.getResolvedPlugins().contains(p);

        var requestToReload = requestToReload(plugin);
        // TODO Check load location
        var shouldUnload = requestToUnload || requestToReload || notFullyLoaded;
        if (shouldUnload) {
            // check if the plugin is already loaded or not fully loaded.
            if (alreadyLoaded || notFullyLoaded) {
                // get all dependencies
                var dependents = requestToUnloadChildren(pluginName);
                if (!CollectionUtils.isEmpty(dependents)) {
                    removeConditionBy(conditions, ConditionType.READY);
                    conditions.addAndEvictFIFO(Condition.builder()
                        .type(ConditionType.PROGRESSING)
                        .status(ConditionStatus.UNKNOWN)
                        .reason(ConditionReason.WAIT_FOR_DEPENDENTS_UNLOADED)
                        .message("Wait for children plugins " + dependents + "to be unloaded")
                        .lastTransitionTime(clock.instant())
                        .build());
                    plugin.getStatus().setPhase(Plugin.Phase.UNKNOWN);
                    // wait for children plugins unloaded
                    // retry after 1 second
                    return Result.requeue(Duration.ofSeconds(1));
                }

                // unload the plugin exactly
                pluginManager.unloadPlugin(pluginName);

                removeConditionBy(conditions, ConditionType.INITIALIZED);
                removeConditionBy(conditions, ConditionType.PROGRESSING);
                removeConditionBy(conditions, ConditionType.READY);

                cancelUnloadRequest(pluginName);
                p = null;
            }

            // ensure removing the reload annotation after the plugin is unloaded
            if (requestToUnload) {
                // skip loading and wait for removing the annotation by other plugins.
                var status = plugin.getStatus();
                status.getConditions().addAndEvictFIFO(Condition.builder()
                    .type(ConditionType.INITIALIZED)
                    .status(ConditionStatus.FALSE)
                    .reason(ConditionReason.REQUEST_TO_UNLOAD)
                    .message("Request to unload by " + requestToUnloadBy)
                    .lastTransitionTime(clock.instant())
                    .build());
                return Result.doNotRetry();
            }

            if (requestToReload) {
                removeRequestToReload(plugin);
            }
        }

        // check dependencies before loading
        var unresolvedParentPlugins = plugin.getSpec().getPluginDependencies().keySet()
            .stream()
            .filter(dependency -> {
                var parentPlugin = pluginManager.getPlugin(dependency);
                return parentPlugin == null
                    || pluginManager.getUnresolvedPlugins().contains(parentPlugin);
            })
            .sorted()
            .toList();
        if (!unresolvedParentPlugins.isEmpty()) {
            // requeue if the parent plugin is not loaded yet.
            removeConditionBy(conditions, ConditionType.INITIALIZED);
            removeConditionBy(conditions, ConditionType.READY);
            conditions.addAndEvictFIFO(Condition.builder()
                .type(ConditionType.PROGRESSING)
                .status(ConditionStatus.UNKNOWN)
                .reason(ConditionReason.WAIT_FOR_DEPENDENCIES_LOADED)
                .message("Wait for parent plugins " + unresolvedParentPlugins + " to be loaded")
                .lastTransitionTime(clock.instant())
                .build());
            plugin.getStatus().setPhase(Plugin.Phase.UNKNOWN);
            return Result.requeue(Duration.ofSeconds(1));
        }

        if (p == null) {
            var loadLocation = plugin.getStatus().getLoadLocation();
            log.info("Loading plugin {} from {}", pluginName, loadLocation);
            pluginManager.loadPlugin(Paths.get(loadLocation));
            log.info("Loaded plugin {} from {}", pluginName, loadLocation);
        }

        conditions.addAndEvictFIFO(Condition.builder()
            .type(ConditionType.INITIALIZED)
            .status(ConditionStatus.TRUE)
            .reason(ConditionReason.LOADED)
            .lastTransitionTime(clock.instant())
            .build());
        plugin.getStatus().setPhase(Plugin.Phase.RESOLVED);
        return null;
    }

    private Result createOrUpdateSetting(Plugin plugin) {
        log.info("Initializing setting and config map for plugin {}",
            plugin.getMetadata().getName());
        var settingName = plugin.getSpec().getSettingName();
        if (StringUtils.isBlank(settingName)) {
            // do nothing if no setting name provided.
            return null;
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
            return null;
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
        return null;
    }

    private Result resolveLoadLocation(Plugin plugin) {
        log.debug("Resolving load location for plugin {}", plugin.getMetadata().getName());

        // populate load location from annotations
        var pluginName = plugin.getMetadata().getName();
        var annotations = nullSafeAnnotations(plugin);
        var pluginPathAnno = annotations.get(PLUGIN_PATH);
        var status = plugin.statusNonNull();
        if (isDevelopmentMode(plugin)) {
            if (!isInDevEnvironment()) {
                status.getConditions().addAndEvictFIFO(Condition.builder()
                    .type(ConditionType.INITIALIZED)
                    .status(ConditionStatus.FALSE)
                    .reason(ConditionReason.INVALID_RUNTIME_MODE)
                    .message("""
                        Cannot run the plugin with development mode in non-development environment.\
                        """)
                    .lastTransitionTime(clock.instant())
                    .build());
                status.setPhase(Plugin.Phase.UNKNOWN);
                return Result.doNotRetry();
            }
            log.debug("Plugin {} is in development mode", pluginName);
            if (StringUtils.isBlank(pluginPathAnno)) {
                status.getConditions().addAndEvictFIFO(Condition.builder()
                    .type(ConditionType.INITIALIZED)
                    .status(ConditionStatus.FALSE)
                    .reason(ConditionReason.PLUGIN_PATH_NOT_SET)
                    .message("""
                        Plugin path annotation is not set. \
                        Please set plugin path annotation "%s" in development mode.\
                        """.formatted(PLUGIN_PATH))
                    .build());
                return Result.doNotRetry();
            }
            try {
                var loadLocation = ResourceUtils.getURL(pluginPathAnno).toURI();
                status.setLoadLocation(loadLocation);
            } catch (URISyntaxException | FileNotFoundException e) {
                // TODO Refactor this using event in the future.
                var condition = Condition.builder()
                    .type(ConditionType.INITIALIZED)
                    .status(ConditionStatus.FALSE)
                    .reason(ConditionReason.INVALID_PLUGIN_PATH)
                    .message("Invalid plugin path " + pluginPathAnno + " configured.")
                    .lastTransitionTime(clock.instant())
                    .build();
                status.getConditions().addAndEvictFIFO(condition);
                status.setPhase(Plugin.Phase.UNKNOWN);
                return Result.doNotRetry();
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

        status.getConditions().addAndEvictFIFO(Condition.builder()
            .type(ConditionType.INITIALIZED)
            .status(ConditionStatus.TRUE)
            .reason(ConditionReason.LOAD_LOCATION_RESOLVED)
            .lastTransitionTime(clock.instant())
            .build());
        status.setPhase(Plugin.Phase.RESOLVED);
        log.debug("Populated load location {} for plugin {}", status.getLoadLocation(), pluginName);
        return null;
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new Plugin())
            .build();
    }

    private Result createOrUpdateReverseProxy(Plugin plugin) {
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
        return null;
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

    private List<String> requestToUnloadChildren(String pluginName) {
        // get all dependencies
        var dependents = pluginManager.getDependents(pluginName)
            .stream()
            .map(PluginWrapper::getPluginId)
            .toList();
        // request all dependents to reload.
        dependents.forEach(dependent -> client.fetch(Plugin.class, dependent)
            .ifPresent(childPlugin -> {
                var labels = childPlugin.getMetadata().getLabels();
                if (labels == null) {
                    labels = new HashMap<>();
                    childPlugin.getMetadata().setLabels(labels);
                }
                var label = labels.get(REQUEST_TO_UNLOAD_LABEL);
                if (!pluginName.equals(label)) {
                    labels.put(REQUEST_TO_UNLOAD_LABEL, pluginName);
                    client.update(childPlugin);
                }
            }));
        return dependents;
    }

    private void cancelUnloadRequest(String pluginName) {
        // remove label REQUEST_TO_UNLOAD_LABEL
        // TODO Use index mechanism
        Predicate<Plugin> filter = aplugin -> {
            var labels = aplugin.getMetadata().getLabels();
            return labels != null && pluginName.equals(labels.get(REQUEST_TO_UNLOAD_LABEL));
        };

        client.list(Plugin.class, filter, null)
            .forEach(aplugin -> {
                var labels = aplugin.getMetadata().getLabels();
                if (labels != null && labels.remove(REQUEST_TO_UNLOAD_LABEL) != null) {
                    client.update(aplugin);
                }
            });

    }

    private static void removeConditionBy(ConditionList conditions, String type) {
        conditions.removeIf(condition -> Objects.equals(type, condition.getType()));
    }

    public static class ConditionType {
        /**
         * Indicates whether the plugin is initialized.
         */
        public static final String INITIALIZED = "Initialized";

        /**
         * Indicates whether the plugin is starting, disabling or deleting.
         */
        public static final String PROGRESSING = "Progressing";

        /**
         * Indicates whether the plugin is ready.
         */
        public static final String READY = "Ready";
    }

    public static class ConditionReason {
        public static final String LOAD_LOCATION_RESOLVED = "LoadLocationResolved";
        public static final String INVALID_PLUGIN_PATH = "InvalidPluginPath";

        public static final String WAIT_FOR_DEPENDENCIES_STARTED = "WaitForDependenciesStarted";
        public static final String WAIT_FOR_DEPENDENCIES_LOADED = "WaitForDependenciesLoaded";

        public static final String WAIT_FOR_DEPENDENTS_DELETED = "WaitForDependentsDeleted";
        public static final String WAIT_FOR_DEPENDENTS_DISABLED = "WaitForDependentsDisabled";
        public static final String WAIT_FOR_DEPENDENTS_UNLOADED = "WaitForDependentsUnloaded";

        public static final String STARTED = "Started";
        public static final String DISABLED = "Disabled";
        public static final String SYSTEM_ERROR = "SystemError";
        public static final String REQUEST_TO_UNLOAD = "RequestToUnload";
        public static final String LOADED = "Loaded";
        public static final String START_ERROR = "StartError";
        public static final String DISABLE_ERROR = "DisableError";
        public static final String INVALID_RUNTIME_MODE = "InvalidRuntimeMode";
        public static final String PLUGIN_PATH_NOT_SET = "PluginPathNotSet";
    }

}
