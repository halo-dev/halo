package run.halo.app.plugin;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static org.pf4j.PluginState.STARTED;
import static run.halo.app.plugin.PluginConst.RELOAD_ANNO;

import com.github.zafarkhaja.semver.Version;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.DependencyResolver;
import org.pf4j.PluginDependency;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginWrapper;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;
import run.halo.app.core.extension.Plugin;
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
import run.halo.app.plugin.resources.BundleResourceUtils;

@Slf4j
@Component
public class PluginServiceImpl implements PluginService, InitializingBean, DisposableBean {

    private static final String PRESET_LOCATION_PREFIX = "classpath:/presets/plugins/";
    private static final String PRESETS_LOCATION_PATTERN = PRESET_LOCATION_PREFIX + "*.jar";

    private final ReactiveExtensionClient client;

    private final SystemVersionSupplier systemVersion;

    private final PluginsRootGetter pluginsRootGetter;

    private final SpringPluginManager pluginManager;

    private final BundleCache jsBundleCache;

    private final BundleCache cssBundleCache;

    private Path tempDir;

    private final Scheduler scheduler = Schedulers.boundedElastic();

    private Clock clock = Clock.systemUTC();

    public PluginServiceImpl(ReactiveExtensionClient client,
        SystemVersionSupplier systemVersion,
        PluginsRootGetter pluginsRootGetter,
        SpringPluginManager pluginManager) {
        this.client = client;
        this.systemVersion = systemVersion;
        this.pluginsRootGetter = pluginsRootGetter;
        this.pluginManager = pluginManager;

        this.jsBundleCache = new BundleCache(".js");
        this.cssBundleCache = new BundleCache(".css");
    }

    /**
     * The method is only for testing.
     *
     * @param clock new clock
     */
    void setClock(Clock clock) {
        Assert.notNull(clock, "Clock must not be null");
        this.clock = clock;
    }

    @Override
    public Mono<Void> installPresetPlugins() {
        return getPresetJars()
            .flatMap(path -> this.install(path)
                .onErrorResume(PluginAlreadyExistsException.class, e -> Mono.empty())
                .flatMap(plugin -> FileUtils.deleteFileSilently(path)
                    .thenReturn(plugin)
                )
            )
            .flatMap(this::enablePlugin)
            .subscribeOn(Schedulers.boundedElastic())
            .then();
    }

    private Mono<Plugin> enablePlugin(Plugin plugin) {
        plugin.getSpec().setEnabled(true);
        return client.update(plugin)
            .onErrorResume(OptimisticLockingFailureException.class,
                e -> enablePlugin(plugin.getMetadata().getName())
            );
    }

    private Mono<Plugin> enablePlugin(String name) {
        return Mono.defer(() -> client.get(Plugin.class, name)
                .flatMap(plugin -> {
                    plugin.getSpec().setEnabled(true);
                    return client.update(plugin);
                })
            )
            .retryWhen(Retry.backoff(8, Duration.ofMillis(100))
                .filter(OptimisticLockingFailureException.class::isInstance));
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
        var dataBufferFactory = DefaultDataBufferFactory.sharedInstance;
        var end = Mono.fromSupplier(
            () -> {
                var sb = new StringBuilder("this.enabledPlugins = [");
                var iterator = startedPlugins.iterator();
                while (iterator.hasNext()) {
                    var plugin = iterator.next();
                    sb.append("""
                        {"name":"%s","version":"%s"}\
                        """
                        .formatted(
                            plugin.getPluginId(),
                            plugin.getDescriptor().getVersion()
                        )
                    );
                    if (iterator.hasNext()) {
                        sb.append(',');
                    }
                }
                sb.append(']');
                return dataBufferFactory.wrap(sb.toString().getBytes(StandardCharsets.UTF_8));
            });
        var body = Flux.fromIterable(startedPlugins)
            .sort(Comparator.comparing(PluginWrapper::getPluginId))
            .flatMapSequential(pluginWrapper -> {
                var pluginId = pluginWrapper.getPluginId();
                return getBundleResource(pluginId, BundleResourceUtils.JS_BUNDLE)
                    .flatMapMany(resource -> {
                        var head = Mono.<DataBuffer>fromSupplier(
                            () -> dataBufferFactory.wrap(
                                ("// Generated from plugin " + pluginId + "\n").getBytes()
                            ));
                        var content = DataBufferUtils.read(
                            resource, dataBufferFactory, StreamUtils.BUFFER_SIZE
                        );
                        var tail = Mono.fromSupplier(() -> dataBufferFactory.wrap("\n".getBytes()));
                        return Flux.concat(head, content, tail);
                    });
            });
        return Flux.concat(body, end);
    }

