package run.halo.app.core.extension.reconciler;

import static org.pf4j.util.FileUtils.isJarFile;
import static run.halo.app.extension.MetadataUtil.nullSafeAnnotations;
import static run.halo.app.extension.MetadataUtil.nullSafeLabels;
import static run.halo.app.plugin.PluginConst.DELETE_STAGE;
import static run.halo.app.plugin.PluginConst.PLUGIN_PATH;
import static run.halo.app.plugin.PluginConst.RELOAD_ANNO;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginRuntimeException;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.pf4j.RuntimeMode;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;
import org.springframework.web.util.UriComponentsBuilder;
import run.halo.app.core.extension.Plugin;
import run.halo.app.core.extension.ReverseProxy;
import run.halo.app.core.extension.Setting;
import run.halo.app.core.extension.theme.SettingUtils;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.Unstructured;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.Reconciler.Request;
import run.halo.app.infra.Condition;
import run.halo.app.infra.ConditionStatus;
import run.halo.app.infra.utils.FileUtils;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.infra.utils.PathUtils;
import run.halo.app.infra.utils.YamlUnstructuredLoader;
import run.halo.app.plugin.HaloPluginManager;
import run.halo.app.plugin.HaloPluginWrapper;
import run.halo.app.plugin.PluginConst;
import run.halo.app.plugin.PluginExtensionLoaderUtils;
import run.halo.app.plugin.PluginStartingError;
import run.halo.app.plugin.PluginUtils;
import run.halo.app.plugin.YamlPluginFinder;
import run.halo.app.plugin.event.PluginCreatedEvent;
import run.halo.app.plugin.resources.BundleResourceUtils;

