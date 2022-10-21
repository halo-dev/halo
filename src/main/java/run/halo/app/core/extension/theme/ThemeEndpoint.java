package run.halo.app.core.extension.theme;

import static java.nio.file.Files.createTempDirectory;
import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;
import static org.springframework.util.FileSystemUtils.copyRecursively;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static reactor.core.scheduler.Schedulers.boundedElastic;
import static run.halo.app.core.extension.theme.ThemeUtils.loadThemeManifest;
import static run.halo.app.core.extension.theme.ThemeUtils.locateThemeManifest;
import static run.halo.app.core.extension.theme.ThemeUtils.unzipThemeTo;
import static run.halo.app.infra.utils.DataBufferUtils.toInputStream;
import static run.halo.app.infra.utils.FileUtils.deleteRecursivelyAndSilently;
import static run.halo.app.infra.utils.FileUtils.unzip;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.zip.ZipInputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.lang.NonNull;
import org.springframework.retry.RetryException;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerErrorException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import run.halo.app.core.extension.Setting;
import run.halo.app.core.extension.Theme;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Unstructured;
import run.halo.app.extension.router.IListRequest;
import run.halo.app.extension.router.QueryParamBuildUtil;
import run.halo.app.infra.exception.ThemeInstallationException;
import run.halo.app.infra.properties.HaloProperties;
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
                            .schema(schemaBuilder()
                                .implementation(InstallRequest.class))
                        ))
                    .response(responseBuilder()
                        .implementation(Theme.class))
            )
            .POST("themes/{name}/upgrade", this::upgrade,
                builder -> builder.operationId("UpgradeTheme")
                    .description("Upgrade theme")
                    .tag(tag)
                    .parameter(parameterBuilder().in(ParameterIn.PATH).name("name").required(true))
                    .requestBody(requestBodyBuilder().required(true)
                        .content(contentBuilder().mediaType(MediaType.MULTIPART_FORM_DATA_VALUE)
                            .schema(schemaBuilder().implementation(UpgradeRequest.class))))
                    .build())
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
            .GET("themes", this::listThemes,
                builder -> {
                    builder.operationId("ListThemes")
                        .description("List themes.")
                        .tag(tag)
                        .response(responseBuilder()
                            .implementation(ListResult.generateGenericClass(Theme.class)));
                    QueryParamBuildUtil.buildParametersFromType(builder, ThemeQuery.class);
                }
            )
            .build();
    }

    public static class ThemeQuery extends IListRequest.QueryListRequest {

        public ThemeQuery(MultiValueMap<String, String> queryParams) {
            super(queryParams);
        }

        @NonNull
        public Boolean getUninstalled() {
            return Boolean.parseBoolean(queryParams.getFirst("uninstalled"));
        }
    }

    Mono<ServerResponse> listThemes(ServerRequest request) {
        MultiValueMap<String, String> queryParams = request.queryParams();
        ThemeQuery query = new ThemeQuery(queryParams);
        return Mono.defer(() -> {
            if (query.getUninstalled()) {
                return listUninstalled(query);
            }
            return client.list(Theme.class, null, null, query.getPage(), query.getSize());
        }).flatMap(extensions -> ServerResponse.ok().bodyValue(extensions));
    }

    public interface IUpgradeRequest {

        @Schema(required = true, description = "Theme zip file.")
        FilePart getFile();

    }

    public static class UpgradeRequest implements IUpgradeRequest {

        private final MultiValueMap<String, Part> multipartData;

        public UpgradeRequest(MultiValueMap<String, Part> multipartData) {
            this.multipartData = multipartData;
        }

        @Override
        public FilePart getFile() {
            var part = multipartData.getFirst("file");
            if (!(part instanceof FilePart filePart)) {
                throw new ServerWebInputException("Invalid multipart type of file");
            }
            if (!filePart.filename().endsWith(".zip")) {
                throw new ServerWebInputException("Only zip extension supported");
            }
            return filePart;
        }

    }

    private Mono<ServerResponse> upgrade(ServerRequest request) {
        var themeNameInPath = request.pathVariable("name");
        final var tempDir = new AtomicReference<Path>();
        final var tempThemeRoot = new AtomicReference<Path>();
        // validate the theme first
        return client.fetch(Theme.class, themeNameInPath)
            .switchIfEmpty(Mono.error(() -> new ServerWebInputException(
                "The given theme with name " + themeNameInPath + " does not exist")))
            .then(request.multipartData())
            .map(UpgradeRequest::new)
            .map(UpgradeRequest::getFile)
            .publishOn(boundedElastic())
            .flatMap(file -> {
                try (var zis = new ZipInputStream(toInputStream(file.content()))) {
                    tempDir.set(createTempDirectory("halo-theme-"));
                    unzip(zis, tempDir.get());
                    return locateThemeManifest(tempDir.get());
                } catch (IOException e) {
                    return Mono.error(Exceptions.propagate(e));
                }
            })
            .switchIfEmpty(Mono.error(() -> new ThemeInstallationException(
                "Missing theme manifest file: theme.yaml or theme.yml")))
            .doOnNext(themeManifest -> {
                if (log.isDebugEnabled()) {
                    log.debug("Found theme manifest file: {}", themeManifest);
                }
                tempThemeRoot.set(themeManifest.getParent());
            })
            .map(ThemeUtils::loadThemeManifest)
            .doOnNext(newTheme -> {
                if (!Objects.equals(themeNameInPath, newTheme.getMetadata().getName())) {
                    if (log.isDebugEnabled()) {
                        log.error("Want theme name: {}, but provided: {}", themeNameInPath,
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
                var themePath = getThemeWorkDir().resolve(newTheme.getMetadata().getName());
                try {
                    copyRecursively(tempThemeRoot.get(), themePath);
                } catch (IOException e) {
                    throw Exceptions.propagate(e);
                }
            })
            .flatMap(this::persistent)
            .flatMap(updatedTheme -> ServerResponse.ok()
                .bodyValue(updatedTheme))
            .doFinally(signalType -> {
                // clear the temporary folder
                deleteRecursivelyAndSilently(tempDir.get());
            });
    }

    Mono<ListResult<Theme>> listUninstalled(ThemeQuery query) {
        Path path = themePathPolicy.themesDir();
        return ThemeUtils.listAllThemesFromThemeDir(path)
            .collectList()
            .flatMap(this::filterUnInstalledThemes)
            .map(themes -> {
                Integer page = query.getPage();
                Integer size = query.getSize();
                List<Theme> subList = ListResult.subList(themes, page, size);
                return new ListResult<>(page, size, themes.size(), subList);
            });
    }

    private Mono<List<Theme>> filterUnInstalledThemes(@NonNull List<Theme> allThemes) {
        return client.list(Theme.class, null, null)
            .map(theme -> theme.getMetadata().getName())
            .collectList()
            .map(installed -> allThemes.stream()
                .filter(theme -> !installed.contains(theme.getMetadata().getName()))
                .toList()
            );
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
                Unstructured unstructured = loadThemeManifest(themeManifestPath);
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
            .flatMap(file -> {
                try {
                    var is = toInputStream(file.content());
                    var themeWorkDir = getThemeWorkDir();
                    if (log.isDebugEnabled()) {
                        log.debug("Transferring {} into {}", file.filename(), themeWorkDir);
                    }
                    return unzipThemeTo(is, themeWorkDir);
                } catch (IOException e) {
                    return Mono.error(Exceptions.propagate(e));
                }
            })
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
