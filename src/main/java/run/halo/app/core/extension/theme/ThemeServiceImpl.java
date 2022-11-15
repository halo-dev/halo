package run.halo.app.core.extension.theme;

import static java.nio.file.Files.createTempDirectory;
import static org.springframework.util.FileSystemUtils.copyRecursively;
import static run.halo.app.core.extension.theme.ThemeUtils.locateThemeManifest;
import static run.halo.app.infra.utils.FileUtils.deleteRecursivelyAndSilently;
import static run.halo.app.infra.utils.FileUtils.unzip;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.zip.ZipInputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import run.halo.app.core.extension.Setting;
import run.halo.app.core.extension.Theme;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Unstructured;
import run.halo.app.infra.ThemeRootGetter;
import run.halo.app.infra.exception.ThemeInstallationException;

@Slf4j
@Service
public class ThemeServiceImpl implements ThemeService {

    private final ReactiveExtensionClient client;

    private final ThemeRootGetter themeRoot;

    public ThemeServiceImpl(ReactiveExtensionClient client, ThemeRootGetter themeRoot) {
        this.client = client;
        this.themeRoot = themeRoot;
    }

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
                    return locateThemeManifest(tempDir.get())
                        .switchIfEmpty(Mono.error(() -> new ThemeInstallationException(
                            "Missing theme manifest file: theme.yaml or theme.yml")));
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
                    throw new ServerWebInputException("please make sure the theme name is correct");
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
                    .flatMap(unstructured -> {
                        var spec = theme.getSpec();
                        String name = unstructured.getMetadata().getName();

                        boolean isThemeSetting = unstructured.getKind().equals(Setting.KIND)
                            && StringUtils.equals(spec.getSettingName(), name);

                        boolean isThemeConfig = unstructured.getKind().equals(ConfigMap.KIND)
                            && StringUtils.equals(spec.getConfigMapName(), name);
                        if (isThemeSetting || isThemeConfig) {
                            return client.create(unstructured);
                        }
                        return Mono.empty();
                    })
                    .then(Mono.just(theme));
            });
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
                .filter(t -> t instanceof RetryException))
            .onErrorMap(Exceptions::isRetryExhausted,
                throwable -> new ServerErrorException("Wait timeout for theme deleted", throwable))
            .then();
    }
}
