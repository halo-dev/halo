package run.halo.app.plugin;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import run.halo.app.core.extension.Theme;
import run.halo.app.infra.SystemConfigChangedEvent;
import run.halo.app.infra.ThemeRootGetter;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.plugin.event.HaloPluginStartedEvent;
import run.halo.app.plugin.event.HaloPluginStoppedEvent;
import run.halo.app.theme.ThemeUiResources;
import run.halo.app.theme.service.ThemeService;

@Slf4j
@Component
public class UiPluginBundleServiceImpl implements UiPluginBundleService, InitializingBean, DisposableBean {

    static final String PROVIDER_MANIFEST = "ui-plugin.json";
    private static final int RETAINED_SNAPSHOTS = 2;
    private static final String PLUGIN_TYPE = "plugin";
    private static final String THEME_TYPE = "theme";

    private final SpringPluginManager pluginManager;
    private final ThemeService themeService;
    private final ThemeRootGetter themeRoot;
    private final Scheduler scheduler = Schedulers.boundedElastic();
    private final AtomicLong invalidationRevision = new AtomicLong(1);
    private final Map<String, InternalSnapshot> snapshots = new LinkedHashMap<>();

    private volatile InternalSnapshot currentSnapshot;
    private volatile long snapshotRevision;
    private Path tempDir;

    public UiPluginBundleServiceImpl(
            SpringPluginManager pluginManager, ThemeService themeService, ThemeRootGetter themeRoot) {
        this.pluginManager = pluginManager;
        this.themeService = themeService;
        this.themeRoot = themeRoot;
    }

    @Override
    public Flux<DataBuffer> uglifyJsBundle() {
        return getProviderSnapshot()
                .flatMap(snapshot -> getJsBundle(snapshot.generation()))
                .flatMapMany(this::readResource);
    }

    @Override
    public Flux<DataBuffer> uglifyCssBundle() {
        return getProviderSnapshot()
                .flatMap(snapshot -> getCssBundle(snapshot.generation()))
                .flatMapMany(this::readResource);
    }

    @Override
    public Mono<String> generateBundleVersion() {
        return getProviderSnapshot().map(UiPluginProviderSnapshot::generation);
    }

    @Override
    public Mono<Resource> getJsBundle(String version) {
        return getSnapshot(version).map(InternalSnapshot::jsBundle);
    }

    @Override
    public Mono<Resource> getCssBundle(String version) {
        return getSnapshot(version).map(InternalSnapshot::cssBundle);
    }

    @Override
    public Mono<UiPluginProviderSnapshot> getProviderSnapshot() {
        var cached = currentSnapshot;
        var revision = invalidationRevision.get();
        if (cached != null && snapshotRevision == revision && !cached.development()) {
            return Mono.just(cached.descriptor());
        }
        var plugins = pluginManager.startedPlugins().stream()
                .sorted(Comparator.comparing(PluginWrapper::getPluginId))
                .toList();
        return themeService
                .fetchActivatedTheme()
                .map(Optional::of)
                .defaultIfEmpty(Optional.empty())
                .flatMap(theme -> Mono.fromCallable(() -> buildSnapshot(plugins, theme.orElse(null), revision))
                        .subscribeOn(scheduler))
                .map(InternalSnapshot::descriptor);
    }

    @Override
    public Mono<Resource> getProviderResource(String generation, String type, String name, String resourcePath) {
        return getSnapshot(generation)
                .flatMap(snapshot -> Mono.fromSupplier(() -> {
                    var providerRoot = snapshot.providerRoots().get(providerKey(type, name));
                    if (providerRoot == null) {
                        return null;
                    }
                    try {
                        var normalized = normalizeResourcePath(resourcePath);
                        var path = providerRoot
                                .resolve(normalized)
                                .toAbsolutePath()
                                .normalize();
                        if (!path.startsWith(providerRoot)
                                || !Files.isRegularFile(path)
                                || !path.toRealPath().startsWith(providerRoot.toRealPath())) {
                            return null;
                        }
                        return (Resource) new FileSystemResource(path);
                    } catch (IllegalArgumentException | IOException e) {
                        return null;
                    }
                }));
    }

