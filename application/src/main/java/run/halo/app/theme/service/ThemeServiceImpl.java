package run.halo.app.theme.service;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static org.springframework.util.FileSystemUtils.copyRecursively;
import static run.halo.app.infra.utils.FileUtils.deleteRecursivelyAndSilently;
import static run.halo.app.infra.utils.FileUtils.unzip;
import static run.halo.app.theme.service.ThemeUtils.loadThemeManifest;
import static run.halo.app.theme.service.ThemeUtils.locateThemeManifest;
import static run.halo.app.theme.service.ThemeUtils.unzipThemeTo;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.WritableResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.server.ServerErrorException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;
import run.halo.app.core.extension.AnnotationSetting;
import run.halo.app.core.extension.Setting;
import run.halo.app.core.extension.Theme;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Unstructured;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.SystemVersionSupplier;
import run.halo.app.infra.ThemeRootGetter;
import run.halo.app.infra.exception.ThemeAlreadyExistsException;
import run.halo.app.infra.exception.ThemeUpgradeException;
import run.halo.app.infra.exception.UnsatisfiedAttributeValueException;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.infra.utils.FileUtils;
import run.halo.app.infra.utils.SettingUtils;
import run.halo.app.infra.utils.VersionUtils;
import run.halo.app.theme.ThemeUiResources;

@Slf4j
@Service
public class ThemeServiceImpl implements ThemeService, InitializingBean, DisposableBean {

    private final ReactiveExtensionClient client;

    private final ThemeRootGetter themeRoot;

    private final HaloProperties haloProperties;

    private final SystemVersionSupplier systemVersionSupplier;

    private final SystemConfigFetcher systemConfigFetcher;

    private final BundleCache jsBundleCache;

    private final BundleCache cssBundleCache;

    private Path tempDir;

    private final Scheduler scheduler = Schedulers.boundedElastic();

    public ThemeServiceImpl(
            ReactiveExtensionClient client,
            ThemeRootGetter themeRoot,
            HaloProperties haloProperties,
            SystemVersionSupplier systemVersionSupplier,
            SystemConfigFetcher systemConfigFetcher) {
        this.client = client;
        this.themeRoot = themeRoot;
        this.haloProperties = haloProperties;
        this.systemVersionSupplier = systemVersionSupplier;
        this.systemConfigFetcher = systemConfigFetcher;
        this.jsBundleCache = new BundleCache(".js");
        this.cssBundleCache = new BundleCache(".css");
    }

    @Override
    public Mono<Void> installPresetTheme() {
        var themeProps = haloProperties.getTheme();
        var location = themeProps.getInitializer().getLocation();
        return Mono.using(
                        () -> Files.createTempDirectory("halo-theme-preset"),
                        tempDir -> Mono.fromCallable(() -> {
                                    var themeUrl = ResourceUtils.getURL(location);
                                    var resource = new UrlResource(themeUrl);
                                    var tempThemePath = tempDir.resolve("theme.zip");
                                    FileUtils.copyResource(resource, tempThemePath);
                                    return tempThemePath;
                                })
                                .flatMap(themePath -> {
                                    var content = DataBufferUtils.read(
                                            new FileSystemResource(themePath),
                                            DefaultDataBufferFactory.sharedInstance,
                                            StreamUtils.BUFFER_SIZE);
                                    // We don't want to run on the bounded elastic scheduler again, so pass null
                                    // here.
                                    return doInstall(content, null);
                                }),
                        FileUtils::deleteRecursivelyAndSilently)
                .subscribeOn(scheduler)
                .onErrorResume(IOException.class, e -> {
                    log.warn("Failed to initialize theme from {}", location, e);
                    return Mono.empty();
                })
                .onErrorResume(ThemeAlreadyExistsException.class, e -> {
                    log.warn("Failed to initialize theme from {}, because it already exists", location);
                    return Mono.empty();
                })
                .then();
    }

