package run.halo.app.core.extension.service.impl;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.pf4j.PluginState.STARTED;
import static run.halo.app.plugin.PluginConst.RELOAD_ANNO;

import com.github.zafarkhaja.semver.Version;
import com.google.common.hash.Hashing;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.DependencyResolver;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginWrapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;
import run.halo.app.core.extension.Plugin;
import run.halo.app.core.extension.service.PluginService;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemVersionSupplier;
import run.halo.app.infra.exception.PluginAlreadyExistsException;
import run.halo.app.infra.exception.PluginDependenciesNotEnabledException;
import run.halo.app.infra.exception.PluginDependencyException;
import run.halo.app.infra.exception.PluginDependentsNotDisabledException;
import run.halo.app.infra.exception.PluginInstallationException;
import run.halo.app.infra.exception.UnsatisfiedAttributeValueException;
import run.halo.app.infra.utils.FileUtils;
import run.halo.app.infra.utils.VersionUtils;
import run.halo.app.plugin.PluginConst;
import run.halo.app.plugin.PluginProperties;
import run.halo.app.plugin.PluginUtils;
import run.halo.app.plugin.SpringPluginManager;
import run.halo.app.plugin.YamlPluginDescriptorFinder;
import run.halo.app.plugin.YamlPluginFinder;
import run.halo.app.plugin.resources.BundleResourceUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class PluginServiceImpl implements PluginService {

    private static final String PRESET_LOCATION_PREFIX = "classpath:/presets/plugins/";
    private static final String PRESETS_LOCATION_PATTERN = PRESET_LOCATION_PREFIX + "*.jar";

    private final ReactiveExtensionClient client;

    private final SystemVersionSupplier systemVersion;

    private final PluginProperties pluginProperties;

    private final SpringPluginManager pluginManager;

    @Override
    public Flux<Plugin> getPresets() {
        // list presets from classpath
        return Flux.defer(() -> getPresetJars()
            .map(this::toPath)
            .map(path -> new YamlPluginFinder().find(path)));
    }

    @Override
    public Mono<Plugin> getPreset(String presetName) {
        return getPresets()
            .filter(plugin -> Objects.equals(plugin.getMetadata().getName(), presetName))
            .next();
    }

    @Override
    public Mono<Plugin> install(Path path) {
        return findPluginManifest(path)
            .doOnNext(plugin -> {
                // validate the plugin version
                satisfiesRequiresVersion(plugin);
                checkDependencies(plugin);
            })
            .flatMap(pluginInPath ->
                client.fetch(Plugin.class, pluginInPath.getMetadata().getName())
                    .flatMap(oldPlugin -> Mono.<Plugin>error(
                        new PluginAlreadyExistsException(oldPlugin.getMetadata().getName())))
                    .switchIfEmpty(Mono.defer(
                        () -> copyToPluginHome(pluginInPath)
                            .flatMap(this::findPluginManifest)
                            .doOnNext(p -> {
                                // Disable auto enable after installation
                                p.getSpec().setEnabled(false);
                            })
                            .flatMap(client::create))
                    ));
    }

    private void checkDependencies(Plugin plugin) {
        var resolvedPlugins = new ArrayList<>(pluginManager.getResolvedPlugins());
        var pluginDescriptors = new ArrayList<PluginDescriptor>(resolvedPlugins.size() + 1);

        resolvedPlugins.stream()
            .map(PluginWrapper::getDescriptor)
            .forEach(pluginDescriptors::add);

        var pluginDescriptor = YamlPluginDescriptorFinder.convert(plugin);
        pluginDescriptors.add(pluginDescriptor);

        var deptResolver = new DependencyResolver(pluginManager.getVersionManager());
        var result = deptResolver.resolve(pluginDescriptors);
        if (result.hasCyclicDependency()) {
            throw new PluginDependencyException.CyclicException();
        }
        var notFoundDependencies = result.getNotFoundDependencies();
        if (!CollectionUtils.isEmpty(notFoundDependencies)) {
            throw new PluginDependencyException.NotFoundException(notFoundDependencies);
        }

        var wrongVersionDependencies = result.getWrongVersionDependencies();
        if (!CollectionUtils.isEmpty(wrongVersionDependencies)) {
            throw new PluginDependencyException.WrongVersionsException(wrongVersionDependencies);
        }
    }

    @Override
    public Mono<Plugin> upgrade(String name, Path path) {
        return findPluginManifest(path)
            .doOnNext(plugin -> {
                satisfiesRequiresVersion(plugin);
                checkDependencies(plugin);
            })
            .flatMap(pluginInPath -> {
                // pre-check the plugin in the path
                Assert.notNull(pluginInPath.statusNonNull().getLoadLocation(),
                    "plugin.status.load-location must not be null");
                if (!Objects.equals(name, pluginInPath.getMetadata().getName())) {
                    return Mono.error(new ServerWebInputException(
                        "The provided plugin " + pluginInPath.getMetadata().getName()
                            + " didn't match the given plugin " + name));
                }

                // check if the plugin exists
                return client.fetch(Plugin.class, name)
                    .switchIfEmpty(Mono.error(() -> new ServerWebInputException(
                        "The given plugin with name " + name + " was not found.")))
                    // copy plugin into plugin home
                    .flatMap(oldPlugin -> copyToPluginHome(pluginInPath).thenReturn(oldPlugin))
                    .doOnNext(oldPlugin -> updatePlugin(oldPlugin, pluginInPath))
                    .flatMap(client::update);
            });
    }

    @Override
    public Mono<Plugin> reload(String name) {
        return client.get(Plugin.class, name)
            .flatMap(oldPlugin -> {
                if (oldPlugin.getStatus() == null
                    || oldPlugin.getStatus().getLoadLocation() == null) {
                    return Mono.error(new IllegalStateException(
                        "Load location of plugin has not been populated."));
                }
                var loadLocation = oldPlugin.getStatus().getLoadLocation();
                var loadPath = Path.of(loadLocation);
                return findPluginManifest(loadPath)
                    .doOnNext(newPlugin -> updatePlugin(oldPlugin, newPlugin))
                    .thenReturn(oldPlugin);
            })
            .flatMap(client::update);
    }

    @Override
    public Flux<DataBuffer> uglifyJsBundle() {
        var startedPlugins = List.copyOf(pluginManager.getStartedPlugins());
        String plugins = """
            this.enabledPluginNames = [%s];
            """.formatted(startedPlugins.stream()
            .map(PluginWrapper::getPluginId)
            .collect(Collectors.joining("','", "'", "'")));
        return Flux.fromIterable(startedPlugins)
            .mapNotNull(pluginWrapper -> {
                var pluginName = pluginWrapper.getPluginId();
                return BundleResourceUtils.getJsBundleResource(pluginManager, pluginName,
                    BundleResourceUtils.JS_BUNDLE);
            })
            .flatMap(resource -> {
                try {
                    // Specifying bufferSize as resource content length is
                    // to append line breaks at the end of each plugin
                    return DataBufferUtils.read(resource, DefaultDataBufferFactory.sharedInstance,
                            (int) resource.contentLength())
                        .doOnNext(dataBuffer -> {
                            // add a new line after each plugin bundle to avoid syntax error
                            dataBuffer.write("\n".getBytes(StandardCharsets.UTF_8));
                        });
                } catch (IOException e) {
                    log.error("Failed to read plugin bundle resource", e);
                    return Flux.empty();
                }
            })
            .concatWith(Flux.defer(() -> {
                var dataBuffer = DefaultDataBufferFactory.sharedInstance
                    .wrap(plugins.getBytes(StandardCharsets.UTF_8));
                return Flux.just(dataBuffer);
            }));
    }

    @Override
    public Flux<DataBuffer> uglifyCssBundle() {
        return Flux.fromIterable(pluginManager.getStartedPlugins())
            .mapNotNull(pluginWrapper -> {
                String pluginName = pluginWrapper.getPluginId();
                return BundleResourceUtils.getJsBundleResource(pluginManager, pluginName,
                    BundleResourceUtils.CSS_BUNDLE);
            })
            .flatMap(resource -> {
                try {
                    return DataBufferUtils.read(resource, DefaultDataBufferFactory.sharedInstance,
                        (int) resource.contentLength());
                } catch (IOException e) {
                    log.error("Failed to read plugin css bundle resource", e);
                    return Flux.empty();
                }
            });
    }

    @Override
    public Mono<String> generateJsBundleVersion() {
        return Mono.fromSupplier(() -> {
            var compactVersion = pluginManager.getStartedPlugins()
                .stream()
                .sorted(Comparator.comparing(PluginWrapper::getPluginId))
                .map(pluginWrapper -> pluginWrapper.getPluginId() + ":"
                    + pluginWrapper.getDescriptor().getVersion()
                )
                .collect(Collectors.joining());
            return Hashing.sha256().hashUnencodedChars(compactVersion).toString();
        });
    }

    @Override
    public Mono<Plugin> changeState(String pluginName, boolean requestToEnable, boolean wait) {
        var updatedPlugin = Mono.defer(() -> client.get(Plugin.class, pluginName))
            .flatMap(plugin -> {
                if (!Objects.equals(requestToEnable, plugin.getSpec().getEnabled())) {
                    // preflight check
                    if (requestToEnable) {
                        // make sure the dependencies are enabled
                        var dependencies = plugin.getSpec().getPluginDependencies().keySet();
                        var notStartedDependencies = dependencies.stream()
                            .filter(dependency -> {
                                var pluginWrapper = pluginManager.getPlugin(dependency);
                                return pluginWrapper == null
                                    || !Objects.equals(STARTED, pluginWrapper.getPluginState());
                            })
                            .toList();
                        if (!CollectionUtils.isEmpty(notStartedDependencies)) {
                            return Mono.error(
                                new PluginDependenciesNotEnabledException(notStartedDependencies)
                            );
                        }
                    } else {
                        // make sure the dependents are disabled
                        var dependents = pluginManager.getDependents(pluginName);
                        var notDisabledDependents = dependents.stream()
                            .filter(
                                dependent -> Objects.equals(STARTED, dependent.getPluginState())
                            )
                            .map(PluginWrapper::getPluginId)
                            .toList();
                        if (!CollectionUtils.isEmpty(notDisabledDependents)) {
                            return Mono.error(
                                new PluginDependentsNotDisabledException(notDisabledDependents)
                            );
                        }
                    }

                    plugin.getSpec().setEnabled(requestToEnable);
                    log.debug("Updating plugin {} state to {}", pluginName, requestToEnable);
                    return client.update(plugin);
                }
                log.debug("Checking plugin {} state, no need to update", pluginName);
                return Mono.just(plugin);
            });

        if (wait) {
            // if we want to wait the state of plugin to be updated
            updatedPlugin = updatedPlugin
                .flatMap(plugin -> {
                    var phase = plugin.statusNonNull().getPhase();
                    if (requestToEnable) {
                        // if we request to enable the plugin
                        if (!(Plugin.Phase.STARTED.equals(phase)
                            || Plugin.Phase.FAILED.equals(phase))) {
                            return Mono.error(UnexpectedPluginStateException::new);
                        }
                    } else {
                        // if we request to disable the plugin
                        if (Plugin.Phase.STARTED.equals(phase)) {
                            return Mono.error(UnexpectedPluginStateException::new);
                        }
                    }
                    return Mono.just(plugin);
                })
                .retryWhen(
                    Retry.backoff(10, Duration.ofMillis(100))
                        .filter(UnexpectedPluginStateException.class::isInstance)
                        .doBeforeRetry(signal ->
                            log.debug("Waiting for plugin {} to meet expected state", pluginName)
                        )
                )
                .doOnSuccess(plugin -> {
                    log.info("Plugin {} met expected state {}",
                        pluginName, plugin.statusNonNull().getPhase());
                });
        }

        return updatedPlugin;
    }

    Mono<Plugin> findPluginManifest(Path path) {
        return Mono.fromSupplier(
                () -> {
                    final var pluginFinder = new YamlPluginFinder();
                    return pluginFinder.find(path);
                })
            .onErrorMap(e -> new PluginInstallationException("Failed to parse the plugin manifest",
                "problemDetail.plugin.missingManifest", null)
            );
    }

    /**
     * Copy plugin into plugin home.
     *
     * @param plugin is a staging plugin.
     * @return new path in plugin home.
     */
    private Mono<Path> copyToPluginHome(Plugin plugin) {
        return Mono.fromCallable(
                () -> {
                    var fileName = PluginUtils.generateFileName(plugin);
                    var pluginRoot = Paths.get(pluginProperties.getPluginsRoot());
                    try {
                        Files.createDirectories(pluginRoot);
                    } catch (IOException e) {
                        throw Exceptions.propagate(e);
                    }
                    var pluginFilePath = pluginRoot.resolve(fileName);
                    FileUtils.checkDirectoryTraversal(pluginRoot, pluginFilePath);
                    // move the plugin jar file to the plugin root
                    // replace the old plugin jar file if exists
                    var path = Path.of(plugin.getStatus().getLoadLocation());
                    FileUtils.copy(path, pluginFilePath, REPLACE_EXISTING);
                    return pluginFilePath;
                })
            .subscribeOn(Schedulers.boundedElastic())
            .doOnNext(loadLocation -> {
                // reset load location and annotation PLUGIN_PATH
                plugin.getStatus().setLoadLocation(loadLocation.toUri());
                var annotations = plugin.getMetadata().getAnnotations();
                if (annotations == null) {
                    annotations = new HashMap<>();
                    plugin.getMetadata().setAnnotations(annotations);
                }
                annotations.put(PluginConst.PLUGIN_PATH, loadLocation.toString());
            });
    }

    private void satisfiesRequiresVersion(Plugin newPlugin) {
        Assert.notNull(newPlugin, "The plugin must not be null.");
        Version version = systemVersion.get();
        // validate the plugin version
        // only use the nominal system version to compare, the format is like MAJOR.MINOR.PATCH
        String systemVersion = version.getNormalVersion();
        String requires = newPlugin.getSpec().getRequires();
        if (!VersionUtils.satisfiesRequires(systemVersion, requires)) {
            throw new UnsatisfiedAttributeValueException(String.format(
                "Plugin requires a minimum system version of [%s], but the current version is "
                    + "[%s].",
                requires, systemVersion),
                "problemDetail.plugin.version.unsatisfied.requires",
                new String[] {requires, systemVersion});
        }
    }

    private Flux<Resource> getPresetJars() {
        var resolver = new PathMatchingResourcePatternResolver();
        try {
            var resources = resolver.getResources(PRESETS_LOCATION_PATTERN);
            return Flux.fromArray(resources);
        } catch (IOException e) {
            return Flux.error(e);
        }
    }

    private Path toPath(Resource resource) {
        try {
            return Path.of(resource.getURI());
        } catch (IOException e) {
            throw Exceptions.propagate(e);
        }
    }

    private static void updatePlugin(Plugin oldPlugin, Plugin newPlugin) {
        var oldMetadata = oldPlugin.getMetadata();
        var newMetadata = newPlugin.getMetadata();
        // merge labels
        if (!CollectionUtils.isEmpty(newMetadata.getLabels())) {
            var labels = oldMetadata.getLabels();
            if (labels == null) {
                labels = new HashMap<>();
                oldMetadata.setLabels(labels);
            }
            labels.putAll(newMetadata.getLabels());
        }

        var annotations = oldMetadata.getAnnotations();
        if (annotations == null) {
            annotations = new HashMap<>();
            oldMetadata.setAnnotations(annotations);
        }

        // merge annotations
        if (!CollectionUtils.isEmpty(newMetadata.getAnnotations())) {
            annotations.putAll(newMetadata.getAnnotations());
        }

        // request to reload
        annotations.put(RELOAD_ANNO,
            newPlugin.getStatus().getLoadLocation().toString());

        // apply spec and keep enabled request
        var enabled = oldPlugin.getSpec().getEnabled();
        oldPlugin.setSpec(newPlugin.getSpec());
        oldPlugin.getSpec().setEnabled(enabled);
    }

    private static class UnexpectedPluginStateException extends RuntimeException {

    }

}
