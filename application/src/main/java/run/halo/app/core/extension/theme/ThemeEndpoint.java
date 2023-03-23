package run.halo.app.core.extension.theme;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static run.halo.app.infra.utils.DataBufferUtils.toInputStream;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import run.halo.app.core.extension.Setting;
import run.halo.app.core.extension.Theme;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.router.IListRequest;
import run.halo.app.extension.router.QueryParamBuildUtil;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.ThemeRootGetter;
import run.halo.app.infra.exception.NotFoundException;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.theme.TemplateEngineManager;

/**
 * Endpoint for managing themes.
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
@Component
@AllArgsConstructor
public class ThemeEndpoint implements CustomEndpoint {

    private final ReactiveExtensionClient client;

    private final ThemeRootGetter themeRoot;

    private final ThemeService themeService;

    private final TemplateEngineManager templateEngineManager;

    private final SystemConfigurableEnvironmentFetcher systemEnvironmentFetcher;

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
            .PUT("themes/{name}/reload", this::reloadTheme,
                builder -> builder.operationId("Reload")
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
            .PUT("themes/{name}/reset-config", this::resetSettingConfig,
                builder -> builder.operationId("ResetThemeConfig")
                    .description("Reset the configMap of theme setting.")
                    .tag(tag)
                    .parameter(parameterBuilder()
                        .name("name")
                        .in(ParameterIn.PATH)
                        .required(true)
                        .implementation(String.class)
                    )
                    .response(responseBuilder()
                        .implementation(ConfigMap.class))
            )
            .PUT("themes/{name}/config", this::updateThemeConfig,
                builder -> builder.operationId("updateThemeConfig")
                    .description("Update the configMap of theme setting.")
                    .tag(tag)
                    .parameter(parameterBuilder()
                        .name("name")
                        .in(ParameterIn.PATH)
                        .required(true)
                        .implementation(String.class)
                    )
                    .requestBody(requestBodyBuilder()
                        .required(true)
                        .content(contentBuilder().mediaType(MediaType.APPLICATION_JSON_VALUE)
                            .schema(schemaBuilder().implementation(ConfigMap.class))))
                    .response(responseBuilder()
                        .implementation(ConfigMap.class))
            )
            .PUT("themes/{name}/activation", this::activateTheme,
                builder -> builder.operationId("activateTheme")
                    .description("Activate a theme by name.")
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
            .GET("themes/-/activation", this::fetchActivatedTheme,
                builder -> builder.operationId("fetchActivatedTheme")
                    .description("Fetch the activated theme.")
                    .tag(tag)
                    .response(responseBuilder()
                        .implementation(Theme.class))
            )
            .GET("themes/{name}/setting", this::fetchThemeSetting,
                builder -> builder.operationId("fetchThemeSetting")
                    .description("Fetch setting of theme.")
                    .tag(tag)
                    .parameter(parameterBuilder()
                        .name("name")
                        .in(ParameterIn.PATH)
                        .required(true)
                        .implementation(String.class)
                    )
                    .response(responseBuilder()
                        .implementation(Setting.class))
            )
            .GET("themes/{name}/config", this::fetchThemeConfig,
                builder -> builder.operationId("fetchThemeConfig")
                    .description("Fetch configMap of theme by configured configMapName.")
                    .tag(tag)
                    .parameter(parameterBuilder()
                        .name("name")
                        .in(ParameterIn.PATH)
                        .required(true)
                        .implementation(String.class)
                    )
                    .response(responseBuilder()
                        .implementation(ConfigMap.class))
            )
            .build();
    }

    private Mono<ServerResponse> activateTheme(ServerRequest request) {
        final var activatedThemeName = request.pathVariable("name");
        return client.fetch(Theme.class, activatedThemeName)
            .switchIfEmpty(Mono.error(new NotFoundException("Theme not found.")))
            .flatMap(theme -> systemEnvironmentFetcher.fetch(SystemSetting.Theme.GROUP,
                    SystemSetting.Theme.class)
                .flatMap(themeSetting -> {
                    // update active theme config
                    themeSetting.setActive(activatedThemeName);
                    return systemEnvironmentFetcher.getConfigMap()
                        .filter(configMap -> configMap.getData() != null)
                        .map(configMap -> {
                            var themeConfigJson = JsonUtils.objectToJson(themeSetting);
                            configMap.getData()
                                .put(SystemSetting.Theme.GROUP, themeConfigJson);
                            return configMap;
                        });
                })
                .flatMap(client::update)
                .retryWhen(Retry.backoff(5, Duration.ofMillis(300))
                    .filter(OptimisticLockingFailureException.class::isInstance)
                )
                .thenReturn(theme)
            )
            .flatMap(activatedTheme -> ServerResponse.ok().bodyValue(activatedTheme));
    }

    private Mono<ServerResponse> updateThemeConfig(ServerRequest request) {
        final var themeName = request.pathVariable("name");
        return client.fetch(Theme.class, themeName)
            .doOnNext(theme -> {
                String configMapName = theme.getSpec().getConfigMapName();
                if (StringUtils.isBlank(configMapName)) {
                    throw new ServerWebInputException(
                        "Unable to complete the request because the theme configMapName is blank.");
                }
            })
            .flatMap(theme -> {
                final var configMapName = theme.getSpec().getConfigMapName();
                return request.bodyToMono(ConfigMap.class)
                    .doOnNext(configMapToUpdate -> {
                        var configMapNameToUpdate = configMapToUpdate.getMetadata().getName();
                        if (!configMapName.equals(configMapNameToUpdate)) {
                            throw new ServerWebInputException(
                                "The name from the request body does not match the theme "
                                    + "configMapName name.");
                        }
                    })
                    .flatMap(configMapToUpdate -> client.fetch(ConfigMap.class, configMapName)
                        .map(persisted -> {
                            configMapToUpdate.getMetadata()
                                .setVersion(persisted.getMetadata().getVersion());
                            return configMapToUpdate;
                        })
                        .switchIfEmpty(client.create(configMapToUpdate))
                    )
                    .flatMap(client::update)
                    .retryWhen(Retry.backoff(5, Duration.ofMillis(300))
                        .filter(OptimisticLockingFailureException.class::isInstance)
                    );
            })
            .flatMap(configMap -> ServerResponse.ok().bodyValue(configMap));
    }

    private Mono<ServerResponse> fetchThemeConfig(ServerRequest request) {
        return themeNameInPathVariableOrActivated(request)
            .flatMap(themeName -> client.fetch(Theme.class, themeName))
            .mapNotNull(theme -> theme.getSpec().getConfigMapName())
            .flatMap(configMapName -> client.fetch(ConfigMap.class, configMapName))
            .flatMap(configMap -> ServerResponse.ok().bodyValue(configMap));
    }

    private Mono<ServerResponse> fetchActivatedTheme(ServerRequest request) {
        return systemEnvironmentFetcher.fetch(SystemSetting.Theme.GROUP, SystemSetting.Theme.class)
            .map(SystemSetting.Theme::getActive)
            .flatMap(activatedName -> client.fetch(Theme.class, activatedName))
            .flatMap(theme -> ServerResponse.ok().bodyValue(theme));
    }

    private Mono<ServerResponse> fetchThemeSetting(ServerRequest request) {
        return themeNameInPathVariableOrActivated(request)
            .flatMap(name -> client.fetch(Theme.class, name))
            .mapNotNull(theme -> theme.getSpec().getSettingName())
            .flatMap(settingName -> client.fetch(Setting.class, settingName))
            .flatMap(setting -> ServerResponse.ok().bodyValue(setting));
    }

    private Mono<String> themeNameInPathVariableOrActivated(ServerRequest request) {
        Assert.notNull(request, "request must not be null.");
        return Mono.fromSupplier(() -> request.pathVariable("name"))
            .flatMap(name -> {
                if ("-".equals(name)) {
                    return systemEnvironmentFetcher.fetch(SystemSetting.Theme.GROUP,
                            SystemSetting.Theme.class)
                        .mapNotNull(SystemSetting.Theme::getActive)
                        .defaultIfEmpty(name);
                }
                return Mono.just(name);
            });
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

    // TODO Extract the method into ThemeService
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
        // validate the theme first
        var themeNameInPath = request.pathVariable("name");
        return request.multipartData()
            .map(UpgradeRequest::new)
            .map(UpgradeRequest::getFile)
            .flatMap(file -> {
                try {
                    return themeService.upgrade(themeNameInPath, toInputStream(file.content()));
                } catch (IOException e) {
                    return Mono.error(e);
                }
            })
            .flatMap((updatedTheme) -> templateEngineManager.clearCache(
                    updatedTheme.getMetadata().getName())
                .thenReturn(updatedTheme))
            .flatMap(updatedTheme -> ServerResponse.ok()
                .bodyValue(updatedTheme));
    }

    Mono<ListResult<Theme>> listUninstalled(ThemeQuery query) {
        Path path = themeRoot.get();
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

    Mono<ServerResponse> reloadTheme(ServerRequest request) {
        String name = request.pathVariable("name");
        return themeService.reloadTheme(name)
            .flatMap(theme -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(theme));
    }

    Mono<ServerResponse> resetSettingConfig(ServerRequest request) {
        String name = request.pathVariable("name");
        return themeService.resetSettingConfig(name)
            .flatMap(theme -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(theme));
    }

    @Schema(name = "ThemeInstallRequest")
    public record InstallRequest(
        @Schema(required = true, description = "Theme zip file.") FilePart file) {
    }

    Mono<ServerResponse> install(ServerRequest request) {
        return request.body(BodyExtractors.toMultipartData())
            .flatMap(this::getZipFilePart)
            .flatMap(file -> {
                try {
                    return themeService.install(toInputStream(file.content()));
                } catch (IOException e) {
                    return Mono.error(Exceptions.propagate(e));
                }
            })
            .flatMap(theme -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(theme));
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
