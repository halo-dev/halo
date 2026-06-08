package run.halo.app.plugin;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StreamUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import run.halo.app.core.extension.Theme;
import run.halo.app.infra.ThemeRootGetter;
import run.halo.app.plugin.resources.BundleResourceUtils;
import run.halo.app.theme.ThemeUiResources;
import run.halo.app.theme.service.ThemeService;

@Slf4j
@Component
public class UiPluginBundleServiceImpl implements UiPluginBundleService, InitializingBean, DisposableBean {

    private final SpringPluginManager pluginManager;

    private final ThemeService themeService;

    private final ThemeRootGetter themeRoot;

    private final BundleCache jsBundleCache;

    private final BundleCache cssBundleCache;

    private final Scheduler scheduler = Schedulers.boundedElastic();

    private Path tempDir;

    private Clock clock = Clock.systemUTC();

    public UiPluginBundleServiceImpl(
            SpringPluginManager pluginManager, ThemeService themeService, ThemeRootGetter themeRoot) {
        this.pluginManager = pluginManager;
        this.themeService = themeService;
        this.themeRoot = themeRoot;
        this.jsBundleCache = new BundleCache(".js");
        this.cssBundleCache = new BundleCache(".css");
    }

    void setClock(Clock clock) {
        Assert.notNull(clock, "Clock must not be null");
        this.clock = clock;
    }

    @Override
    public Flux<DataBuffer> uglifyJsBundle() {
        var dataBufferFactory = DefaultDataBufferFactory.sharedInstance;
        var startedPlugins = pluginManager.startedPlugins();
        var pluginBundles = Flux.fromIterable(startedPlugins)
                .sort(Comparator.comparing(PluginWrapper::getPluginId))
                .flatMapSequential(plugin -> readPluginBundle(plugin, BundleResourceUtils.JS_BUNDLE, true));
        var activatedThemeBundle =
                fetchActivatedThemeBundle(ThemeUiResources.JS_BUNDLE).cache();
        var themeBundle = activatedThemeBundle.flatMapMany(bundle -> readThemeBundle(bundle, true));
        var enabledUiPlugins = activatedThemeBundle
                .map(ThemeBundle::theme)
                .map(theme -> enabledUiPluginsScript(startedPlugins, theme))
                .defaultIfEmpty(enabledUiPluginsScript(startedPlugins, null))
                .map(script -> dataBufferFactory.wrap(script.getBytes(UTF_8)));
        return Flux.concat(pluginBundles, themeBundle, enabledUiPlugins);
    }

    @Override
    public Flux<DataBuffer> uglifyCssBundle() {
        var pluginBundles = Flux.fromIterable(pluginManager.startedPlugins())
                .sort(Comparator.comparing(PluginWrapper::getPluginId))
                .flatMapSequential(plugin -> readPluginBundle(plugin, BundleResourceUtils.CSS_BUNDLE, false));
        var themeBundle = fetchActivatedThemeBundle(ThemeUiResources.CSS_BUNDLE)
                .flatMapMany(bundle -> readThemeBundle(bundle, false));
        return Flux.concat(pluginBundles, themeBundle);
    }

