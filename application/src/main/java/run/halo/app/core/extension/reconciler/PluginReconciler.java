package run.halo.app.core.extension.reconciler;

import static run.halo.app.plugin.PluginConst.DELETE_STAGE;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.PluginDescriptor;
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
import org.springframework.web.util.UriComponentsBuilder;
import run.halo.app.core.extension.Plugin;
import run.halo.app.core.extension.ReverseProxy;
import run.halo.app.core.extension.Setting;
import run.halo.app.core.extension.theme.SettingUtils;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.Unstructured;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.Reconciler.Request;
import run.halo.app.infra.Condition;
import run.halo.app.infra.ConditionStatus;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.infra.utils.PathUtils;
import run.halo.app.infra.utils.YamlUnstructuredLoader;
import run.halo.app.plugin.HaloPluginManager;
import run.halo.app.plugin.PluginConst;
import run.halo.app.plugin.PluginExtensionLoaderUtils;
import run.halo.app.plugin.PluginNotFoundException;
import run.halo.app.plugin.PluginStartingError;
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
    }

    boolean readinessDetection(String name) {
        return client.fetch(Plugin.class, name)
            .map(plugin -> {
                if (waitForSettingCreation(plugin)) {
                    return true;
                }
                createInitialReverseProxyIfNotPresent(plugin);

                // filled logo path
                generateAccessibleLogoUrl(plugin);

                // update phase
                PluginWrapper pluginWrapper = getPluginWrapper(name);
                Plugin.PluginStatus status = plugin.statusNonNull();
                status.setPhase(pluginWrapper.getPluginState());
                updateStatus(plugin.getMetadata().getName(), status);
                return false;
            })
            .orElse(false);
    }

    void generateAccessibleLogoUrl(Plugin plugin) {
        String logo = plugin.getSpec().getLogo();
        if (StringUtils.isBlank(logo)) {
            return;
        }
        Plugin.PluginStatus status = plugin.statusNonNull();
        if (PathUtils.isAbsoluteUri(logo)) {
            status.setLogo(logo);
        } else {
            String assetsPrefix =
                PluginConst.assertsRoutePrefix(plugin.getMetadata().getName());
            String versionedLogo =
                applyVersioningToStaticResource(logo, plugin.getSpec().getVersion());
            status.setLogo(PathUtils.combinePath(assetsPrefix, versionedLogo));
        }
    }

    Optional<Setting> lookupPluginSetting(String name, String settingName) {
        Assert.notNull(name, "Plugin name must not be null");
        Assert.notNull(settingName, "Setting name must not be null");
        PluginWrapper pluginWrapper = getPluginWrapper(name);
        // If it already exists, do not look for setting
        if (RuntimeMode.DEPLOYMENT.equals(pluginWrapper.getRuntimeMode())) {
            Optional<Setting> existing = client.fetch(Setting.class, settingName);
            if (existing.isPresent()) {
                return existing;
            }
        }

        var resourceLoader =
            new DefaultResourceLoader(pluginWrapper.getPluginClassLoader());
        return PluginExtensionLoaderUtils.lookupExtensions(pluginWrapper.getPluginPath(),
                pluginWrapper.getRuntimeMode())
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

        Optional<Setting> settingOption = lookupPluginSetting(pluginName, settingName)
            .map(setting -> {
                // This annotation is added to prevent it from being deleted when stopped.
                Map<String, String> settingAnnotations = MetadataUtil.nullSafeAnnotations(setting);
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
            Plugin.PluginStatus status = plugin.statusNonNull();
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
            updateStatus(plugin.getMetadata().getName(), status);
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
                case CREATED -> ensurePluginLoaded();
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
                case CREATED -> ensurePluginLoaded();
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
        PluginWrapper pluginWrapper = getPluginWrapper(name);
        PluginState currentState = pluginWrapper.getPluginState();
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
                currentState = pluginWrapper.getPluginState();
            } catch (Throwable e) {
                persistenceFailureStatus(name, e);
                throw e;
            }
        }
    }

    void persistenceFailureStatus(String pluginName, Throwable e) {
        client.fetch(Plugin.class, pluginName).ifPresent(plugin -> {
            Plugin.PluginStatus status = plugin.statusNonNull();

            PluginWrapper pluginWrapper = getPluginWrapper(pluginName);
            status.setPhase(pluginWrapper.getPluginState());

            Plugin.PluginStatus oldStatus = JsonUtils.deepCopy(status);
            Condition condition = Condition.builder()
                .type(PluginState.FAILED.toString())
                .reason("UnexpectedState")
                .message(e.getMessage())
                .status(ConditionStatus.FALSE)
                .lastTransitionTime(Instant.now())
                .build();
            Plugin.PluginStatus.nullSafeConditions(status)
                .addAndEvictFIFO(condition);
            if (!Objects.equals(oldStatus, status)) {
                client.update(plugin);
            }
        });
    }

    @NonNull
    private PluginWrapper getPluginWrapper(String name) {
        PluginWrapper pluginWrapper = haloPluginManager.getPlugin(name);
        if (pluginWrapper == null) {
            ensurePluginLoaded();
            pluginWrapper = haloPluginManager.getPlugin(name);
        }

        if (pluginWrapper == null) {
            Plugin.PluginStatus status = new Plugin.PluginStatus();
            status.setPhase(PluginState.FAILED);

            String errorMsg = "Plugin " + name + " not found in plugin manager.";
            Condition condition = Condition.builder()
                .type(PluginState.FAILED.toString())
                .reason("PluginNotFound")
                .message(errorMsg)
                .status(ConditionStatus.FALSE)
                .lastTransitionTime(Instant.now())
                .build();
            Plugin.PluginStatus.nullSafeConditions(status)
                .addAndEvictFIFO(condition);
            updateStatus(name, status);
            throw new PluginNotFoundException(errorMsg);
        }
        return pluginWrapper;
    }

    void updateStatus(String name, Plugin.PluginStatus status) {
        if (status == null) {
            return;
        }
        client.fetch(Plugin.class, name).ifPresent(plugin -> {
            Plugin.PluginStatus oldStatus = JsonUtils.deepCopy(plugin.statusNonNull());
            plugin.setStatus(status);
            if (!Objects.equals(oldStatus, status)) {
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

        client.fetch(Plugin.class, name).ifPresent(plugin -> {
            final Plugin.PluginStatus status = plugin.statusNonNull();
            final Plugin.PluginStatus oldStatus = JsonUtils.deepCopy(status);

            PluginState currentState = haloPluginManager.startPlugin(name);
            if (!PluginState.STARTED.equals(currentState)) {
                PluginStartingError staringErrorInfo = getStaringErrorInfo(name);
                log.debug("Failed to start plugin: " + staringErrorInfo.getDevMessage());
                throw new IllegalStateException(staringErrorInfo.getMessage());
            }

            plugin.statusNonNull().setLastStartTime(Instant.now());

            final String pluginVersion = plugin.getSpec().getVersion();
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
            if (!Objects.equals(oldStatus, status)) {
                client.update(plugin);
            }
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
        client.fetch(Plugin.class, name).ifPresent(plugin -> {
            final Plugin.PluginStatus status = plugin.statusNonNull();
            final Plugin.PluginStatus oldStatus = JsonUtils.deepCopy(status);

            PluginState currentState = haloPluginManager.stopPlugin(name);
            if (!PluginState.STOPPED.equals(currentState)) {
                throw new IllegalStateException("Failed to stop plugin: " + name);
            }
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
            if (!Objects.equals(oldStatus, status)) {
                client.update(plugin);
            }
        });
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new Plugin())
            .build();
    }

    private void reconcilePluginState(String name) {
        if (haloPluginManager.getPlugin(name) == null) {
            ensurePluginLoaded();
        }

        client.fetch(Plugin.class, name).ifPresent(plugin -> {
            // Transition plugin status if necessary
            if (shouldReconcileStartState(plugin)) {
                startAction(name);
            }

            if (shouldReconcileStopState(plugin)) {
                stopAction(name);
            }
        });
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
        PluginWrapper pluginWrapper = getPluginWrapper(plugin.getMetadata().getName());
        return BooleanUtils.isTrue(plugin.getSpec().getEnabled())
            && !PluginState.STARTED.equals(pluginWrapper.getPluginState());
    }

    private boolean shouldReconcileStopState(Plugin plugin) {
        PluginWrapper pluginWrapper = getPluginWrapper(plugin.getMetadata().getName());
        return BooleanUtils.isFalse(plugin.getSpec().getEnabled())
            && PluginState.STARTED.equals(pluginWrapper.getPluginState());
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
        if (pluginWrapper == null) {
            return;
        }
        // pluginWrapper must not be null in below code
        // stop and unload plugin, see also PluginBeforeStopSyncListener
        if (!haloPluginManager.unloadPlugin(name)) {
            throw new IllegalStateException("Failed to unload plugin: " + name);
        }
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