    @Override
    public Mono<Theme> install(Publisher<DataBuffer> content) {
        return doInstall(content, this.scheduler);
    }

    @Override
    public Mono<Theme> upgrade(String themeName, Publisher<DataBuffer> content) {
        var checkTheme = client.fetch(Theme.class, themeName)
                .switchIfEmpty(Mono.error(() ->
                        new ServerWebInputException("The given theme with name " + themeName + " did not exist")));
        var upgradeTheme = Mono.using(
                        () -> Files.createTempDirectory("halo-theme-"),
                        tempDir -> {
                            var locateThemeManifest = Mono.fromCallable(
                                            () -> locateThemeManifest(tempDir).orElse(null))
                                    .switchIfEmpty(Mono.error(() -> new ThemeUpgradeException(
                                            "Missing theme manifest file: theme.yaml or theme.yml",
                                            "problemDetail.theme.upgrade.missingManifest",
                                            null)));
                            return unzip(content, tempDir)
                                    .then(locateThemeManifest)
                                    .flatMap(themeManifest -> {
                                        if (log.isDebugEnabled()) {
                                            log.debug("Found theme manifest file: {}", themeManifest);
                                        }
                                        var newTheme = loadThemeManifest(themeManifest);
                                        if (!Objects.equals(
                                                themeName,
                                                newTheme.getMetadata().getName())) {
                                            if (log.isDebugEnabled()) {
                                                log.error(
                                                        "Want theme name: {}, but provided: {}",
                                                        themeName,
                                                        newTheme.getMetadata().getName());
                                            }
                                            return Mono.error(new ThemeUpgradeException(
                                                    "Please make sure the theme name is correct",
                                                    "problemDetail.theme.upgrade.nameMismatch",
                                                    new Object[] {
                                                        newTheme.getMetadata().getName(), themeName
                                                    }));
                                        }
                                        var copyTheme = Mono.fromCallable(() -> {
                                            var themePath = themeRoot.get().resolve(themeName);
                                            // TODO Create backup before deleting
                                            deleteRecursivelyAndSilently(themePath);
                                            copyRecursively(themeManifest.getParent(), themePath);
                                            return themePath;
                                        });
                                        return copyTheme.then(this.persistent(newTheme, true));
                                    });
                        },
                        FileUtils::deleteRecursivelyAndSilently)
                .subscribeOn(scheduler);
        return checkTheme.then(upgradeTheme);
    }

    private Mono<Theme> doInstall(Publisher<DataBuffer> content, @Nullable Scheduler scheduler) {
        var themeRoot = this.themeRoot.get();
        return unzipThemeTo(content, themeRoot, scheduler).flatMap(theme -> persistent(theme, false));
    }

    /**
     * Creates theme manifest and related unstructured resources. TODO: In case of failure in saving midway, the problem
     * of data consistency needs to be solved.
     *
     * @param themeManifest the theme custom model
     * @return a theme custom model
     * @see Theme
     */
    private Mono<Theme> persistent(Unstructured themeManifest, boolean isUpgrade) {
        Assert.state(StringUtils.equals(Theme.KIND, themeManifest.getKind()), "Theme manifest kind must be Theme.");
        var newTheme = Unstructured.OBJECT_MAPPER.convertValue(themeManifest, Theme.class);
        final Mono<Theme> createOrUpdateTheme;
        if (isUpgrade) {
            createOrUpdateTheme = client.get(Theme.class, newTheme.getMetadata().getName())
                    .doOnNext(theme -> updateTheme(theme, newTheme))
                    .flatMap(client::update);
        } else {
            createOrUpdateTheme = client.create(newTheme);
        }
        return createOrUpdateTheme
                .doOnNext(theme -> {
                    String systemVersion =
                            systemVersionSupplier.get().toStableVersion().toString();
                    String requires = theme.getSpec().getRequires();
                    if (!VersionUtils.satisfiesRequires(systemVersion, requires)) {
                        throw new UnsatisfiedAttributeValueException(
                                String.format(
                                        "The theme requires a minimum system version of %s, "
                                                + "but the current version is %s.",
                                        requires, systemVersion),
                                "problemDetail.theme.version.unsatisfied.requires",
                                new String[] {requires, systemVersion});
                    }
                })
                .delayUntil(theme -> {
                    var unstructureds = ThemeUtils.loadThemeResources(getThemePath(theme));
                    if (unstructureds.stream().filter(hasSettingsYaml(theme)).count() > 1) {
                        return Mono.error(
                                new IllegalStateException("Theme must only have one settings.yaml or settings.yml."));
                    }
                    if (unstructureds.stream().filter(hasConfigYaml(theme)).count() > 1) {
                        return Mono.error(
                                new IllegalStateException("Theme must only have one config.yaml or config.yml."));
                    }
                    return Mono.empty();
                });
    }

