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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Theme;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.router.IListRequest;
import run.halo.app.extension.router.QueryParamBuildUtil;
import run.halo.app.infra.ThemeRootGetter;

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

    private final ThemeRootGetter themeRoot;

    private final ThemeService themeService;

    public ThemeEndpoint(ReactiveExtensionClient client, ThemeRootGetter themeRoot,
        ThemeService themeService) {
        this.client = client;
        this.themeRoot = themeRoot;
        this.themeService = themeService;
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
            .PUT("themes/{name}/reload", this::reloadTheme,
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

    private Path getThemePath(Theme theme) {
        return getThemeWorkDir().resolve(theme.getMetadata().getName());
    }

    private Path getThemeWorkDir() {
        Path themePath = themeRoot.get();
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
