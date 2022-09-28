package run.halo.app.core.extension.endpoint;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.fn.builders.schema.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.core.extension.Setting;
import run.halo.app.core.extension.Theme;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Unstructured;
import run.halo.app.infra.exception.ThemeInstallationException;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.infra.utils.DataBufferUtils;
import run.halo.app.infra.utils.FileUtils;
import run.halo.app.infra.utils.YamlUnstructuredLoader;
import run.halo.app.theme.ThemePathPolicy;

/**
 * Endpoint for managing themes.
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
@Component
public class ThemeEndpoint implements CustomEndpoint {

    private final ReactiveExtensionClient client;
    private final HaloProperties haloProperties;
    private final ThemePathPolicy themePathPolicy;

    public ThemeEndpoint(ReactiveExtensionClient client, HaloProperties haloProperties) {
        this.client = client;
        this.haloProperties = haloProperties;
        this.themePathPolicy = new ThemePathPolicy(haloProperties.getWorkDir());
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final var tag = "api.console.halo.run/v1alpha1/Theme";
        return SpringdocRouteBuilder.route()
            .POST("themes/install", contentType(MediaType.MULTIPART_FORM_DATA),
                this::install, builder -> builder.operationId("InstallTheme")
                    .description("Install a theme by uploading a zip file.")
                    .tag(tag)
                    .requestBody(requestBodyBuilder()
                        .required(true)
                        .content(contentBuilder()
                            .mediaType(MediaType.MULTIPART_FORM_DATA_VALUE)
                            .schema(Builder.schemaBuilder()
                                .implementation(InstallRequest.class))
                        ))
                    .response(responseBuilder()
                        .implementation(Theme.class))
            )
            .PUT("themes/{name}/reload-setting", this::reloadSetting,
                builder -> builder.operationId("ReloadThemeSetting")
                    .description("Reload theme setting.")
                    .tag(tag)
                    .parameter(parameterBuilder()
                        .name("name")
                        .in(ParameterIn.PATH)
                        .required(true)
                        .implementation(String.class)
                    )
                    .response(responseBuilder()
                        .implementation(Theme.class))
            )
            .build();
    }

    Mono<ServerResponse> reloadSetting(ServerRequest request) {
        String name = request.pathVariable("name");
        return client.fetch(Theme.class, name)
            .filter(theme -> StringUtils.isNotBlank(theme.getSpec().getSettingName()))
            .flatMap(theme -> {
                String settingName = theme.getSpec().getSettingName();
                return ThemeUtils.loadThemeSetting(getThemePath(theme))
                    .stream()
                    .filter(unstructured ->
                        settingName.equals(unstructured.getMetadata().getName()))
                    .findFirst()
                    .map(setting -> Unstructured.OBJECT_MAPPER.convertValue(setting, Setting.class))
                    .map(setting -> client.fetch(Setting.class, settingName)
                        .flatMap(persistent -> {
                            // update spec to persisted setting
                            persistent.setSpec(setting.getSpec());
                            return client.update(persistent);
                        })
                        .switchIfEmpty(Mono.defer(() -> client.create(setting)))
                        .thenReturn(theme)
                    )
                    .orElse(Mono.just(theme));
            })
            .flatMap(themeToUse -> {
                Path themePath = themePathPolicy.generate(themeToUse);
                Path themeManifestPath = ThemeUtils.resolveThemeManifest(themePath);
                if (themeManifestPath == null) {
                    return Mono.error(new IllegalArgumentException(
                        "The manifest file [theme.yaml] is required."));
                }
                Unstructured unstructured = ThemeUtils.loadThemeManifest(themeManifestPath);
                Theme newTheme = Unstructured.OBJECT_MAPPER.convertValue(unstructured, Theme.class);
                themeToUse.setSpec(newTheme.getSpec());
                return client.update(themeToUse);
            })
            .flatMap(theme -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(theme));
    }

    public record InstallRequest(
        @Schema(required = true, description = "Theme zip file.") FilePart file) {
    }

    Mono<ServerResponse> install(ServerRequest request) {
        return request.body(BodyExtractors.toMultipartData())
            .flatMap(this::getZipFilePart)
            .map(file -> {
                try {
                    var is = DataBufferUtils.toInputStream(file.content());
                    var themeWorkDir = getThemeWorkDir();
                    if (log.isDebugEnabled()) {
                        log.debug("Transferring {} into {}", file.filename(), themeWorkDir);
                    }
                    return ThemeUtils.unzipThemeTo(is, themeWorkDir);
                } catch (IOException e) {
                    throw Exceptions.propagate(e);
                }
            })
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap(this::persistent)
            .flatMap(theme -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(theme));
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
        return getThemeWorkDir().resolve(theme.getMetadata().getName());
    }

    private Predicate<Unstructured> hasSettingsYaml(Theme theme) {
        return unstructured -> Setting.KIND.equals(unstructured.getKind())
            && theme.getSpec().getSettingName().equals(unstructured.getMetadata().getName());
    }

    private Predicate<Unstructured> hasConfigYaml(Theme theme) {
        return unstructured -> ConfigMap.KIND.equals(unstructured.getKind())
            && theme.getSpec().getConfigMapName().equals(unstructured.getMetadata().getName());
    }

    static class ThemeUtils {
        private static final String THEME_TMP_PREFIX = "halo-theme-";
        private static final String[] themeManifests = {"theme.yaml", "theme.yml"};

        private static final String[] THEME_CONFIG = {"config.yaml", "config.yml"};

        private static final String[] THEME_SETTING = {"settings.yaml", "settings.yml"};

        static List<Unstructured> loadThemeSetting(Path themePath) {
            return loadUnstructured(themePath, THEME_SETTING);
        }

        private static List<Unstructured> loadUnstructured(Path themePath,
            String[] themeSetting) {
            List<Resource> resources = new ArrayList<>(4);
            for (String themeResource : themeSetting) {
                Path resourcePath = themePath.resolve(themeResource);
                if (Files.exists(resourcePath)) {
                    resources.add(new FileSystemResource(resourcePath));
                }
            }
            if (CollectionUtils.isEmpty(resources)) {
                return List.of();
            }
            return new YamlUnstructuredLoader(resources.toArray(new Resource[0]))
                .load();
        }

        static List<Unstructured> loadThemeResources(Path themePath) {
            String[] resourceNames = ArrayUtils.addAll(THEME_SETTING, THEME_CONFIG);
            return loadUnstructured(themePath, resourceNames);
        }

        static Unstructured unzipThemeTo(InputStream inputStream, Path themeWorkDir) {
            return unzipThemeTo(inputStream, themeWorkDir, false);
        }

        static Unstructured unzipThemeTo(InputStream inputStream, Path themeWorkDir,
            boolean override) {

            Path tempDirectory = null;
            try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
                tempDirectory = Files.createTempDirectory(THEME_TMP_PREFIX);

                ZipEntry firstEntry = zipInputStream.getNextEntry();
                if (firstEntry == null) {
                    throw new IllegalArgumentException("Theme zip file is empty.");
                }

                Path themeTempWorkDir = tempDirectory.resolve(firstEntry.getName());
                FileUtils.unzip(zipInputStream, tempDirectory);

                Path themeManifestPath = resolveThemeManifest(themeTempWorkDir);
                if (themeManifestPath == null) {
                    throw new IllegalArgumentException(
                        "It's an invalid zip format for the theme, manifest "
                            + "file [theme.yaml] is required.");
                }
                Unstructured unstructured = loadThemeManifest(themeManifestPath);
                String themeName = unstructured.getMetadata().getName();
                Path themeTargetPath = themeWorkDir.resolve(themeName);
                if (!override && !FileUtils.isEmpty(themeTargetPath)) {
                    throw new ThemeInstallationException("Theme already exists.");
                }
                // install theme to theme work dir
                FileSystemUtils.copyRecursively(themeTempWorkDir, themeTargetPath);
                return unstructured;
            } catch (IOException e) {
                throw new ThemeInstallationException("Unable to install theme", e);
            } finally {
                // clean temp directory
                try {
                    // null safe
                    FileSystemUtils.deleteRecursively(tempDirectory);
                } catch (IOException e) {
                    // ignore this exception
                }
            }
        }

        static Unstructured loadThemeManifest(Path themeManifestPath) {
            List<Unstructured> unstructureds =
                new YamlUnstructuredLoader(new FileSystemResource(themeManifestPath))
                    .load();
            if (CollectionUtils.isEmpty(unstructureds)) {
                throw new IllegalArgumentException(
                    "The [theme.yaml] does not conform to the theme specification.");
            }
            return unstructureds.get(0);
        }

        @Nullable
        private static Path resolveThemeManifest(Path tempDirectory) {
            for (String themeManifest : themeManifests) {
                Path path = tempDirectory.resolve(themeManifest);
                if (Files.exists(path)) {
                    return path;
                }
            }
            return null;
        }
    }

    private Path getThemeWorkDir() {
        Path themePath = haloProperties.getWorkDir()
            .resolve("themes");
        if (Files.notExists(themePath)) {
            try {
                Files.createDirectories(themePath);
            } catch (IOException e) {
                throw new UnsupportedOperationException(
                    "Failed to create directory " + themePath, e);
            }
        }
        return themePath;
    }

    Mono<FilePart> getZipFilePart(MultiValueMap<String, Part> formData) {
        Part part = formData.getFirst("file");
        if (!(part instanceof FilePart file)) {
            return Mono.error(new ServerWebInputException(
                "Invalid parameter of file, binary data is required"));
        }
        if (!Paths.get(file.filename()).toString().endsWith(".zip")) {
            return Mono.error(new ServerWebInputException(
                "Invalid file type, only zip format is supported"));
        }
        return Mono.just(file);
    }
}
