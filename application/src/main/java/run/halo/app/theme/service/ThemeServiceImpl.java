package run.halo.app.theme.service;

import static org.springframework.util.FileSystemUtils.copyRecursively;
import static run.halo.app.infra.utils.FileUtils.createTempDir;
import static run.halo.app.infra.utils.FileUtils.deleteRecursivelyAndSilently;
import static run.halo.app.infra.utils.FileUtils.unzip;
import static run.halo.app.theme.service.ThemeUtils.loadThemeManifest;
import static run.halo.app.theme.service.ThemeUtils.locateThemeManifest;
import static run.halo.app.theme.service.ThemeUtils.unzipThemeTo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.RetryException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
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
import run.halo.app.core.extension.notification.NotificationTemplate;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.Extension;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Unstructured;
import run.halo.app.infra.SystemVersionSupplier;
import run.halo.app.infra.ThemeRootGetter;
import run.halo.app.infra.exception.ThemeAlreadyExistsException;
import run.halo.app.infra.exception.ThemeUpgradeException;
import run.halo.app.infra.exception.UnsatisfiedAttributeValueException;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.infra.utils.FileUtils;
import run.halo.app.infra.utils.SettingUtils;
import run.halo.app.infra.utils.VersionUtils;

@Slf4j
@Service
@AllArgsConstructor
public class ThemeServiceImpl implements ThemeService {

    private final ReactiveExtensionClient client;

    private final ThemeRootGetter themeRoot;

    private final HaloProperties haloProperties;

    private final SystemVersionSupplier systemVersionSupplier;

    private final Scheduler scheduler = Schedulers.boundedElastic();