    @Override
    public Flux<DataBuffer> uglifyCssBundle() {
        return Flux.fromIterable(pluginManager.getStartedPlugins())
            .sort(Comparator.comparing(PluginWrapper::getPluginId))
            .flatMapSequential(pluginWrapper -> {
                var pluginId = pluginWrapper.getPluginId();
                var dataBufferFactory = DefaultDataBufferFactory.sharedInstance;
                return getBundleResource(pluginId, BundleResourceUtils.CSS_BUNDLE)
                    .flatMapMany(resource -> {
                        var head = Mono.<DataBuffer>fromSupplier(() -> dataBufferFactory.wrap(
                            ("/* Generated from plugin " + pluginId + " */\n").getBytes()
                        ));
                        var content = DataBufferUtils.read(
                            resource, dataBufferFactory, StreamUtils.BUFFER_SIZE);
                        var tail = Mono.fromSupplier(() -> dataBufferFactory.wrap("\n".getBytes()));
                        return Flux.concat(head, content, tail);
                    });
            });
    }

    private Mono<Resource> getBundleResource(String pluginName, String bundleName) {
        return Mono.fromSupplier(
                () -> BundleResourceUtils.getJsBundleResource(pluginManager, pluginName, bundleName)
            )
            .filter(Resource::isReadable);
    }

    @Override
    public Mono<String> generateBundleVersion() {
        if (pluginManager.isDevelopment()) {
            return Mono.just(String.valueOf(clock.instant().toEpochMilli()));
        }
        return Flux.fromIterable(new ArrayList<>(pluginManager.getStartedPlugins()))
            .sort(Comparator.comparing(PluginWrapper::getPluginId))
            .map(pw -> pw.getPluginId() + ':' + pw.getDescriptor().getVersion())
            .collect(Collectors.joining())
            .map(Hashing.sha256()::hashUnencodedChars)
            .map(HashCode::toString);
    }

    @Override
    public Mono<Resource> getJsBundle(String version) {
        return jsBundleCache.computeIfAbsent(version, this.uglifyJsBundle());
    }

    @Override
    public Mono<Resource> getCssBundle(String version) {
        return cssBundleCache.computeIfAbsent(version, this.uglifyCssBundle());
    }

