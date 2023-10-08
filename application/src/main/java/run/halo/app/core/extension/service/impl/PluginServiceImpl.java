package run.halo.app.core.extension.service.impl;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import com.github.zafarkhaja.semver.Version;
import com.google.common.hash.Hashing;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.pf4j.PluginWrapper;
import org.pf4j.RuntimeMode;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.core.extension.Plugin;
import run.halo.app.core.extension.service.PluginService;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.SystemVersionSupplier;
import run.halo.app.infra.exception.PluginAlreadyExistsException;
import run.halo.app.infra.exception.PluginInstallationException;
import run.halo.app.infra.exception.UnsatisfiedAttributeValueException;
import run.halo.app.infra.utils.FileUtils;
import run.halo.app.infra.utils.VersionUtils;
import run.halo.app.plugin.HaloPluginManager;
import run.halo.app.plugin.PluginConst;
import run.halo.app.plugin.PluginProperties;
import run.halo.app.plugin.PluginUtils;
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

    private final HaloPluginManager pluginManager;

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
            .flatMap(pluginInPath -> {
                // validate the plugin version
                satisfiesRequiresVersion(pluginInPath);

                return client.fetch(Plugin.class, pluginInPath.getMetadata().getName())
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
                    );
            });
    }

    @Override
    public Mono<Plugin> upgrade(String name, Path path) {
        return findPluginManifest(path)
            .flatMap(pluginInPath -> {
                // pre-check the plugin in the path
                Validate.notNull(pluginInPath.statusNonNull().getLoadLocation());
                satisfiesRequiresVersion(pluginInPath);
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
                    .flatMap(prevPlugin -> copyToPluginHome(pluginInPath))
                    .flatMap(pluginPath -> updateReloadAnno(name, pluginPath));
            });
    }

    @Override
    public Mono<Plugin> reload(String name) {
        PluginWrapper pluginWrapper = pluginManager.getPlugin(name);
        if (pluginWrapper == null) {
            return Mono.error(() -> new ServerWebInputException(
                "The given plugin with name " + name + " was not found."));
        }
        return updateReloadAnno(name, pluginWrapper.getPluginPath());
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
            if (RuntimeMode.DEVELOPMENT.equals(pluginManager.getRuntimeMode())) {
                return String.valueOf(System.currentTimeMillis());
            }
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

    private Mono<Plugin> updateReloadAnno(String name, Path pluginPath) {
        return client.get(Plugin.class, name)
            .flatMap(plugin -> {
                // add reload annotation to flag the plugin to be reloaded
                Map<String, String> annotations = MetadataUtil.nullSafeAnnotations(plugin);
                annotations.put(PluginConst.RELOAD_ANNO, pluginPath.toString());
                return client.update(plugin);
            });
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
            .subscribeOn(Schedulers.boundedElastic());
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
}