    @Override
    public Mono<String> generateBundleVersion() {
        if (pluginManager.isDevelopment()) {
            return Mono.just(String.valueOf(clock.instant().toEpochMilli()));
        }
        var pluginVersion = Flux.fromIterable(pluginManager.startedPlugins())
                .sort(Comparator.comparing(PluginWrapper::getPluginId))
                .map(pw -> pw.getPluginId() + ':' + pw.getDescriptor().getVersion())
                .collect(Collectors.joining());
        var themeVersion = themeService
                .fetchActivatedTheme()
                .map(theme -> "theme:" + theme.getMetadata().getName() + ':'
                        + Objects.toString(theme.getSpec().getVersion(), ""))
                .defaultIfEmpty("");
        return Mono.zip(pluginVersion, themeVersion)
                .map(tuple -> tuple.getT1() + tuple.getT2())
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
    public void afterPropertiesSet() throws Exception {
        this.tempDir = Files.createTempDirectory("halo-ui-plugin-bundle");
    }

    @Override
    public void destroy() throws Exception {
        if (this.tempDir != null) {
            FileSystemUtils.deleteRecursively(this.tempDir);
        }
    }

    void setTempDir(Path tempDir) {
        this.tempDir = tempDir;
    }

    private Flux<DataBuffer> readPluginBundle(PluginWrapper plugin, String bundleName, boolean js) {
        var pluginId = plugin.getPluginId();
        var dataBufferFactory = DefaultDataBufferFactory.sharedInstance;
        return getPluginBundleResource(pluginId, bundleName).flatMapMany(resource -> {
            var head = Mono.<DataBuffer>fromSupplier(
                    () -> dataBufferFactory.wrap(((js ? "// Generated from plugin " : "/* Generated from plugin ")
                                    + pluginId
                                    + (js ? "\n" : " */\n"))
                            .getBytes(UTF_8)));
            var content = DataBufferUtils.read(resource, dataBufferFactory, StreamUtils.BUFFER_SIZE);
            var tail = Mono.fromSupplier(() -> dataBufferFactory.wrap("\n".getBytes(UTF_8)));
            return Flux.concat(head, content, tail);
        });
    }

    private Flux<DataBuffer> readThemeBundle(ThemeBundle bundle, boolean js) {
        var themeName = bundle.theme().getMetadata().getName();
        var dataBufferFactory = DefaultDataBufferFactory.sharedInstance;
        var head = Mono.<DataBuffer>fromSupplier(() -> dataBufferFactory.wrap(
                ((js ? "// Generated from theme " : "/* Generated from theme ") + themeName + (js ? "\n" : " */\n"))
                        .getBytes(UTF_8)));
        var content = DataBufferUtils.read(bundle.resource(), dataBufferFactory, StreamUtils.BUFFER_SIZE);
        var tail = Mono.fromSupplier(() -> dataBufferFactory.wrap("\n".getBytes(UTF_8)));
        return Flux.concat(head, content, tail);
    }

    private Mono<Resource> getPluginBundleResource(String pluginName, String bundleName) {
        return Mono.fromSupplier(
                        () -> BundleResourceUtils.getSelectedBundleResource(pluginManager, pluginName, bundleName))
                .filter(Resource::isReadable);
    }

    private Mono<ThemeBundle> fetchActivatedThemeBundle(String bundleName) {
        return themeService
                .fetchActivatedTheme()
                .flatMap(theme -> Mono.fromSupplier(() -> ThemeUiResources.getBundleResource(
                                themeRoot.get(), theme.getMetadata().getName(), bundleName))
                        .map(resource -> new ThemeBundle(theme, resource)));
    }

    private static String enabledUiPluginsScript(Iterable<PluginWrapper> plugins, Theme theme) {
        var sb = new StringBuilder("this.enabledUiPlugins = [");
        var first = true;
        for (var plugin : plugins) {
            if (!first) {
                sb.append(',');
            }
            sb.append("""
                {"name":"%s","type":"plugin","version":"%s"}\
                """.formatted(plugin.getPluginId(), plugin.getDescriptor().getVersion()));
            first = false;
        }
        if (theme != null) {
            if (!first) {
                sb.append(',');
            }
            var themeName = theme.getMetadata().getName();
            sb.append("""
                {"name":"%s","type":"theme","themeName":"%s","version":"%s"}\
                """.formatted(
                            ThemeUiResources.buildModuleName(themeName),
                            themeName,
                            Objects.toString(theme.getSpec().getVersion(), "")));
        }
        sb.append(']');
        return sb.toString();
    }

    private record ThemeBundle(Theme theme, Resource resource) {}

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
            return generateBundleVersion().flatMap(newVersion -> {
                var newFilename = buildBundleFilename(newVersion, suffix);
                if (isResourceMatch(this.resource, newFilename)) {
                    return Mono.just(resource);
                }
                if (writing.compareAndSet(false, true)) {
                    return Mono.justOrEmpty(this.resource)
                            .filter(res -> isResourceMatch(res, newFilename))
                            .switchIfEmpty(Mono.using(
                                            () -> {
                                                if (!Files.exists(tempDir)) {
                                                    Files.createDirectories(tempDir);
                                                }
                                                return tempDir.resolve(newFilename);
                                            },
                                            path -> DataBufferUtils.write(content, path, CREATE, TRUNCATE_EXISTING)
                                                    .then(Mono.<Resource>fromSupplier(
                                                            () -> new FileSystemResource(path))),
                                            path -> {
                                                if (shouldCleanUp(path)) {
                                                    cleanUp(this.resource);
                                                }
                                            })
                                    .subscribeOn(scheduler)
                                    .doOnNext(newResource -> this.resource = newResource))
                            .doFinally(signalType -> writing.set(false));
                }
                return Mono.defer(() -> {
                            if (this.writing.get()) {
                                log.debug("Waiting for the UI plugin bundle file {} to be written", filename);
                                return Mono.empty();
                            }
                            log.debug("Waited the UI plugin bundle file {} to be written", filename);
                            return Mono.just(this.resource);
                        })
                        .repeatWhenEmpty(100, count -> count.delayElements(Duration.ofMillis(100)));
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
            if (resource instanceof WritableResource wr && wr.isWritable() && wr.isFile()) {
                try {
                    Files.deleteIfExists(wr.getFile().toPath());
                } catch (IOException e) {
                    log.warn("Failed to delete old UI plugin bundle file {}", wr.getFilename(), e);
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
}