    @EventListener
    void onPluginStarted(HaloPluginStartedEvent ignored) {
        invalidationRevision.incrementAndGet();
    }

    @EventListener
    void onPluginStopped(HaloPluginStoppedEvent ignored) {
        invalidationRevision.incrementAndGet();
    }

    @EventListener
    void onSystemConfigChanged(SystemConfigChangedEvent ignored) {
        invalidationRevision.incrementAndGet();
    }

    @Override
    @SuppressWarnings("java:S5443")
    public void afterPropertiesSet() throws Exception {
        this.tempDir = Files.createTempDirectory("halo-ui-provider-snapshots");
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

    synchronized int snapshotCount() {
        return snapshots.size();
    }

    private synchronized InternalSnapshot buildSnapshot(
            List<PluginWrapper> plugins, Theme activatedTheme, long revision) throws IOException {
        var cached = currentSnapshot;
        if (cached != null && snapshotRevision == revision && !cached.development()) {
            return cached;
        }

        Files.createDirectories(tempDir);
        var staging = tempDir.resolve("staging-" + UUID.randomUUID());
        Files.createDirectories(staging);
        try {
            var candidates = new ArrayList<ProviderCandidate>();
            for (var plugin : plugins) {
                candidates.add(copyPluginProvider(plugin, staging));
            }
            if (activatedTheme != null) {
                candidates.add(copyThemeProvider(activatedTheme, staging));
            }

            var classified = candidates.stream().map(this::classify).toList();
            var generation = fingerprint(classified);
            var existing = snapshots.get(generation);
            if (existing != null) {
                FileSystemUtils.deleteRecursively(staging);
                snapshots.remove(generation);
                snapshots.put(generation, existing);
                currentSnapshot = existing;
                snapshotRevision = revision;
                return existing;
            }

            var snapshotRoot = tempDir.resolve(generation);
            Files.move(staging, snapshotRoot, StandardCopyOption.ATOMIC_MOVE);
            var relocated = classified.stream()
                    .map(provider -> provider.relocate(staging, snapshotRoot))
                    .toList();
            var internal = createInternalSnapshot(generation, snapshotRoot, relocated);
            snapshots.put(generation, internal);
            currentSnapshot = internal;
            snapshotRevision = revision;
            evictSnapshots();
            return internal;
        } catch (IOException | RuntimeException | Error error) {
            FileSystemUtils.deleteRecursively(staging);
            throw error;
        }
    }

    private ProviderCandidate copyPluginProvider(PluginWrapper plugin, Path staging) throws IOException {
        var name = plugin.getPluginId();
        var target = providerTarget(staging, PLUGIN_TYPE, name);
        Files.createDirectories(target);
        copyPluginUiRoot(plugin, target);
        return new ProviderCandidate(
                name,
                name,
                PLUGIN_TYPE,
                Objects.toString(plugin.getDescriptor().getVersion(), ""),
                target,
                pluginManager.isDevelopment());
    }

    private ProviderCandidate copyThemeProvider(Theme theme, Path staging) throws IOException {
        var themeName = theme.getMetadata().getName();
        var moduleName = ThemeUiResources.buildModuleName(themeName);
        var target = providerTarget(staging, THEME_TYPE, themeName);
        Files.createDirectories(target);
        var source = themeRoot
                .get()
                .resolve(themeName)
                .resolve(ThemeUiResources.UI_LOCATION)
                .resolve(ThemeUiResources.DIST_LOCATION);
        if (Files.isDirectory(source)) {
            FileSystemUtils.copyRecursively(source, target);
        }
        var status = theme.getStatus();
        return new ProviderCandidate(
                moduleName,
                themeName,
                THEME_TYPE,
                Objects.toString(theme.getSpec().getVersion(), ""),
                target,
                status != null && Boolean.TRUE.equals(status.getInDevelopment()));
    }

    private void copyPluginUiRoot(PluginWrapper plugin, Path target) throws IOException {
        if (!(plugin.getPluginClassLoader() instanceof URLClassLoader classLoader)) {
            return;
        }
        for (var url : classLoader.getURLs()) {
            if (!"file".equals(url.getProtocol())) {
                continue;
            }
            try {
                var classpathPath = Path.of(url.toURI());
                if (Files.isDirectory(classpathPath)) {
                    var selected = selectPluginUiRoot(classpathPath);
                    if (selected != null) {
                        FileSystemUtils.copyRecursively(selected, target);
                        return;
                    }
                } else if (Files.isRegularFile(classpathPath)) {
                    try (var fileSystem = FileSystems.newFileSystem(classpathPath)) {
                        var selected = selectPluginUiRoot(fileSystem.getPath("/"));
                        if (selected != null) {
                            FileSystemUtils.copyRecursively(selected, target);
                            return;
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("Unable to copy UI root from {}", url, e);
            }
        }
    }

    private static Path selectPluginUiRoot(Path classpathRoot) {
        for (var location : List.of("ui", "console")) {
            var candidate = classpathRoot.resolve(location);
            if (Files.isDirectory(candidate)
                    && (Files.exists(candidate.resolve(PROVIDER_MANIFEST))
                            || Files.exists(candidate.resolve("main.js"))
                            || Files.exists(candidate.resolve("style.css")))) {
                return candidate;
            }
        }
        return null;
    }

    private ClassifiedProvider classify(ProviderCandidate candidate) {
        var manifestPath = candidate.root().resolve(PROVIDER_MANIFEST);
        if (!Files.exists(manifestPath)) {
            return ClassifiedProvider.legacy(candidate);
        }
        try {
            var manifest = readManifest(manifestPath, candidate.root());
            return ClassifiedProvider.esm(candidate, manifest);
        } catch (Exception e) {
            return ClassifiedProvider.invalid(
                    candidate, Objects.toString(e.getMessage(), e.getClass().getSimpleName()));
        }
    }

    private static ProviderManifest readManifest(Path manifestPath, Path providerRoot) throws IOException {
        JsonNode manifest = JsonUtils.mapper().readTree(manifestPath.toFile());
        if (!manifest.isObject()) {
            throw new IllegalArgumentException("Provider manifest must be an object");
        }
        var fields = new HashSet<String>();
        manifest.fieldNames().forEachRemaining(fields::add);
        if (!Set.of("format", "entry", "styles").equals(fields)) {
            throw new IllegalArgumentException("Provider manifest must contain only format, entry, and styles");
        }
        if (!"esm".equals(manifest.path("format").textValue())) {
            throw new IllegalArgumentException("Provider manifest format must be esm");
        }
        var entry = validateManifestResource(providerRoot, manifest.path("entry"));
        var stylesNode = manifest.path("styles");
        if (!stylesNode.isArray()) {
            throw new IllegalArgumentException("Provider manifest styles must be an array");
        }
        var styles = new ArrayList<String>();
        for (var style : stylesNode) {
            styles.add(validateManifestResource(providerRoot, style));
        }
        return new ProviderManifest(entry, styles);
    }

    private static String validateManifestResource(Path providerRoot, JsonNode value) {
        if (!value.isTextual()) {
            throw new IllegalArgumentException("Provider resource path must be a string");
        }
        var resourcePath = normalizeResourcePath(value.textValue());
        var root = providerRoot.toAbsolutePath().normalize();
        var path = root.resolve(resourcePath).normalize();
        try {
            if (!path.startsWith(root)
                    || !Files.isRegularFile(path)
                    || !Files.isReadable(path)
                    || !path.toRealPath().startsWith(root.toRealPath())) {
                throw new IllegalArgumentException(
                        "Provider resource does not exist inside its root: " + value.textValue());
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Provider resource does not exist inside its root: " + value.textValue(), e);
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

    private InternalSnapshot createInternalSnapshot(
            String generation, Path snapshotRoot, List<ClassifiedProvider> providers) throws IOException {
        var registrations = providers.stream()
                .map(provider -> new UiPluginProviderSnapshot.Registration(
                        provider.candidate().name(),
                        provider.candidate().type(),
                        provider.candidate().version()))
                .toList();
        var esmProviders = providers.stream()
                .filter(provider -> provider.kind() == ProviderKind.ESM)
                .map(provider -> new UiPluginProviderSnapshot.EsmProvider(
                        provider.candidate().name(),
                        provider.candidate().type(),
                        provider.candidate().version(),
                        providerUrl(generation, provider, provider.manifest().entry()),
                        provider.manifest().styles().stream()
                                .map(style -> providerUrl(generation, provider, style))
                                .toList()))
                .toList();
        var invalid = providers.stream()
                .filter(provider -> provider.kind() == ProviderKind.INVALID)
                .map(provider -> new UiPluginProviderSnapshot.InvalidProvider(
                        provider.candidate().name(),
                        provider.candidate().type(),
                        provider.candidate().version(),
                        provider.error()))
                .toList();
        var baseUrl = "/apis/api.console.halo.run/v1alpha1/ui-plugins/-/snapshots/" + generation;
        var descriptor = new UiPluginProviderSnapshot(
                generation,
                new UiPluginProviderSnapshot.LegacyResources(baseUrl + "/bundle.js", baseUrl + "/bundle.css"),
                registrations,
                esmProviders,
                invalid);
        var jsBundle = writeLegacyBundle(snapshotRoot, providers, true);
        var cssBundle = writeLegacyBundle(snapshotRoot, providers, false);
        var roots = providers.stream()
                .filter(provider -> provider.kind() == ProviderKind.ESM)
                .collect(java.util.stream.Collectors.toUnmodifiableMap(
                        provider -> providerKey(
                                provider.candidate().type(),
                                provider.candidate().name()),
                        provider -> provider.candidate().root().toAbsolutePath().normalize()));
        var development =
                providers.stream().anyMatch(provider -> provider.candidate().development());
        return new InternalSnapshot(descriptor, jsBundle, cssBundle, roots, snapshotRoot, development);
    }

    private Resource writeLegacyBundle(Path snapshotRoot, List<ClassifiedProvider> providers, boolean javascript)
            throws IOException {
        var output = snapshotRoot.resolve(javascript ? "bundle.js" : "bundle.css");
        var content = new StringBuilder();
        for (var provider : providers) {
            if (provider.kind() != ProviderKind.LEGACY) {
                continue;
            }
            var bundle = provider.candidate().root().resolve(javascript ? "main.js" : "style.css");
            if (Files.isRegularFile(bundle)) {
                content.append(javascript ? "// Generated from " : "/* Generated from ")
                        .append(provider.candidate().type())
                        .append(' ')
                        .append(provider.candidate().name())
                        .append(javascript ? "\n" : " */\n")
                        .append(Files.readString(bundle))
                        .append('\n');
            }
        }
        if (javascript) {
            content.append(enabledUiPluginsScript(providers));
        }
        Files.writeString(output, content, UTF_8);
        return new FileSystemResource(output);
    }

    private static String enabledUiPluginsScript(List<ClassifiedProvider> providers) {
        var legacy = providers.stream()
                .filter(provider -> provider.kind() == ProviderKind.LEGACY)
                .filter(provider -> PLUGIN_TYPE.equals(provider.candidate().type())
                        || Files.isRegularFile(provider.candidate().root().resolve("main.js")))
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
                .collect(java.util.stream.Collectors.joining(","));
        var plugins = legacy.stream()
                .filter(provider -> PLUGIN_TYPE.equals(provider.candidate().type()))
                .map(provider -> {
                    var metadata = new LinkedHashMap<String, String>();
                    metadata.put("name", provider.candidate().name());
                    metadata.put("version", provider.candidate().version());
                    return JsonUtils.objectToJson(metadata);
                })
                .collect(java.util.stream.Collectors.joining(","));
        // TODO(Halo 3): Remove after legacy IIFE UI provider support ends.
        return "this.enabledUiPlugins = [" + uiPlugins + "];this.enabledPlugins = [" + plugins + "]";
    }

    private static String providerUrl(String generation, ClassifiedProvider provider, String resourcePath) {
        return "/apis/api.console.halo.run/v1alpha1/ui-plugins/-/snapshots/"
                + generation
                + "/providers/"
                + UriUtils.encodePathSegment(provider.candidate().type(), UTF_8)
                + '/'
                + UriUtils.encodePathSegment(provider.candidate().name(), UTF_8)
                + '/'
                + UriUtils.encodePath(resourcePath, UTF_8);
    }

    private static String fingerprint(List<ClassifiedProvider> providers) throws IOException {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            for (var provider : providers) {
                updateDigest(
                        digest,
                        provider.candidate().name()
                                + ':'
                                + provider.candidate().type()
                                + ':'
                                + provider.candidate().version()
                                + ':'
                                + provider.kind());
                try (Stream<Path> paths = Files.walk(provider.candidate().root())) {
                    for (var path : paths.filter(Files::isRegularFile).sorted().toList()) {
                        updateDigest(
                                digest,
                                provider.candidate().root().relativize(path).toString());
                        digest.update(Files.readAllBytes(path));
                    }
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    private static void updateDigest(MessageDigest digest, String value) {
        digest.update(value.getBytes(UTF_8));
    }

    private synchronized Mono<InternalSnapshot> getSnapshot(String generation) {
        return Mono.justOrEmpty(snapshots.get(generation));
    }

    private void evictSnapshots() throws IOException {
        while (snapshots.size() > RETAINED_SNAPSHOTS) {
            var iterator = snapshots.entrySet().iterator();
            var evicted = iterator.next().getValue();
            iterator.remove();
            FileSystemUtils.deleteRecursively(evicted.root());
        }
    }

    private Flux<DataBuffer> readResource(Resource resource) {
        return DataBufferUtils.read(resource, DefaultDataBufferFactory.sharedInstance, StreamUtils.BUFFER_SIZE);
    }

    private static Path providerTarget(Path root, String type, String name) {
        var providersRoot = root.resolve("providers").toAbsolutePath().normalize();
        var target = providersRoot.resolve(type).resolve(name).normalize();
        if (!target.startsWith(providersRoot)) {
            throw new IllegalArgumentException("Invalid provider name: " + name);
        }
        return target;
    }

    private static String providerKey(String type, String name) {
        return type + ':' + name;
    }

    private enum ProviderKind {
        LEGACY,
        ESM,
        INVALID
    }

    private record ProviderManifest(String entry, List<String> styles) {
        ProviderManifest {
            styles = List.copyOf(styles);
        }
    }

    private record ProviderCandidate(
            String name, String resourceName, String type, String version, Path root, boolean development) {}

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

        ClassifiedProvider relocate(Path oldRoot, Path newRoot) {
            return new ClassifiedProvider(
                    new ProviderCandidate(
                            candidate.name(),
                            candidate.resourceName(),
                            candidate.type(),
                            candidate.version(),
                            newRoot.resolve(oldRoot.relativize(candidate.root())),
                            candidate.development()),
                    kind,
                    manifest,
                    error);
        }
    }

    private record InternalSnapshot(
            UiPluginProviderSnapshot descriptor,
            Resource jsBundle,
            Resource cssBundle,
            Map<String, Path> providerRoots,
            Path root,
            boolean development) {}
}
