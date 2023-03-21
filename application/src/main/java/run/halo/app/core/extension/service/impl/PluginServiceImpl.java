package run.halo.app.core.extension.service.impl;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import com.github.zafarkhaja.semver.Version;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.retry.RetryException;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerErrorException;
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
import run.halo.app.infra.exception.UnsatisfiedAttributeValueException;
import run.halo.app.infra.utils.FileUtils;
import run.halo.app.infra.utils.VersionUtils;
import run.halo.app.plugin.PluginProperties;
import run.halo.app.plugin.YamlPluginFinder;

@Slf4j
@Component
public class PluginServiceImpl implements PluginService {

    private static final String PRESET_LOCATION_PREFIX = "classpath:/presets/plugins/";
    private static final String PRESETS_LOCATION_PATTERN = PRESET_LOCATION_PREFIX + "*.jar";

    private final ReactiveExtensionClient client;

    private final SystemVersionSupplier systemVersion;

    private final PluginProperties pluginProperties;

    public PluginServiceImpl(ReactiveExtensionClient client,
        SystemVersionSupplier systemVersion, PluginProperties pluginProperties) {
        this.client = client;
        this.systemVersion = systemVersion;
        this.pluginProperties = pluginProperties;
    }

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
        return Mono.defer(() -> {
            final var pluginFinder = new YamlPluginFinder();
            final var pluginInPath = pluginFinder.find(path);
            // validate the plugin version
            satisfiesRequiresVersion(pluginInPath);

            return client.fetch(Plugin.class, pluginInPath.getMetadata().getName())
                .flatMap(oldPlugin -> Mono.<Plugin>error(
                    new PluginAlreadyExistsException(oldPlugin.getMetadata().getName())))
                .switchIfEmpty(Mono.defer(
                    () -> copyToPluginHome(pluginInPath)
                        .map(pluginFinder::find)
                        .doOnNext(p -> {
                            // Disable auto enable after installation
                            p.getSpec().setEnabled(false);
                        })
                        .flatMap(client::create)));

        });
    }

    @Override
    public Mono<Plugin> upgrade(String name, Path path) {
        return Mono.defer(() -> {
            // pre-check the plugin in the path
            final var pluginFinder = new YamlPluginFinder();
            final var pluginInPath = pluginFinder.find(path);
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
                // delete the plugin and wait for the deletion
                .then(Mono.defer(() -> deletePluginAndWaitForComplete(name)))
                // copy plugin into plugin home
                .flatMap(prevPlugin -> copyToPluginHome(pluginInPath)
                    .map(pluginFinder::find)
                    // reset enabled spec
                    .doOnNext(pluginToCreate -> {
                        var enabled = prevPlugin.getSpec().getEnabled();
                        pluginToCreate.getSpec().setEnabled(enabled);
                    }))
                // create the plugin
                .flatMap(client::create);
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
                    var fileName = plugin.generateFileName();
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

    private Mono<Plugin> deletePluginAndWaitForComplete(String pluginName) {
        return client.fetch(Plugin.class, pluginName)
            .flatMap(client::delete)
            .flatMap(plugin -> waitForDeleted(pluginName).thenReturn(plugin));
    }

    private Mono<Void> waitForDeleted(String pluginName) {
        return Mono.defer(() -> client.fetch(Plugin.class, pluginName)
                .flatMap(plugin -> Mono.error(
                    new RetryException("Re-check if the plugin is deleted successfully"))))
            .retryWhen(Retry.fixedDelay(20, Duration.ofMillis(100))
                .filter(t -> t instanceof RetryException)
            )
            .onErrorMap(Exceptions::isRetryExhausted,
                t -> new ServerErrorException("Wait timeout for plugin deleted", t))
            .then();
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