    @Override
    public Mono<Theme> reloadTheme(String name) {
        return client.fetch(Theme.class, name)
                .delayUntil(oldTheme -> {
                    String settingName = oldTheme.getSpec().getSettingName();
                    return waitForSettingDeleted(settingName).then(waitForAnnotationSettingsDeleted(name));
                })
                .flatMap(oldTheme -> {
                    var themePath = themeRoot.get().resolve(name);
                    var themeManifestPath = ThemeUtils.resolveThemeManifest(themePath);
                    if (themeManifestPath == null) {
                        return Mono.error(new ServerWebInputException("The manifest file [theme.yaml] is required."));
                    }
                    var unstructured = loadThemeManifest(themeManifestPath);
                    var newTheme = Unstructured.OBJECT_MAPPER.convertValue(unstructured, Theme.class);
                    updateTheme(oldTheme, newTheme);
                    return client.update(oldTheme);
                });
    }

    @Override
    public Flux<DataBuffer> uglifyJsBundle() {
        var dataBufferFactory = DefaultDataBufferFactory.sharedInstance;
        return fetchActivatedThemeBundle(ThemeUiResources.JS_BUNDLE)
                .flatMapMany(bundle -> {
                    var themeName = bundle.theme().getMetadata().getName();
                    var head = Mono.<DataBuffer>fromSupplier(() -> dataBufferFactory.wrap(
                            ("// Generated from theme " + themeName + "\n").getBytes(StandardCharsets.UTF_8)));
                    var content = DataBufferUtils.read(bundle.resource(), dataBufferFactory, StreamUtils.BUFFER_SIZE);
                    var tail = Mono.fromSupplier(() -> dataBufferFactory.wrap(
                            ("\n" + enabledThemesScript(bundle.theme())).getBytes(StandardCharsets.UTF_8)));
                    return Flux.concat(head, content, tail);
                })
                .switchIfEmpty(Mono.fromSupplier(
                        () -> dataBufferFactory.wrap(emptyEnabledThemesScript().getBytes(StandardCharsets.UTF_8))));
    }

    @Override
    public Flux<DataBuffer> uglifyCssBundle() {
        var dataBufferFactory = DefaultDataBufferFactory.sharedInstance;
        return fetchActivatedThemeBundle(ThemeUiResources.CSS_BUNDLE).flatMapMany(bundle -> {
            var themeName = bundle.theme().getMetadata().getName();
            var head = Mono.<DataBuffer>fromSupplier(() -> dataBufferFactory.wrap(
                    ("/* Generated from theme " + themeName + " */\n").getBytes(StandardCharsets.UTF_8)));
            var content = DataBufferUtils.read(bundle.resource(), dataBufferFactory, StreamUtils.BUFFER_SIZE);
            var tail = Mono.fromSupplier(() -> dataBufferFactory.wrap("\n".getBytes(StandardCharsets.UTF_8)));
            return Flux.concat(head, content, tail);
        });
    }

