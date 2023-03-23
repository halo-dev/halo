package run.halo.app.core.extension.theme;

import static java.nio.file.Files.createTempDirectory;
import static org.springframework.util.FileSystemUtils.copyRecursively;
import static run.halo.app.core.extension.theme.ThemeUtils.loadThemeManifest;
import static run.halo.app.core.extension.theme.ThemeUtils.locateThemeManifest;
import static run.halo.app.infra.utils.FileUtils.deleteRecursivelyAndSilently;
import static run.halo.app.infra.utils.FileUtils.unzip;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.zip.ZipInputStream;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.RetryException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerErrorException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;
import run.halo.app.core.extension.AnnotationSetting;
import run.halo.app.core.extension.Setting;
import run.halo.app.core.extension.Theme;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Unstructured;
import run.halo.app.infra.SystemVersionSupplier;
import run.halo.app.infra.ThemeRootGetter;
import run.halo.app.infra.exception.ThemeUpgradeException;
import run.halo.app.infra.exception.UnsatisfiedAttributeValueException;
import run.halo.app.infra.utils.VersionUtils;

@Slf4j
@Service
@AllArgsConstructor
public class ThemeServiceImpl implements ThemeService {

    private final ReactiveExtensionClient client;

    private final ThemeRootGetter themeRoot;

    private final SystemVersionSupplier systemVersionSupplier;

    @Override
    public Mono<Theme> install(InputStream is) {
        var themeRoot = this.themeRoot.get();
        return ThemeUtils.unzipThemeTo(is, themeRoot)
            .flatMap(this::persistent);
    }

    @Override
    public Mono<Theme> upgrade(String themeName, InputStream is) {
        var tempDir = new AtomicReference<Path>();
        var tempThemeRoot = new AtomicReference<Path>();
        return client.fetch(Theme.class, themeName)
            .switchIfEmpty(Mono.error(() -> new ServerWebInputException(
                "The given theme with name " + themeName + " did not exist")))
            .publishOn(Schedulers.boundedElastic())
            .doFirst(() -> {
                try {
                    tempDir.set(createTempDirectory("halo-theme-"));
                } catch (IOException e) {
                    throw Exceptions.propagate(e);
                }
            })
            .flatMap(oldTheme -> {
                try (var zis = new ZipInputStream(is)) {
                    unzip(zis, tempDir.get());
                    return locateThemeManifest(tempDir.get()).switchIfEmpty(Mono.error(
                        () -> new ThemeUpgradeException(
                            "Missing theme manifest file: theme.yaml or theme.yml",
                            "problemDetail.theme.upgrade.missingManifest", null)));
                } catch (IOException e) {
                    return Mono.error(e);
                }
            })
            .doOnNext(themeManifest -> {
                if (log.isDebugEnabled()) {
                    log.debug("Found theme manifest file: {}", themeManifest);
                }
                tempThemeRoot.set(themeManifest.getParent());
            })
            .map(ThemeUtils::loadThemeManifest)
            .doOnNext(newTheme -> {
                if (!Objects.equals(themeName, newTheme.getMetadata().getName())) {
                    if (log.isDebugEnabled()) {
                        log.error("Want theme name: {}, but provided: {}", themeName,
                            newTheme.getMetadata().getName());
                    }
                    throw new ThemeUpgradeException("Please make sure the theme name is correct",
                        "problemDetail.theme.upgrade.nameMismatch",
                        new Object[] {newTheme.getMetadata().getName(), themeName});
                }
            })
            .flatMap(newTheme -> {
                // Remove the theme before upgrading
                return deleteThemeAndWaitForComplete(newTheme.getMetadata().getName())
                    .thenReturn(newTheme);
            })
            .doOnNext(newTheme -> {
                // prepare the theme
                var themePath = themeRoot.get().resolve(newTheme.getMetadata().getName());
                try {
                    copyRecursively(tempThemeRoot.get(), themePath);
                } catch (IOException e) {
                    throw Exceptions.propagate(e);
                }
            })
            .flatMap(this::persistent)
            .doFinally(signalType -> {
                // clear the temporary folder
                deleteRecursivelyAndSilently(tempDir.get());
            });
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
    public Mono<Theme> persistent(Unstructured themeManifest) {
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
                var spec = theme.getSpec();
                return Flux.fromIterable(unstructureds)
                    .filter(unstructured -> {
                        String name = unstructured.getMetadata().getName();
                        boolean isThemeSetting = unstructured.getKind().equals(Setting.KIND)
                            && StringUtils.equals(spec.getSettingName(), name);

                        boolean isThemeConfig = unstructured.getKind().equals(ConfigMap.KIND)
                            && StringUtils.equals(spec.getConfigMapName(), name);

                        boolean isAnnotationSetting = unstructured.getKind()
                            .equals(AnnotationSetting.KIND);
                        return isThemeSetting || isThemeConfig || isAnnotationSetting;
                    })
                    .doOnNext(unstructured ->
                        populateThemeNameLabel(unstructured, theme.getMetadata().getName()))
                    .flatMap(unstructured -> client.create(unstructured)
                        .retryWhen(Retry.backoff(5, Duration.ofMillis(100))
                            .filter(OptimisticLockingFailureException.class::isInstance))
                    )
                    .then(Mono.just(theme));
            });
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
            .flatMap(theme -> {
                String settingName = theme.getSpec().getSettingName();
                return Flux.fromIterable(ThemeUtils.loadThemeResources(getThemePath(theme)))
                    .filter(unstructured -> (Setting.KIND.equals(unstructured.getKind())
                        && unstructured.getMetadata().getName().equals(settingName))
                        || AnnotationSetting.KIND.equals(unstructured.getKind())
                    )
                    .doOnNext(unstructured -> populateThemeNameLabel(unstructured, name))
                    .flatMap(client::create)
                    .then(Mono.just(theme));
            });
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
}
