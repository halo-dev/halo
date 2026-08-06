package run.halo.app.plugin;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.hash.Hashing;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import run.halo.app.core.extension.Theme;
import run.halo.app.infra.ThemeRootGetter;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.plugin.resources.BundleResourceUtils;
import run.halo.app.theme.ThemeUiResources;
import run.halo.app.theme.service.ThemeService;

@Slf4j
@Component
public class UiPluginBundleServiceImpl implements UiPluginBundleService, InitializingBean, DisposableBean {

    static final String PROVIDER_MANIFEST = "ui-plugin.json";
    private static final String PLUGIN_TYPE = "plugin";
    private static final String THEME_TYPE = "theme";
    private static final String BUNDLE_BASE_URL = "/apis/api.console.halo.run/v1alpha1/ui-plugins/-";

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
        return discoverProviders().flatMapMany(providers -> {
            var bundles = Flux.fromIterable(providers)
                    .filter(provider -> provider.kind() == ProviderKind.LEGACY)
                    .flatMapSequential(provider -> readProviderBundle(provider, BundleResourceUtils.JS_BUNDLE, true));
            var metadata = Mono.fromSupplier(() -> enabledUiPluginsScript(providers))
                    .map(script -> DefaultDataBufferFactory.sharedInstance.wrap(script.getBytes(UTF_8)));
            return Flux.concat(bundles, metadata);
        });
    }

    @Override
    public Flux<DataBuffer> uglifyCssBundle() {
        return discoverProviders()
                .flatMapMany(providers -> Flux.fromIterable(providers).flatMapSequential(provider -> {
                    if (provider.kind() == ProviderKind.LEGACY) {
                        return readProviderBundle(provider, BundleResourceUtils.CSS_BUNDLE, false);
                    }
                    if (provider.kind() == ProviderKind.ESM
                            && provider.manifest().style() != null) {
                        return readProviderBundle(provider, provider.manifest().style(), false);
                    }
                    return Flux.empty();
                }));
    }

    @Override
    public Mono<String> generateBundleVersion() {
        return discoverProviders().map(this::providerVersion);
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
    public Mono<UiPluginProviderDescriptor> getProviderDescriptor() {
        return discoverProviders().map(providers -> createDescriptor(providerVersion(providers), providers));
    }

    private Mono<List<ClassifiedProvider>> discoverProviders() {
        return Mono.defer(() -> {
                    var plugins = pluginManager.startedPlugins().stream()
                            .sorted(Comparator.comparing(PluginWrapper::getPluginId))
                            .map(this::pluginCandidate)
                            .toList();
                    return themeService
                            .fetchActivatedTheme()
                            .map(Optional::of)
                            .defaultIfEmpty(Optional.empty())
                            .map(theme -> {
                                var candidates = new ArrayList<>(plugins);
                                theme.map(this::themeCandidate).ifPresent(candidates::add);
                                return candidates.stream().map(this::classify).toList();
                            });
                })
                .subscribeOn(scheduler);
    }

    private ProviderCandidate pluginCandidate(PluginWrapper plugin) {
        var pluginName = plugin.getPluginId();
        return new ProviderCandidate(
                pluginName,
                pluginName,
                PLUGIN_TYPE,
                Objects.toString(plugin.getDescriptor().getVersion(), ""),
                selectPluginBundleLocation(pluginName),
                pluginManager.isDevelopment());
    }

    private ProviderCandidate themeCandidate(Theme theme) {
        var themeName = theme.getMetadata().getName();
        return new ProviderCandidate(
                ThemeUiResources.buildModuleName(themeName),
                themeName,
                THEME_TYPE,
                Objects.toString(theme.getSpec().getVersion(), ""),
                null,
                isThemeInDevelopment(theme));
    }

    private String selectPluginBundleLocation(String pluginName) {
        for (var location :
                List.of(BundleResourceUtils.UI_BUNDLE_LOCATION, BundleResourceUtils.CONSOLE_BUNDLE_LOCATION)) {
            if (providerResource(pluginName, location, PROVIDER_MANIFEST) != null
                    || providerResource(pluginName, location, BundleResourceUtils.JS_BUNDLE) != null
                    || providerResource(pluginName, location, BundleResourceUtils.CSS_BUNDLE) != null) {
                return location;
            }
        }
        return null;
    }

    private Resource providerResource(String pluginName, String bundleLocation, String resourcePath) {
        return BundleResourceUtils.getBundleResource(pluginManager, pluginName, bundleLocation, resourcePath);
    }

    private Resource providerResource(ProviderCandidate candidate, String resourcePath) {
        if (PLUGIN_TYPE.equals(candidate.type())) {
            if (candidate.bundleLocation() == null) {
                return null;
            }
            return providerResource(candidate.resourceName(), candidate.bundleLocation(), resourcePath);
        }
        return ThemeUiResources.getResource(themeRoot.get(), candidate.resourceName(), resourcePath);
    }

    private ClassifiedProvider classify(ProviderCandidate candidate) {
        var manifestResource = providerResource(candidate, PROVIDER_MANIFEST);
        if (manifestResource == null) {
            return ClassifiedProvider.legacy(candidate);
        }
        try {
            var manifest = readManifest(candidate, manifestResource);
            return ClassifiedProvider.esm(candidate, manifest);
        } catch (Exception e) {
            return ClassifiedProvider.invalid(
                    candidate, Objects.toString(e.getMessage(), e.getClass().getSimpleName()));
        }
    }

    private ProviderManifest readManifest(ProviderCandidate candidate, Resource resource) throws IOException {
        JsonNode manifest;
        try (var inputStream = resource.getInputStream()) {
            manifest = JsonUtils.mapper().readTree(inputStream);
        }
        if (!manifest.isObject()) {
            throw new IllegalArgumentException("Provider manifest must be an object");
        }
        var fields = new HashSet<String>();
        manifest.fieldNames().forEachRemaining(fields::add);
        if (!fields.containsAll(Set.of("format", "entry"))
                || !Set.of("format", "entry", "style").containsAll(fields)) {
            throw new IllegalArgumentException("Provider manifest must contain format, entry, and optional style only");
        }
        if (!"esm".equals(manifest.path("format").textValue())) {
            throw new IllegalArgumentException("Provider manifest format must be esm");
        }
        var entry = validateManifestResource(candidate, manifest.path("entry"));
        var style = manifest.has("style") ? validateManifestResource(candidate, manifest.path("style")) : null;
        return new ProviderManifest(entry, style);
    }

    private String validateManifestResource(ProviderCandidate candidate, JsonNode value) {
        if (!value.isTextual()) {
            throw new IllegalArgumentException("Provider resource path must be a string");
        }
        var resourcePath = normalizeResourcePath(value.textValue());
        var resource = providerResource(candidate, resourcePath);
        if (resource == null || !resource.isReadable()) {
            throw new IllegalArgumentException(
                    "Provider resource does not exist inside its root: " + value.textValue());
        }
        return resourcePath;
    }

    private static String normalizeResourcePath(String resourcePath) {
        if (!StringUtils.hasText(resourcePath)) {
            throw new IllegalArgumentException("Provider resource path must not be blank");
        }
        var normalizedSlashes = resourcePath.replace('\\', '/');
        if (normalizedSlashes.startsWith("/")
                || normalizedSlashes.startsWith("//")
                || normalizedSlashes.contains("?")
                || normalizedSlashes.contains("#")
                || normalizedSlashes.matches("^[a-zA-Z][a-zA-Z0-9+.-]*:.*")) {
            throw new IllegalArgumentException(
                    "Provider resource path must be provider-root-relative: " + resourcePath);
        }
        var normalized = Path.of(normalizedSlashes).normalize();
        if (normalized.isAbsolute()
                || normalized.startsWith("..")
                || normalized.toString().equals(".")) {
            throw new IllegalArgumentException("Provider resource path escapes its root: " + resourcePath);
        }
        return normalized.toString().replace('\\', '/');
    }

    private UiPluginProviderDescriptor createDescriptor(String version, List<ClassifiedProvider> providers) {
        var registrations = providers.stream()
                .map(provider -> new UiPluginProviderDescriptor.Registration(
                        provider.candidate().name(),
                        provider.candidate().type(),
                        provider.candidate().version()))
                .toList();
        var esmProviders = providers.stream()
                .filter(provider -> provider.kind() == ProviderKind.ESM)
                .map(provider -> new UiPluginProviderDescriptor.EsmProvider(
                        provider.candidate().name(),
                        provider.candidate().type(),
                        provider.candidate().version(),
                        providerUrl(version, provider, provider.manifest().entry())))
                .toList();
        var invalid = providers.stream()
                .filter(provider -> provider.kind() == ProviderKind.INVALID)
                .map(provider -> new UiPluginProviderDescriptor.InvalidProvider(
                        provider.candidate().name(),
                        provider.candidate().type(),
                        provider.candidate().version(),
                        provider.error()))
                .toList();
        return new UiPluginProviderDescriptor(
                version,
                hasProviderStyles(providers) ? versionedBundleUrl("bundle.css", version) : null,
                new UiPluginProviderDescriptor.LegacyResources(versionedBundleUrl("bundle.js", version)),
                registrations,
                esmProviders,
                invalid);
    }

    private boolean hasProviderStyles(List<ClassifiedProvider> providers) {
        return providers.stream().anyMatch(provider -> {
            if (provider.kind() == ProviderKind.ESM) {
                return provider.manifest().style() != null;
            }
            return provider.kind() == ProviderKind.LEGACY
                    && Optional.ofNullable(providerResource(provider.candidate(), BundleResourceUtils.CSS_BUNDLE))
                            .filter(Resource::isReadable)
                            .isPresent();
        });
    }

    private static String providerUrl(String version, ClassifiedProvider provider, String resourcePath) {
        var candidate = provider.candidate();
        if (PLUGIN_TYPE.equals(candidate.type())) {
            return BundleResourceUtils.buildAssetUrl(
                    candidate.resourceName(), candidate.bundleLocation(), resourcePath, version);
        }
        return ThemeUiResources.buildAssetUrl(candidate.resourceName(), resourcePath, version);
    }

    private static String versionedBundleUrl(String filename, String version) {
        return UriComponentsBuilder.fromPath(BUNDLE_BASE_URL + '/' + filename)
                .queryParam("v", version)
                .build()
                .encode()
                .toUriString();
    }

    private String providerVersion(List<ClassifiedProvider> providers) {
        if (pluginManager.isDevelopment()
                || providers.stream().anyMatch(provider -> provider.candidate().development())) {
            return String.valueOf(clock.instant().toEpochMilli());
        }
        var value = providers.stream()
                .map(provider -> provider.candidate().type()
                        + ':'
                        + provider.candidate().name()
                        + ':'
                        + provider.candidate().version())
                .collect(Collectors.joining());
        return Hashing.sha256().hashUnencodedChars(value).toString();
    }

    private Flux<DataBuffer> readProviderBundle(ClassifiedProvider provider, String bundleName, boolean javascript) {
        var resource = providerResource(provider.candidate(), bundleName);
        if (resource == null || !resource.isReadable()) {
            return Flux.empty();
        }
        var dataBufferFactory = DefaultDataBufferFactory.sharedInstance;
        var head = Mono.<DataBuffer>fromSupplier(
                () -> dataBufferFactory.wrap(((javascript ? "// Generated from " : "/* Generated from ")
                                + provider.candidate().type()
                                + ' '
                                + provider.candidate().name()
                                + (javascript ? "\n" : " */\n"))
                        .getBytes(UTF_8)));
        var content = DataBufferUtils.read(resource, dataBufferFactory, StreamUtils.BUFFER_SIZE);
        var tail = Mono.fromSupplier(() -> dataBufferFactory.wrap("\n".getBytes(UTF_8)));
        return Flux.concat(head, content, tail);
    }

    private String enabledUiPluginsScript(List<ClassifiedProvider> providers) {
        var legacy = providers.stream()
                .filter(provider -> provider.kind() == ProviderKind.LEGACY)
                .filter(provider -> PLUGIN_TYPE.equals(provider.candidate().type())
                        || providerResource(provider.candidate(), BundleResourceUtils.JS_BUNDLE) != null)
                .toList();
        var uiPlugins = legacy.stream()
                .map(provider -> {
                    var metadata = new LinkedHashMap<String, String>();
                    metadata.put("name", provider.candidate().name());
                    metadata.put("type", provider.candidate().type());
                    if (THEME_TYPE.equals(provider.candidate().type())) {
                        metadata.put("themeName", provider.candidate().resourceName());
                    }
                    metadata.put("version", provider.candidate().version());
                    return JsonUtils.objectToJson(metadata);
                })
                .collect(Collectors.joining(","));
        var plugins = legacy.stream()
                .filter(provider -> PLUGIN_TYPE.equals(provider.candidate().type()))
                .map(provider -> {
                    var metadata = new LinkedHashMap<String, String>();
                    metadata.put("name", provider.candidate().name());
                    metadata.put("version", provider.candidate().version());
                    return JsonUtils.objectToJson(metadata);
                })
                .collect(Collectors.joining(","));
        // TODO(Halo 3): Remove after legacy IIFE UI provider support ends.
        return "this.enabledUiPlugins = [" + uiPlugins + "];this.enabledPlugins = [" + plugins + ']';
    }

    private static boolean isThemeInDevelopment(Theme theme) {
        var status = theme.getStatus();
        return status != null && Boolean.TRUE.equals(status.getInDevelopment());
    }

    @Override
    @SuppressWarnings("java:S5443")
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

    private enum ProviderKind {
        LEGACY,
        ESM,
        INVALID
    }

    private record ProviderManifest(String entry, String style) {}

    private record ProviderCandidate(
            String name,
            String resourceName,
            String type,
            String version,
            String bundleLocation,
            boolean development) {}

    private record ClassifiedProvider(
            ProviderCandidate candidate, ProviderKind kind, ProviderManifest manifest, String error) {

        static ClassifiedProvider legacy(ProviderCandidate candidate) {
            return new ClassifiedProvider(candidate, ProviderKind.LEGACY, null, null);
        }

        static ClassifiedProvider esm(ProviderCandidate candidate, ProviderManifest manifest) {
            return new ClassifiedProvider(candidate, ProviderKind.ESM, manifest, null);
        }

        static ClassifiedProvider invalid(ProviderCandidate candidate, String error) {
            return new ClassifiedProvider(candidate, ProviderKind.INVALID, null, error);
        }
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
                    .map(newVersion -> buildBundleFilename(newVersion, suffix))
                    .flatMap(newFilename -> computeByFilename(filename, newFilename, content));
        }

        private Mono<Resource> computeByFilename(
                String requestedFilename, String newFilename, Publisher<DataBuffer> content) {
            if (isResourceMatch(this.resource, newFilename)) {
                return Mono.just(resource);
            }
            if (writing.compareAndSet(false, true)) {
                return writeBundle(newFilename, content).doFinally(signalType -> writing.set(false));
            }
            return waitForBundle(requestedFilename);
        }

        private Mono<Resource> writeBundle(String newFilename, Publisher<DataBuffer> content) {
            return Mono.justOrEmpty(this.resource)
                    .filter(res -> isResourceMatch(res, newFilename))
                    .switchIfEmpty(Mono.using(
                                    () -> bundlePath(newFilename),
                                    path -> DataBufferUtils.write(content, path, CREATE, TRUNCATE_EXISTING)
                                            .then(Mono.<Resource>fromSupplier(() -> new FileSystemResource(path))),
                                    path -> {
                                        if (shouldCleanUp(path)) {
                                            cleanUp(this.resource);
                                        }
                                    })
                            .subscribeOn(scheduler)
                            .doOnNext(newResource -> this.resource = newResource));
        }

        private Path bundlePath(String filename) throws IOException {
            if (!Files.exists(tempDir)) {
                Files.createDirectories(tempDir);
            }
            return tempDir.resolve(filename);
        }

        private Mono<Resource> waitForBundle(String filename) {
            return Mono.defer(() -> {
                        if (this.writing.get()) {
                            log.debug("Waiting for the UI plugin bundle file {} to be written", filename);
                            return Mono.empty();
                        }
                        log.debug("Waited the UI plugin bundle file {} to be written", filename);
                        return Mono.just(this.resource);
                    })
                    .repeatWhenEmpty(100, count -> count.delayElements(Duration.ofMillis(100)));
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

    private static String buildBundleFilename(String version, String suffix) {
        Assert.notNull(version, "Version must not be null");
        Assert.notNull(suffix, "Suffix must not be null");
        return version + suffix;
    }
}