    private Mono<ThemeBundle> fetchActivatedThemeBundle(String bundleName) {
        return fetchActivatedTheme()
                .flatMap(theme ->
                        getBundleResource(theme, bundleName).map(resource -> new ThemeBundle(theme, resource)));
    }

    private Mono<Resource> getBundleResource(Theme theme, String bundleName) {
        return Mono.fromSupplier(() -> ThemeUiResources.getBundleResource(
                themeRoot.get(), theme.getMetadata().getName(), bundleName));
    }

    @Override
    public Mono<String> generateBundleVersion() {
        return fetchActivatedTheme()
                .map(theme -> theme.getMetadata().getName()
                        + ':'
                        + Objects.toString(theme.getSpec().getVersion(), ""))
                .defaultIfEmpty(StringUtils.EMPTY)
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
    public Mono<ConfigMap> resetSettingConfig(String name) {
        return client.fetch(Theme.class, name)
                .filter(theme -> StringUtils.isNotBlank(theme.getSpec().getSettingName()))
                .flatMap(theme -> {
                    String configMapName = theme.getSpec().getConfigMapName();
                    String settingName = theme.getSpec().getSettingName();
                    return client.fetch(Setting.class, settingName)
                            .map(SettingUtils::settingDefinedDefaultValueMap)
                            .flatMap(data -> updateConfigMapData(configMapName, data));
                });
    }

    @Override
    public Mono<Theme> fetchActivatedTheme() {
        return fetchSystemSetting()
                .mapNotNull(SystemSetting.Theme::getActive)
                .flatMap(name -> client.fetch(Theme.class, name));
    }

    @Override
    public Mono<SystemSetting.Theme> fetchSystemSetting() {
        return systemConfigFetcher.fetch(SystemSetting.Theme.GROUP, SystemSetting.Theme.class);
    }

    private Mono<ConfigMap> updateConfigMapData(String configMapName, Map<String, String> data) {
        return client.fetch(ConfigMap.class, configMapName)
                .flatMap(configMap -> {
                    configMap.setData(data);
                    return client.update(configMap);
                })
                .retryWhen(Retry.fixedDelay(10, Duration.ofMillis(100))
                        .filter(t -> t instanceof OptimisticLockingFailureException));
    }

    private Mono<Void> waitForSettingDeleted(String settingName) {
        return client.fetch(Setting.class, settingName)
                .flatMap(setting -> client.delete(setting)
                        .flatMap(deleted -> client.fetch(Setting.class, settingName)
                                .flatMap(s -> Mono.error(
                                        () -> new IllegalStateException("Re-check if the setting is deleted.")))
                                .retryWhen(Retry.fixedDelay(10, Duration.ofMillis(100))
                                        .filter(IllegalStateException.class::isInstance))))
                .then();
    }

    private Mono<Void> waitForAnnotationSettingsDeleted(String themeName) {
        return client.list(
                        AnnotationSetting.class,
                        annotationSetting -> {
                            Map<String, String> labels = MetadataUtil.nullSafeLabels(annotationSetting);
                            return StringUtils.equals(themeName, labels.get(Theme.THEME_NAME_LABEL));
                        },
                        null)
                .flatMap(annotationSetting -> client.delete(annotationSetting)
                        .flatMap(deleted -> client.fetch(
                                        AnnotationSetting.class,
                                        annotationSetting.getMetadata().getName())
                                .flatMap(latest ->
                                        Mono.error(new IllegalStateException("AnnotationSetting is not deleted yet.")))
                                .retryWhen(Retry.fixedDelay(10, Duration.ofMillis(100))
                                        .filter(t -> t instanceof IllegalStateException))))
                .then();
    }

    private Path getThemePath(Theme theme) {
        return themeRoot.get().resolve(theme.getMetadata().getName());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.tempDir = Files.createTempDirectory("halo-theme-bundle");
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

    private Predicate<Unstructured> hasSettingsYaml(Theme theme) {
        return unstructured -> Setting.KIND.equals(unstructured.getKind())
                && theme.getSpec()
                        .getSettingName()
                        .equals(unstructured.getMetadata().getName());
    }

    private Predicate<Unstructured> hasConfigYaml(Theme theme) {
        return unstructured -> ConfigMap.KIND.equals(unstructured.getKind())
                && theme.getSpec()
                        .getConfigMapName()
                        .equals(unstructured.getMetadata().getName());
    }

    Mono<Theme> deleteThemeAndWaitForComplete(String themeName) {
        return client.fetch(Theme.class, themeName)
                .flatMap(client::delete)
                .flatMap(deletingTheme -> waitForThemeDeleted(themeName).thenReturn(deletingTheme));
    }

    Mono<Void> waitForThemeDeleted(String themeName) {
        return client.fetch(Theme.class, themeName)
                .flatMap(
                        theme -> Mono.error(new IllegalStateException("Re-check if the theme is deleted successfully")))
                .retryWhen(Retry.fixedDelay(20, Duration.ofMillis(100))
                        .filter(IllegalStateException.class::isInstance)
                        .onRetryExhaustedThrow(
                                (spec, signal) -> new ServerErrorException("Wait timeout for theme deleted", null)))
                .then();
    }

    private static void updateTheme(Theme existing, Theme updating) {
        var existingSpec = existing.getSpec();
        var updatingSpec = updating.getSpec();
        // merge spec
        existingSpec.setAuthor(updatingSpec.getAuthor());
        existingSpec.setCustomTemplates(updatingSpec.getCustomTemplates());
        existingSpec.setDescription(updatingSpec.getDescription());
        existingSpec.setDisplayName(updatingSpec.getDisplayName());
        existingSpec.setHomepage(updatingSpec.getHomepage());
        existingSpec.setIssues(updatingSpec.getIssues());
        existingSpec.setLicense(updatingSpec.getLicense());
        existingSpec.setLogo(updatingSpec.getLogo());
        existingSpec.setRepo(updatingSpec.getRepo());
        existingSpec.setSettingName(updatingSpec.getSettingName());
        existingSpec.setVersion(updatingSpec.getVersion());
        existingSpec.setRequires(updatingSpec.getRequires());
        // Do not overwrite configMapName to avoid data loss

        var existingMeta = existing.getMetadata();
        var updatingMeta = updating.getMetadata();
        // merge labels
        if (updatingMeta.getLabels() != null) {
            if (existingMeta.getLabels() == null) {
                existingMeta.setLabels(new HashMap<>());
            }
            existingMeta.getLabels().putAll(updatingMeta.getLabels());
        }
        if (existingMeta.getAnnotations() == null) {
            existingMeta.setAnnotations(new HashMap<>());
        }
        // make sure the theme will be reloaded after upgrade by adding a special annotation with
        // current timestamp
        existingMeta
                .getAnnotations()
                .put(Theme.REQUEST_RELOAD_ANNOTATION, Instant.now().toString());
        // merge annotations
        if (updatingMeta.getAnnotations() != null) {
            existingMeta.getAnnotations().putAll(updatingMeta.getAnnotations());
        }
    }

    private static String enabledThemesScript(Theme theme) {
        var themeName = theme.getMetadata().getName();
        var moduleName = ThemeUiResources.buildModuleName(themeName);
        return """
            this.enabledThemes = [{"name":"%s","themeName":"%s","version":"%s"}]\
            """.formatted(
                        moduleName, themeName, Objects.toString(theme.getSpec().getVersion(), ""));
    }

    private static String emptyEnabledThemesScript() {
        return "this.enabledThemes = []";
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
                                log.debug("Waiting for the theme bundle file {} to be written", filename);
                                return Mono.empty();
                            }
                            log.debug("Waited the theme bundle file {} to be written", filename);
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
                    log.warn("Failed to delete old theme bundle file {}", wr.getFilename(), e);
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
