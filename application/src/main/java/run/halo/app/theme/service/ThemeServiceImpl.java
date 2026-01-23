package run.halo.app.theme.service;

import static org.springframework.util.FileSystemUtils.copyRecursively;
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
import org.jspecify.annotations.Nullable;
import org.reactivestreams.Publisher;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.dao.OptimisticLockingFailureException;
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

@Slf4j
@Service
@AllArgsConstructor
public class ThemeServiceImpl implements ThemeService {

    private final ReactiveExtensionClient client;

    private final ThemeRootGetter themeRoot;

    private final HaloProperties haloProperties;

    private final SystemVersionSupplier systemVersionSupplier;

    private final SystemConfigFetcher systemConfigFetcher;

    private final Scheduler scheduler = Schedulers.boundedElastic();

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
                }).flatMap(themePath -> {
                    var content = DataBufferUtils.read(new FileSystemResource(themePath),
                        DefaultDataBufferFactory.sharedInstance,
                        StreamUtils.BUFFER_SIZE);
                    // We don't want to run on the bounded elastic scheduler again, so pass null
                    // here.
                    return doInstall(content, null);
                }),
                FileUtils::deleteRecursivelyAndSilently
            )
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
            .switchIfEmpty(Mono.error(() -> new ServerWebInputException(
                "The given theme with name " + themeName + " did not exist")
            ));
        var upgradeTheme = Mono.using(
            () -> Files.createTempDirectory("halo-theme-"),
            tempDir -> {
                var locateThemeManifest = Mono.fromCallable(
                        () -> locateThemeManifest(tempDir).orElse(null)
                    )
                    .switchIfEmpty(Mono.error(() -> new ThemeUpgradeException(
                        "Missing theme manifest file: theme.yaml or theme.yml",
                        "problemDetail.theme.upgrade.missingManifest", null)
                    ));
                return unzip(content, tempDir)
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
                            // TODO Create backup before deleting
                            deleteRecursivelyAndSilently(themePath);
                            copyRecursively(themeManifest.getParent(), themePath);
                            return themePath;
                        });
                        return copyTheme.then(this.persistent(newTheme, true));
                    });
            },
            FileUtils::deleteRecursivelyAndSilently
        ).subscribeOn(scheduler);
        return checkTheme.then(upgradeTheme);
    }

    private Mono<Theme> doInstall(Publisher<DataBuffer> content, @Nullable Scheduler scheduler) {
        var themeRoot = this.themeRoot.get();
        return unzipThemeTo(content, themeRoot, scheduler)
            .flatMap(theme -> persistent(theme, false));
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
                String systemVersion = systemVersionSupplier.get().toStableVersion().toString();
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
                    .then()
                    .thenReturn(theme);
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

    @Override
    public Mono<Theme> fetchActivatedTheme() {
        return fetchSystemSetting().mapNotNull(SystemSetting.Theme::getActive)
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
                        .filter(IllegalStateException.class::isInstance)
                    )
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
                    .flatMap(latest -> Mono.error(
                        new IllegalStateException("AnnotationSetting is not deleted yet.")
                    ))
                    .retryWhen(Retry.fixedDelay(10, Duration.ofMillis(100))
                        .filter(IllegalStateException.class::isInstance)
                    )
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
            .flatMap(theme -> {
                return Mono.error(new IllegalStateException(
                    "Re-check if the theme is deleted successfully"
                ));
            })
            .retryWhen(Retry.fixedDelay(20, Duration.ofMillis(100))
                .filter(IllegalStateException.class::isInstance)
                .onRetryExhaustedThrow((spec, signal) ->
                    new ServerErrorException("Wait timeout for theme deleted", null)
                )
            )
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
        // merge annotations
        if (updatingMeta.getAnnotations() != null) {
            if (existingMeta.getAnnotations() == null) {
                existingMeta.setAnnotations(new HashMap<>());
            }
            existingMeta.getAnnotations().putAll(updatingMeta.getAnnotations());
        }
    }

}