/**
 * Plugin reconciler.
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
@Component
@AllArgsConstructor
public class PluginReconciler implements Reconciler<Request> {
    private static final String FINALIZER_NAME = "plugin-protection";
    private final ExtensionClient client;
    private final HaloPluginManager haloPluginManager;
    private final ApplicationEventPublisher eventPublisher;
    private final RetryTemplate retryTemplate = RetryTemplate.builder()
        .maxAttempts(20)
        .fixedBackoff(300)
        .retryOn(IllegalStateException.class)
        .build();

    @Override
    public Result reconcile(Request request) {
        try {
            return client.fetch(Plugin.class, request.name())
                .map(plugin -> {
                    if (plugin.getMetadata().getDeletionTimestamp() != null) {
                        cleanUpResourcesAndRemoveFinalizer(request.name());
                        return Result.doNotRetry();
                    }
                    addFinalizerIfNecessary(plugin);

                    // if true returned, it means it is not ready
                    if (readinessDetection(request.name())) {
                        return new Result(true, null);
                    }

                    reconcilePluginState(plugin.getMetadata().getName());
                    return Result.doNotRetry();
                })
                .orElse(Result.doNotRetry());
        } catch (DoNotRetryException e) {
            log.error("Failed to reconcile plugin: [{}]", request.name(), e);
            persistenceFailureStatus(request.name(), e);
            return Result.doNotRetry();
        } catch (Exception e) {
            persistenceFailureStatus(request.name(), e);
            throw e;
        }
    }

    private void updatePluginPathAnno(String name) {
        // TODO do it in a better way
        client.fetch(Plugin.class, name).ifPresent(plugin -> {
            Map<String, String> annotations = nullSafeAnnotations(plugin);
            String oldPluginPath = annotations.get(PLUGIN_PATH);
            String pluginPath = StringUtils.isBlank(oldPluginPath)
                ? Optional.ofNullable(plugin.statusNonNull().getLoadLocation())
                .map(URI::getPath)
                .orElseGet(() -> PluginUtils.generateFileName(plugin)) : oldPluginPath;
            String pluginPathAnno = resolvePluginPathForAnno(pluginPath);
            annotations.put(PLUGIN_PATH, pluginPathAnno);
            if (!StringUtils.equals(pluginPathAnno, oldPluginPath)) {
                client.update(plugin);
            }
        });
    }

    boolean readinessDetection(String name) {
        updatePluginPathAnno(name);
        return client.fetch(Plugin.class, name)
            .map(plugin -> {
                if (waitForSettingCreation(plugin)) {
                    return true;
                }
                recreateDefaultReverseProxy(plugin);

                updateStatus(name, status -> {
                    String logoUrl = generateAccessibleLogoUrl(plugin);
                    status.setLogo(logoUrl);

                    // If phase in status is not equal to plugin state, then reset plugin to
                    // stopped state and keep the state in memory consistent with the database
                    PluginState pluginState = getPluginWrapper(name).getPluginState();
                    status.setPhase(pluginState);
                    if (!Objects.equals(status.getPhase(), pluginState)) {
                        // stop and set phase
                        status.setPhase(haloPluginManager.stopPlugin(name));
                        status.setEntry(StringUtils.EMPTY);
                        status.setStylesheet(StringUtils.EMPTY);
                    }
                    return status;
                });
                return false;
            })
            .orElse(false);
    }

    String generateAccessibleLogoUrl(Plugin plugin) {
        String logo = plugin.getSpec().getLogo();
        if (StringUtils.isBlank(logo)) {
            return null;
        }
        if (!PathUtils.isAbsoluteUri(logo)) {
            String assetsPrefix =
                PluginConst.assertsRoutePrefix(plugin.getMetadata().getName());
            String versionedLogo =
                applyVersioningToStaticResource(logo, plugin.getSpec().getVersion());
            return PathUtils.combinePath(assetsPrefix, versionedLogo);
        }
        return logo;
    }

    Optional<Setting> lookupPluginSetting(String name, String settingName) {
        Assert.notNull(name, "Plugin name must not be null");
        Assert.notNull(settingName, "Setting name must not be null");
        PluginWrapper pluginWrapper = getPluginWrapper(name);
        var runtimeMode = getRuntimeMode(name);

        var resourceLoader =
            new DefaultResourceLoader(pluginWrapper.getPluginClassLoader());
        return PluginExtensionLoaderUtils.lookupExtensions(pluginWrapper.getPluginPath(),
                runtimeMode)
            .stream()
            .map(resourceLoader::getResource)
            .filter(Resource::exists)
            .map(resource -> new YamlUnstructuredLoader(resource).load())
            .flatMap(Collection::stream)
            .filter(unstructured -> {
                GroupVersionKind groupVersionKind =
                    GroupVersionKind.fromAPIVersionAndKind(unstructured.getApiVersion(),
                        unstructured.getKind());
                GroupVersionKind settingGvk = GroupVersionKind.fromExtension(Setting.class);
                return settingGvk.groupKind().equals(groupVersionKind.groupKind())
                    && settingName.equals(unstructured.getMetadata().getName());
            })
            .findFirst()
            .map(unstructured -> Unstructured.OBJECT_MAPPER.convertValue(unstructured,
                Setting.class));
    }

    boolean waitForSettingCreation(Plugin plugin) {
        final String pluginName = plugin.getMetadata().getName();

        final String settingName = plugin.getSpec().getSettingName();
        if (StringUtils.isBlank(settingName)) {
            return false;
        }

        var runtimeMode = getRuntimeMode(pluginName);
        Optional<Setting> settingOption = lookupPluginSetting(pluginName, settingName)
            .map(setting -> {
                // This annotation is added to prevent it from being deleted when stopped.
                Map<String, String> settingAnnotations = nullSafeAnnotations(setting);
                settingAnnotations.put(DELETE_STAGE, PluginConst.DeleteStage.UNINSTALL.name());
                return setting;
            })
            .map(settingFromYaml -> {
                client.fetch(Setting.class, settingName)
                    .ifPresentOrElse(setting -> {
                        settingFromYaml.getMetadata()
                            .setVersion(setting.getMetadata().getVersion());
                        client.update(settingFromYaml);
                    }, () -> client.create(settingFromYaml));
                return settingFromYaml;
            });

        // Fix gh-3224
        // Maybe Setting is being created and cannot be queried. so try again.
        if (settingOption.isEmpty()) {
            updateStatus(plugin.getMetadata().getName(), status -> {
                status.setPhase(PluginState.FAILED);
                var condition = Condition.builder()
                    .type("BackOff")
                    .reason("BackOff")
                    .message("Wait for setting [" + settingName + "] creation")
                    .status(ConditionStatus.FALSE)
                    .lastTransitionTime(Instant.now())
                    .build();
                Plugin.PluginStatus.nullSafeConditions(status)
                    .addAndEvictFIFO(condition);
                return status;
            });
            // need requeue
            return true;
        }

        final String configMapNameToUse = plugin.getSpec().getConfigMapName();
        if (StringUtils.isBlank(configMapNameToUse)) {
            return false;
        }

        SettingUtils.createOrUpdateConfigMap(client, settingName, configMapNameToUse);
        return false;
    }

    void startAction(String name) {
        stateTransition(name, currentState -> {
            boolean termination = false;
            switch (currentState) {
                case CREATED -> getPluginWrapper(name);
                case STARTED -> termination = true;
                // plugin can be started when it is stopped or failed
                case RESOLVED, STOPPED, FAILED -> doStart(name);
                default -> {
                }
            }
            return termination;
        }, PluginState.STARTED);
    }

    void stopAction(String name) {
        stateTransition(name, currentState -> {
            boolean termination = false;
            switch (currentState) {
                case CREATED -> getPluginWrapper(name);
                case RESOLVED, STARTED -> doStop(name);
                case FAILED, STOPPED -> termination = true;
                default -> {
                }
            }
            return termination;
        }, PluginState.STOPPED);
    }

    void stateTransition(String name, Function<PluginState, Boolean> stateAction,
        PluginState desiredState) {
        PluginState currentState = getPluginWrapper(name).getPluginState();
        int maxRetries = PluginState.values().length;
        for (int i = 0; i < maxRetries && currentState != desiredState; i++) {
            try {
                if (log.isDebugEnabled() && i > 2) {
                    log.debug("Plugin [{}] state transition from [{}] to [{}]", name, currentState,
                        desiredState);
                }
                // When true is returned, the status loop is ended directly
                if (BooleanUtils.isTrue(stateAction.apply(currentState))) {
                    break;
                }
                // update current state
                currentState = getPluginWrapper(name).getPluginState();
            } catch (Throwable e) {
                persistenceFailureStatus(name, e);
                throw e;
            }
        }

        if (currentState != desiredState) {
            log.error("Plugin [{}] state transition failed: {}", name,
                haloPluginManager.getPluginStartingError(name));
            throw new DoNotRetryException("Plugin [" + name + "] state transition from ["
                + currentState + "] to [" + desiredState + "] failed");
        }
    }

    void persistenceFailureStatus(String pluginName, Throwable e) {
        updateStatus(pluginName, status -> {
            PluginWrapper pluginWrapper = haloPluginManager.getPlugin(pluginName);
            if (pluginWrapper != null) {
                haloPluginManager.stopPlugin(pluginName);
                pluginWrapper.setPluginState(PluginState.FAILED);
                pluginWrapper.setFailedException(e);
            }

            status.setPhase(PluginState.FAILED);

            Condition condition = Condition.builder()
                .type(PluginState.FAILED.toString())
                .reason("UnexpectedState")
                .message(StringUtils.defaultString(e.getMessage()))
                .status(ConditionStatus.FALSE)
                .lastTransitionTime(Instant.now())
                .build();
            Plugin.PluginStatus.nullSafeConditions(status)
                .addAndEvictFIFO(condition);
            return status;
        });
    }

    @NonNull
    private PluginWrapper getPluginWrapper(String name) {
        PluginWrapper pluginWrapper = haloPluginManager.getPlugin(name);
        if (pluginWrapper == null) {
            ensurePluginLoaded(name);
            pluginWrapper = haloPluginManager.getPlugin(name);
        }

        if (pluginWrapper == null) {
            String errorMsg = "Plugin " + name + " not found in plugin manager.";
            updateStatus(name, status -> {
                status.setPhase(PluginState.FAILED);

                Condition condition = Condition.builder()
                    .type(PluginState.FAILED.toString())
                    .reason("PluginNotFound")
                    .message(errorMsg)
                    .status(ConditionStatus.FALSE)
                    .lastTransitionTime(Instant.now())
                    .build();
                Plugin.PluginStatus.nullSafeConditions(status)
                    .addAndEvictFIFO(condition);
                return status;
            });
            throw new DoNotRetryException(errorMsg);
        }
        return pluginWrapper;
    }

    void updateStatus(String name, UnaryOperator<Plugin.PluginStatus> operator) {
        try {
            retryTemplate.execute(callback -> {
                try {
                    doUpdateStatus(name, operator);
                } catch (Exception e) {
                    // trigger retry
                    throw new IllegalStateException(e);
                }
                return null;
            });
        } catch (Exception e) {
            PluginWrapper pluginWrapper = haloPluginManager.getPlugin(name);
            if (pluginWrapper != null) {
                haloPluginManager.stopPlugin(name);
                pluginWrapper.setPluginState(PluginState.FAILED);
            }
            throw e;
        }
    }

    void doUpdateStatus(String name, UnaryOperator<Plugin.PluginStatus> operator) {
        client.fetch(Plugin.class, name).ifPresent(plugin -> {
            Plugin.PluginStatus oldStatus = JsonUtils.deepCopy(plugin.statusNonNull());
            Plugin.PluginStatus newStatus =
                Optional.ofNullable(operator.apply(plugin.statusNonNull()))
                    .orElse(new Plugin.PluginStatus());
            plugin.setStatus(newStatus);

            URI loadLocation = newStatus.getLoadLocation();
            if (loadLocation == null) {
                String pluginPath = nullSafeAnnotations(plugin).get(PLUGIN_PATH);
                if (StringUtils.isNotBlank(pluginPath)) {
                    String absolutePath = buildPluginLocation(name, pluginPath);
                    loadLocation = toUri(absolutePath);
                } else {
                    loadLocation = getPluginWrapper(name).getPluginPath().toUri();
                }
                newStatus.setLoadLocation(loadLocation);
            }
            if (!Objects.equals(oldStatus, newStatus)) {
                client.update(plugin);
            }
        });
    }

    void doStart(String name) {
        PluginWrapper pluginWrapper = getPluginWrapper(name);
        // Check if this plugin version is match requires param.
        if (!haloPluginManager.validatePluginVersion(pluginWrapper)) {
            PluginDescriptor descriptor = pluginWrapper.getDescriptor();
            String message = String.format(
                "Plugin requires a minimum system version of [%s], and you have [%s].",
                descriptor.getRequires(), haloPluginManager.getSystemVersion());
            throw new IllegalStateException(message);
        }

        if (PluginState.DISABLED.equals(pluginWrapper.getPluginState())) {
            throw new IllegalStateException(
                "The plugin is disabled for some reason and cannot be started.");
        }

        PluginState currentState = haloPluginManager.startPlugin(name);
        if (!PluginState.STARTED.equals(currentState)) {
            PluginStartingError staringErrorInfo = getStaringErrorInfo(name);
            log.debug("Failed to start plugin: " + staringErrorInfo.getDevMessage(),
                pluginWrapper.getFailedException());
            throw new IllegalStateException(staringErrorInfo.getMessage(),
                pluginWrapper.getFailedException());
        }

        String pluginVersion = pluginWrapper.getDescriptor().getVersion();
        updateStatus(name, status -> {
            status.setLastStartTime(Instant.now());

            String jsBundlePath =
                BundleResourceUtils.getJsBundlePath(haloPluginManager, name);
            jsBundlePath = applyVersioningToStaticResource(jsBundlePath, pluginVersion);
            status.setEntry(jsBundlePath);

            String cssBundlePath =
                BundleResourceUtils.getCssBundlePath(haloPluginManager, name);
            cssBundlePath = applyVersioningToStaticResource(cssBundlePath, pluginVersion);
            status.setStylesheet(cssBundlePath);

            status.setPhase(currentState);
            Condition condition = Condition.builder()
                .type(PluginState.STARTED.toString())
                .reason(PluginState.STARTED.toString())
                .message("Started successfully")
                .lastTransitionTime(Instant.now())
                .status(ConditionStatus.TRUE)
                .build();
            Plugin.PluginStatus.nullSafeConditions(status)
                .addAndEvictFIFO(condition);
            return status;
        });
    }

    private String applyVersioningToStaticResource(@Nullable String path, String pluginVersion) {
        if (StringUtils.isNotBlank(path)) {
            return UriComponentsBuilder.fromUriString(path)
                .queryParam("version", pluginVersion)
                .build().toString();
        }
        return path;
    }

    PluginStartingError getStaringErrorInfo(String name) {
        PluginStartingError startingError =
            haloPluginManager.getPluginStartingError(name);
        if (startingError == null) {
            startingError = PluginStartingError.of(name, "Unknown error", "");
        }
        return startingError;
    }

    void doStop(String name) {
        PluginState currentState = haloPluginManager.stopPlugin(name);
        if (!PluginState.STOPPED.equals(currentState)) {
            throw new IllegalStateException("Failed to stop plugin: " + name);
        }
        updateStatus(name, status -> {
            status.setPhase(currentState);
            // reset js bundle path
            status.setStylesheet(StringUtils.EMPTY);
            status.setEntry(StringUtils.EMPTY);

            Condition condition = Condition.builder()
                .type(PluginState.STOPPED.toString())
                .reason(PluginState.STOPPED.toString())
                .message("Stopped successfully")
                .lastTransitionTime(Instant.now())
                .status(ConditionStatus.TRUE)
                .build();
            Plugin.PluginStatus.nullSafeConditions(status)
                .addAndEvictFIFO(condition);
            return status;
        });
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new Plugin())
            .build();
    }

    private void reconcilePluginState(String name) {
        client.fetch(Plugin.class, name).ifPresent(plugin -> {
            // reload detection
            Map<String, String> annotations = nullSafeAnnotations(plugin);
            if (annotations.containsKey(RELOAD_ANNO)) {
                reload(plugin);
                // update will requeue to make next reconciliation
                return;
            }

            // Transition plugin status if necessary
            if (shouldReconcileStartState(plugin)) {
                startAction(name);
            }

            if (shouldReconcileStopState(plugin)) {
                stopAction(name);
            }
        });
    }

    void reload(Plugin plugin) {
        String newPluginPath = nullSafeAnnotations(plugin).get(RELOAD_ANNO);
        if (StringUtils.isBlank(newPluginPath)) {
            return;
        }
        final String pluginName = plugin.getMetadata().getName();
        URI oldPluginLocation = plugin.statusNonNull().getLoadLocation();
        if (shouldDeleteFile(newPluginPath, oldPluginLocation)) {
            try {
                // delete old plugin jar file
                Files.deleteIfExists(Paths.get(oldPluginLocation.getPath()));
            } catch (IOException e) {
                throw new PluginRuntimeException(e);
            }
        }
        final var pluginFinder = new YamlPluginFinder();
        final var pluginInPath = pluginFinder.find(toPath(newPluginPath));
        client.fetch(Plugin.class, plugin.getMetadata().getName())
            .ifPresent(persisted -> {
                if (!persisted.getMetadata().getName()
                    .equals(pluginInPath.getMetadata().getName())) {
                    throw new DoNotRetryException("Plugin name is not match, skip reload.");
                }
                persisted.setSpec(pluginInPath.getSpec());
                // merge annotations and labels
                Map<String, String> newAnnotations = nullSafeAnnotations(persisted);
                newAnnotations.putAll(nullSafeAnnotations(pluginInPath));

                newAnnotations.put(PLUGIN_PATH, resolvePluginPathForAnno(newPluginPath));
                newAnnotations.remove(RELOAD_ANNO);
                nullSafeLabels(persisted).putAll(nullSafeLabels(pluginInPath));
                persisted.statusNonNull().setLoadLocation(toUri(newPluginPath));

                // reload
                haloPluginManager.reloadPluginWithPath(pluginName, toPath(newPluginPath));
                // update plugin
                client.update(persisted);
            });
    }

    String resolvePluginPathForAnno(String pluginPathString) {
        Path pluginsRoot = toPath(haloPluginManager.getPluginsRoot().toString());
        Path pluginPath = toPath(pluginPathString);
        if (pluginPath.startsWith(pluginsRoot)) {
            return pluginsRoot.relativize(pluginPath).toString();
        }
        return pluginPath.toString();
    }

    /**
     * Returns an absolute plugin path.
     * if a plugin path is absolute, use it directly in development mode.
     * otherwise, combine a plugin path with a plugin root path.
     * Note: plugin location without a scheme
     */
    String buildPluginLocation(String name, String pluginPathString) {
        Assert.notNull(name, "Plugin name must not be null");
        Assert.notNull(pluginPathString, "Plugin path must not be null");
        Path pluginsRoot = toPath(haloPluginManager.getPluginsRoot().toString());
        Path pluginPath = toPath(pluginPathString);
        // if a plugin path is absolute, use it directly in development mode
        if (pluginPath.isAbsolute()) {
            if (!isDevelopmentMode(name) && !pluginPath.startsWith(pluginsRoot)) {
                throw new DoNotRetryException(
                    "Plugin path must be relative path or relative to plugin root path.");
            }
            return pluginPath.toString();
        }
        var result = pluginsRoot.resolve(pluginPath);
        if (!isDevelopmentMode(name)) {
            FileUtils.checkDirectoryTraversal(pluginsRoot, result);
        }
        return result.toString();
    }

    boolean shouldDeleteFile(String newPluginPath, URI oldPluginLocation) {
        if (oldPluginLocation == null) {
            return false;
        }

        if (oldPluginLocation.equals(toUri(newPluginPath))) {
            return false;
        }
        return isJarFile(Paths.get(oldPluginLocation));
    }

    private void ensurePluginLoaded(String name) {
        client.fetch(Plugin.class, name).ifPresent(plugin -> {
            PluginWrapper pluginWrapper = haloPluginManager.getPlugin(name);
            if (pluginWrapper != null) {
                return;
            }
            Path pluginLocation = determinePluginLocation(plugin);
            if (!Files.exists(pluginLocation)) {
                return;
            }
            haloPluginManager.loadPlugin(pluginLocation);
        });
    }

    Path toPath(String pathString) {
        if (StringUtils.isBlank(pathString)) {
            return null;
        }
        try {
            var pathURL = new URL(pathString);
            if (!ResourceUtils.isFileURL(pathURL)) {
                throw new IllegalArgumentException("The path cannot be resolved to absolute file"
                    + " path because it does not reside in the file system: "
                    + pathString);
            }
            var pathURI = ResourceUtils.toURI(pathURL);
            return Paths.get(pathURI);
        } catch (MalformedURLException | URISyntaxException ignored) {
            // the given path string is not a valid URL.
        }
        return Paths.get(pathString);
    }

    URI toUri(String pathString) {
        if (StringUtils.isBlank(pathString)) {
            throw new IllegalArgumentException("Path string must not be blank");
        }
        return Paths.get(pathString).toUri();
    }

    boolean shouldReconcileStartState(Plugin plugin) {
        PluginWrapper pluginWrapper = getPluginWrapper(plugin.getMetadata().getName());
        if (BooleanUtils.isNotTrue(plugin.getSpec().getEnabled())) {
            return false;
        }
        // phase is not started, or plugin state is not started should start
        return !PluginState.STARTED.equals(plugin.statusNonNull().getPhase())
            || !PluginState.STARTED.equals(pluginWrapper.getPluginState());
    }

    boolean shouldReconcileStopState(Plugin plugin) {
        PluginWrapper pluginWrapper = getPluginWrapper(plugin.getMetadata().getName());
        if (BooleanUtils.isNotFalse(plugin.getSpec().getEnabled())) {
            return false;
        }
        // phase is not stopped, or plugin state is not stopped should stop
        return !PluginState.STOPPED.equals(plugin.statusNonNull().getPhase())
            || !PluginState.STOPPED.equals(pluginWrapper.getPluginState());
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

                eventPublisher.publishEvent(
                    new PluginCreatedEvent(this, plugin.getMetadata().getName()));
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
        String name = plugin.getMetadata().getName();
        // delete initial reverse proxy
        String initialReverseProxyName = initialReverseProxyName(name);
        client.fetch(ReverseProxy.class, initialReverseProxyName)
            .ifPresent(client::delete);
        retryTemplate.execute(callback -> {
            client.fetch(ReverseProxy.class, initialReverseProxyName).ifPresent(item -> {
                throw new IllegalStateException(
                    "Waiting for reverseproxy [" + initialReverseProxyName + "] to be deleted.");
            });
            return null;
        });

        // delete plugin setting form
        String settingName = plugin.getSpec().getSettingName();
        if (StringUtils.isNotBlank(settingName)) {
            client.fetch(Setting.class, settingName)
                .ifPresent(client::delete);
            retryTemplate.execute(callback -> {
                client.fetch(Setting.class, settingName).ifPresent(setting -> {
                    throw new IllegalStateException("Waiting for setting to be deleted.");
                });
                return null;
            });
        }

        PluginWrapper pluginWrapper = haloPluginManager.getPlugin(name);
        if (pluginWrapper != null) {
            // pluginWrapper must not be null in below code
            // stop and unload plugin, see also PluginBeforeStopSyncListener
            if (!haloPluginManager.deletePlugin(name)) {
                throw new IllegalStateException("Failed to delete plugin: " + name);
            }
        }
    }

    @NonNull
    Path determinePluginLocation(Plugin plugin) {
        String pluginPath = nullSafeAnnotations(plugin).get(PLUGIN_PATH);
        String name = plugin.getMetadata().getName();
        if (StringUtils.isBlank(pluginPath)) {
            URI loadLocation = plugin.statusNonNull().getLoadLocation();
            if (loadLocation != null) {
                pluginPath = loadLocation.getPath();
            } else {
                throw new DoNotRetryException(
                    "Cannot determine plugin path for plugin: " + name);
            }
        }
        String pluginLocation = buildPluginLocation(name, pluginPath);
        return Paths.get(pluginLocation);
    }

    void recreateDefaultReverseProxy(Plugin plugin) {
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
                reverseProxy.getMetadata()
                    .setVersion(persisted.getMetadata().getVersion());
                client.update(reverseProxy);
            }, () -> client.create(reverseProxy));
    }

    static class DoNotRetryException extends PluginRuntimeException {
        public DoNotRetryException(String message) {
            super(message);
        }
    }

    static String initialReverseProxyName(String pluginName) {
        return pluginName + "-system-generated-reverse-proxy";
    }

    private boolean isDevelopmentMode(String name) {
        return RuntimeMode.DEVELOPMENT.equals(getRuntimeMode(name));
    }

    private RuntimeMode getRuntimeMode(String name) {
        var pluginWrapper = haloPluginManager.getPlugin(name);
        if (pluginWrapper == null) {
            return haloPluginManager.getRuntimeMode();
        }
        if (pluginWrapper instanceof HaloPluginWrapper haloPluginWrapper) {
            return haloPluginWrapper.getRuntimeMode();
        }
        return Files.isDirectory(pluginWrapper.getPluginPath())
            ? RuntimeMode.DEVELOPMENT
            : RuntimeMode.DEPLOYMENT;
    }
}