    @Override
    public Mono<Plugin> changeState(String pluginName, boolean requestToEnable, boolean wait) {
        var updatedPlugin = Mono.defer(() -> client.get(Plugin.class, pluginName))
            .flatMap(plugin -> {
                if (!Objects.equals(requestToEnable, plugin.getSpec().getEnabled())) {
                    // preflight check
                    if (requestToEnable) {
                        // make sure the dependencies are enabled
                        var notStartedDependencies = getRequiredDependencies(plugin,
                            pw -> pw == null || !Objects.equals(STARTED, pw.getPluginState())
                        );
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

    @Override
    public List<String> getRequiredDependencies(Plugin plugin,
        Predicate<PluginWrapper> predicate) {
        return getPluginDependency(plugin)
            .stream()
            .filter(dependency -> !dependency.isOptional())
            .map(PluginDependency::getPluginId)
            .filter(dependencyPlugin -> {
                var pluginWrapper = pluginManager.getPlugin(dependencyPlugin);
                return predicate.test(pluginWrapper);
            })
            .sorted()
            .toList();
    }

    private static List<PluginDependency> getPluginDependency(Plugin plugin) {
        return plugin.getSpec().getPluginDependencies().keySet()
            .stream()
            .map(PluginDependency::new)
            .toList();
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


    @Override
    public void afterPropertiesSet() throws Exception {
        this.tempDir = Files.createTempDirectory("halo-plugin-bundle");
    }

    @Override
    public void destroy() throws Exception {
        FileSystemUtils.deleteRecursively(this.tempDir);
    }

    /**
     * Set temporary directory for plugin bundle.
     *
     * @param tempDir temporary directory.
     */
    void setTempDir(Path tempDir) {
        this.tempDir = tempDir;
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
                    var pluginRoot = pluginsRootGetter.get();
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
        String systemVersion = version.toStableVersion().toString();
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

    private Flux<Path> getPresetJars() {
        var resolver = new PathMatchingResourcePatternResolver();
        try {
            var resources = resolver.getResources(PRESETS_LOCATION_PATTERN);
            return Flux.fromArray(resources)
                .mapNotNull(resource -> {
                    var filename = resource.getFilename();
                    if (StringUtils.isBlank(filename)) {
                        return null;
                    }
                    var path = tempDir.resolve(filename);
                    FileUtils.copyResource(resource, path);
                    return path;
                });
        } catch (IOException e) {
            log.debug("Failed to load preset plugins: {}", e.getMessage());
            return Flux.empty();
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

    class BundleCache {

        private final String suffix;

        private final AtomicBoolean writing = new AtomicBoolean();

        private volatile Resource resource;

        BundleCache(String suffix) {
            this.suffix = suffix;
        }

        Mono<Resource> computeIfAbsent(String version, Publisher<DataBuffer> content) {
            var filename = buildBundleFilename(version, suffix);
            if (isResourceMatch(resource, filename)) {
                return Mono.just(resource);
            }
            return generateBundleVersion()
                .flatMap(newVersion -> {
                    var newFilename = buildBundleFilename(newVersion, suffix);
                    if (isResourceMatch(this.resource, newFilename)) {
                        // if the resource was not changed, just return it
                        return Mono.just(resource);
                    }
                    if (writing.compareAndSet(false, true)) {
                        return Mono.justOrEmpty(this.resource)
                            // double check of the resource
                            .filter(res -> isResourceMatch(res, newFilename))
                            .switchIfEmpty(Mono.using(
                                    () -> {
                                        if (!Files.exists(tempDir)) {
                                            Files.createDirectories(tempDir);
                                        }
                                        return tempDir.resolve(newFilename);
                                    },
                                    path -> DataBufferUtils.write(content, path,
                                            CREATE, TRUNCATE_EXISTING)
                                        .then(Mono.<Resource>fromSupplier(
                                            () -> new FileSystemResource(path)
                                        )),
                                    path -> {
                                        if (shouldCleanUp(path)) {
                                            // clean up old resource
                                            cleanUp(this.resource);
                                        }
                                    })
                                .subscribeOn(scheduler)
                                .doOnNext(newResource -> this.resource = newResource)
                            )
                            .doFinally(signalType -> writing.set(false));
                    } else {
                        return Mono.defer(() -> {
                            if (this.writing.get()) {
                                log.debug("Waiting for the bundle file {} to be written", filename);
                                return Mono.empty();
                            }
                            log.debug("Waited the bundle file {} to be written", filename);
                            return Mono.just(this.resource);
                        }).repeatWhenEmpty(100, count -> {
                            // retry after 100ms
                            return count.delayElements(Duration.ofMillis(100));
                        });
                    }
                });
        }

        private boolean shouldCleanUp(Path newPath) {
            if (this.resource == null || !this.resource.exists()) {
                return false;
            }
            try {
                var oldPath = this.resource.getFile().toPath();
                return !oldPath.equals(newPath);
            } catch (IOException e) {
                return false;
            }
        }

        private static void cleanUp(Resource resource) {
            if (resource instanceof WritableResource wr
                && wr.isWritable()
                && wr.isFile()) {
                try {
                    Files.deleteIfExists(wr.getFile().toPath());
                } catch (IOException e) {
                    log.warn("Failed to delete old bundle file {}",
                        wr.getFilename(), e);
                }
            }
        }

        private static boolean isResourceMatch(Resource resource, String filename) {
            return resource != null
                && resource.exists()
                && resource.isFile()
                && Objects.equals(filename, resource.getFilename());
        }
    }

    private static String buildBundleFilename(String v, String suffix) {
        Assert.notNull(v, "Version must not be null");
        Assert.notNull(suffix, "Suffix must not be null");
        return v + suffix;
    }

    private static class UnexpectedPluginStateException extends RuntimeException {
    }

}