    @Override
    public Mono<Void> installPresetTheme() {
        var themeProps = haloProperties.getTheme();
        var location = themeProps.getInitializer().getLocation();
        return createThemeTempPath()
            .flatMap(tempPath -> Mono.usingWhen(copyPresetThemeToPath(location, tempPath),
                path -> {
                    var content = DataBufferUtils.read(new FileSystemResource(path),
                        DefaultDataBufferFactory.sharedInstance,
                        StreamUtils.BUFFER_SIZE);
                    return install(content);
                }, path -> deleteRecursivelyAndSilently(tempPath, scheduler)
            ))
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

    private Mono<Path> copyPresetThemeToPath(String location, Path tempDir) {
        return Mono.fromCallable(
            () -> {
                var themeUrl = ResourceUtils.getURL(location);
                var resource = new UrlResource(themeUrl);
                var tempThemePath = tempDir.resolve("theme.zip");
                FileUtils.copyResource(resource, tempThemePath);
                return tempThemePath;
            });
    }

    private static Mono<Path> createThemeTempPath() {
        return Mono.fromCallable(() -> Files.createTempDirectory("halo-theme-preset"))
            .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Theme> install(Publisher<DataBuffer> content) {
        var themeRoot = this.themeRoot.get();
        return unzipThemeTo(content, themeRoot, scheduler)
            .flatMap(theme -> persistent(theme, false));
    }

    @Override
    public Mono<Theme> upgrade(String themeName, Publisher<DataBuffer> content) {
        var checkTheme = client.fetch(Theme.class, themeName)
            .switchIfEmpty(Mono.error(() -> new ServerWebInputException(
                "The given theme with name " + themeName + " did not exist")));
        var upgradeTheme = Mono.usingWhen(
            createTempDir("halo-theme-", scheduler),
            tempDir -> {
                var locateThemeManifest = Mono.fromCallable(() -> locateThemeManifest(tempDir)
                    .orElseThrow(() -> new ThemeUpgradeException(
                        "Missing theme manifest file: theme.yaml or theme.yml",
                        "problemDetail.theme.upgrade.missingManifest", null)));
                return unzip(content, tempDir, scheduler)
                    .then(locateThemeManifest)
                    .flatMap(themeManifest -> {
                        if (log.isDebugEnabled()) {
                            log.debug("Found theme manifest file: {}", themeManifest);
                        }
                        var newTheme = loadThemeManifest(themeManifest);
                        if (!Objects.equals(themeName, newTheme.getMetadata().getName())) {
                            if (log.isDebugEnabled()) {
                                log.error("Want theme name: {}, but provided: {}", themeName,
                                    newTheme.getMetadata().getName());
                            }
                            return Mono.error(new ThemeUpgradeException(
                                "Please make sure the theme name is correct",
                                "problemDetail.theme.upgrade.nameMismatch",
                                new Object[] {newTheme.getMetadata().getName(), themeName}));
                        }

                        var copyTheme = Mono.fromCallable(() -> {
                            var themePath = themeRoot.get().resolve(themeName);
                            copyRecursively(themeManifest.getParent(), themePath);
                            return themePath;
                        });

                        return deleteThemeAndWaitForComplete(themeName)
                            .then(copyTheme)
                            .then(this.persistent(newTheme, true));
                    });
            },
            tempDir -> deleteRecursivelyAndSilently(tempDir, scheduler)
        );

        return checkTheme.then(upgradeTheme);
    }

    /**
     * Creates theme manifest and related unstructured resources.
     * TODO: In case of failure in saving midway, the problem of data consistency needs to be
     * solved.
     *
     * @param themeManifest the theme custom model
     * @return a theme custom model
     * @see Theme
     */
    private Mono<Theme> persistent(Unstructured themeManifest, boolean isUpgrade) {
        Assert.state(StringUtils.equals(Theme.KIND, themeManifest.getKind()),
            "Theme manifest kind must be Theme.");
        return client.create(themeManifest)
            .map(theme -> Unstructured.OBJECT_MAPPER.convertValue(theme, Theme.class))
            .doOnNext(theme -> {
                String systemVersion = systemVersionSupplier.get().getNormalVersion();
                String requires = theme.getSpec().getRequires();
                if (!VersionUtils.satisfiesRequires(systemVersion, requires)) {
                    throw new UnsatisfiedAttributeValueException(
                        String.format("The theme requires a minimum system version of %s, "
                                + "but the current version is %s.",
                            requires, systemVersion),
                        "problemDetail.theme.version.unsatisfied.requires",
                        new String[] {requires, systemVersion});
                }
            })
            .flatMap(theme -> {
                var unstructureds = ThemeUtils.loadThemeResources(getThemePath(theme));
                if (unstructureds.stream()
                    .filter(hasSettingsYaml(theme))
                    .count() > 1) {
                    return Mono.error(new IllegalStateException(
                        "Theme must only have one settings.yaml or settings.yml."));
                }
                if (unstructureds.stream()
                    .filter(hasConfigYaml(theme))
                    .count() > 1) {
                    return Mono.error(new IllegalStateException(
                        "Theme must only have one config.yaml or config.yml."));
                }
                return Flux.fromIterable(unstructureds)
                    .filter(unstructured -> ExtensionWhitelist.of(theme).isAllowed(unstructured))
                    .doOnNext(unstructured ->
                        populateThemeNameLabel(unstructured, theme.getMetadata().getName()))
                    .flatMap(unstructured -> {
                        if (isUpgrade) {
                            return createOrUpdate(unstructured);
                        }
                        return client.create(unstructured);
                    })
                    .then(Mono.just(theme));
            });
    }

    private Mono<Unstructured> createOrUpdate(Unstructured unstructured) {
        return Mono.defer(() -> client.fetch(unstructured.groupVersionKind(),
                    unstructured.getMetadata().getName())
                .flatMap(existUnstructured -> {
                    unstructured.getMetadata()
                        .setVersion(existUnstructured.getMetadata().getVersion());
                    return client.update(unstructured);
                })
                .switchIfEmpty(Mono.defer(() -> client.create(unstructured)))
            )
            .retryWhen(Retry.backoff(5, Duration.ofMillis(100))
                .filter(OptimisticLockingFailureException.class::isInstance));
    }

    @Override
    public Mono<Theme> reloadTheme(String name) {
        return client.fetch(Theme.class, name)
            .flatMap(oldTheme -> {
                String settingName = oldTheme.getSpec().getSettingName();
                return waitForSettingDeleted(settingName)
                    .then(waitForAnnotationSettingsDeleted(name));
            })
            .then(Mono.defer(() -> {
                Path themePath = themeRoot.get().resolve(name);
                Path themeManifestPath = ThemeUtils.resolveThemeManifest(themePath);
                if (themeManifestPath == null) {
                    throw new IllegalArgumentException(
                        "The manifest file [theme.yaml] is required.");
                }
                Unstructured unstructured = loadThemeManifest(themeManifestPath);
                Theme newTheme = Unstructured.OBJECT_MAPPER.convertValue(unstructured,
                    Theme.class);
                return client.fetch(Theme.class, name)
                    .map(oldTheme -> {
                        newTheme.getMetadata().setVersion(oldTheme.getMetadata().getVersion());
                        return newTheme;
                    })
                    .flatMap(client::update);
            }))
            .flatMap(theme -> Flux.fromIterable(ThemeUtils.loadThemeResources(getThemePath(theme)))
                .filter(unstructured -> ExtensionWhitelist.of(theme).isAllowed(unstructured))
                .doOnNext(unstructured -> populateThemeNameLabel(unstructured, name))
                .flatMap(this::createOrUpdate)
                .then(Mono.just(theme))
            );
    }

    private static void populateThemeNameLabel(Unstructured unstructured, String themeName) {
        Map<String, String> labels = unstructured.getMetadata().getLabels();
        if (labels == null) {
            labels = new HashMap<>();
            unstructured.getMetadata().setLabels(labels);
        }
        labels.put(Theme.THEME_NAME_LABEL, themeName);
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
                        () -> new RetryException("Re-check if the setting is deleted.")))
                    .retryWhen(Retry.fixedDelay(10, Duration.ofMillis(100))
                        .filter(t -> t instanceof RetryException))
                )
            )
            .then();
    }

    private Mono<Void> waitForAnnotationSettingsDeleted(String themeName) {
        return client.list(AnnotationSetting.class,
                annotationSetting -> {
                    Map<String, String> labels = MetadataUtil.nullSafeLabels(annotationSetting);
                    return StringUtils.equals(themeName, labels.get(Theme.THEME_NAME_LABEL));
                }, null)
            .flatMap(annotationSetting -> client.delete(annotationSetting)
                .flatMap(deleted -> client.fetch(AnnotationSetting.class,
                        annotationSetting.getMetadata().getName())
                    .doOnNext(latest -> {
                        throw new RetryException("AnnotationSetting is not deleted yet.");
                    })
                    .retryWhen(Retry.fixedDelay(10, Duration.ofMillis(100))
                        .filter(t -> t instanceof RetryException))
                )
            )
            .then();
    }

    private Path getThemePath(Theme theme) {
        return themeRoot.get().resolve(theme.getMetadata().getName());
    }

    private Predicate<Unstructured> hasSettingsYaml(Theme theme) {
        return unstructured -> Setting.KIND.equals(unstructured.getKind())
            && theme.getSpec().getSettingName().equals(unstructured.getMetadata().getName());
    }

    private Predicate<Unstructured> hasConfigYaml(Theme theme) {
        return unstructured -> ConfigMap.KIND.equals(unstructured.getKind())
            && theme.getSpec().getConfigMapName().equals(unstructured.getMetadata().getName());
    }

    Mono<Theme> deleteThemeAndWaitForComplete(String themeName) {
        return client.fetch(Theme.class, themeName)
            .flatMap(client::delete)
            .flatMap(deletingTheme -> waitForThemeDeleted(themeName)
                .thenReturn(deletingTheme));
    }

    Mono<Void> waitForThemeDeleted(String themeName) {
        return client.fetch(Theme.class, themeName)
            .doOnNext(theme -> {
                throw new RetryException("Re-check if the theme is deleted successfully");
            })
            .retryWhen(Retry.fixedDelay(20, Duration.ofMillis(100))
                .filter(t -> t instanceof RetryException)
                .onRetryExhaustedThrow((spec, signal) ->
                    new ServerErrorException("Wait timeout for theme deleted", null)))
            .then();
    }

    static class ExtensionWhitelist {
        private final Set<AllowedExtension> rules;

        private ExtensionWhitelist(Theme theme) {
            this.rules = getRules(theme);
        }

        public static ExtensionWhitelist of(Theme theme) {
            return new ExtensionWhitelist(theme);
        }

        public boolean isAllowed(Unstructured unstructured) {
            return this.rules.stream()
                .anyMatch(rule -> rule.matches(unstructured));
        }

        private Set<AllowedExtension> getRules(Theme theme) {
            var rules = new HashSet<AllowedExtension>();
            rules.add(AllowedExtension.of(AnnotationSetting.class));
            rules.add(AllowedExtension.of(NotificationTemplate.class));

            var configMapName = theme.getSpec().getConfigMapName();
            if (StringUtils.isNotBlank(configMapName)) {
                rules.add(AllowedExtension.of(ConfigMap.class, configMapName));
            }

            var settingName = theme.getSpec().getSettingName();
            if (StringUtils.isNotBlank(settingName)) {
                rules.add(AllowedExtension.of(Setting.class, settingName));
            }
            return rules;
        }
    }

    record AllowedExtension(String apiGroup, String kind, String name) {
        AllowedExtension {
            Assert.notNull(apiGroup, "The apiGroup must not be null");
            Assert.notNull(kind, "Kind must not be null");
        }

        public static <E extends Extension> AllowedExtension of(Class<E> clazz) {
            return of(clazz, null);
        }

        public static <E extends Extension> AllowedExtension of(Class<E> clazz, String name) {
            var gvk = GroupVersionKind.fromExtension(clazz);
            return new AllowedExtension(gvk.group(), gvk.kind(), name);
        }

        public boolean matches(Unstructured unstructured) {
            var groupVersionKind = unstructured.groupVersionKind();
            return this.apiGroup.equals(groupVersionKind.group())
                && this.kind.equals(groupVersionKind.kind())
                && (this.name == null || this.name.equals(unstructured.getMetadata().getName()));
        }
    }
}
