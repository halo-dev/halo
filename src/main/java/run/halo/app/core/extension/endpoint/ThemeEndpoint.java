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
import java.io.SequenceInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.fn.builders.schema.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Setting;
import run.halo.app.core.extension.Theme;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.Unstructured;
import run.halo.app.infra.exception.ThemeInstallationException;
import run.halo.app.infra.properties.HaloProperties;
import run.halo.app.infra.utils.FileUtils;
import run.halo.app.infra.utils.YamlUnstructuredLoader;

/**
 * Endpoint for managing themes.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class ThemeEndpoint implements CustomEndpoint {

    private final ExtensionClient client;
    private final HaloProperties haloProperties;

    public ThemeEndpoint(ExtensionClient client, HaloProperties haloProperties) {
        this.client = client;
        this.haloProperties = haloProperties;
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final var tag = "api.halo.run/v1alpha1/Theme";
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
            .DELETE("themes/{name}/uninstall", this::uninstall,
                builder -> builder.operationId("UninstallTheme")
                    .description("Uninstall a theme by name.")
                    .parameter(parameterBuilder()
                        .required(true)
                        .name("name")
                        .description("The name of the theme to uninstall.")
                        .in(ParameterIn.PATH)
                        .implementation(String.class)
                    )
                    .tag(tag)
                    .response(responseBuilder()
                        .implementation(Theme.class))
            )
            .build();
    }

    Mono<ServerResponse> uninstall(ServerRequest request) {
        String name = request.pathVariable("name");
        return client.fetch(Theme.class, name)
            .map(theme -> {
                client.delete(theme);
                return theme;
            })
            .map(theme -> ServerResponse.ok().bodyValue(theme))
            .orElseThrow();
    }

    public record InstallRequest(
        @Schema(required = true, description = "Theme zip file.") FilePart file) {
    }

    Mono<ServerResponse> install(ServerRequest request) {
        return request.bodyToMono(new ParameterizedTypeReference<MultiValueMap<String, Part>>() {
            })
            .flatMap(this::getZipFilePart)
            .flatMap(file -> file.content()
                .map(DataBuffer::asInputStream)
                .reduce(SequenceInputStream::new)
                .map(inputStream -> ThemeUtils.unzipThemeTo(inputStream, getThemeWorkDir())))
            .map(this::persistent)
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
    public Theme persistent(Unstructured themeManifest) {
        Assert.state(StringUtils.equals(Theme.KIND, themeManifest.getKind()),
            "Theme manifest kind must be Theme.");
        client.create(themeManifest);
        Theme theme = client.fetch(Theme.class, themeManifest.getMetadata().getName())
            .orElseThrow();
        List<Unstructured> unstructureds = ThemeUtils.loadThemeResources(getThemePath(theme));
        if (unstructureds.stream()
            .filter(unstructured -> unstructured.getKind().equals(Setting.KIND))
            .filter(unstructured -> unstructured.getMetadata().getName()
                .equals(theme.getSpec().getSettingName()))
            .count() > 1) {
            throw new IllegalStateException(
                "Theme must only have one settings.yaml or settings.yml.");
        }
        if (unstructureds.stream()
            .filter(unstructured -> unstructured.getKind().equals(ConfigMap.KIND))
            .filter(unstructured -> unstructured.getMetadata().getName()
                .equals(theme.getSpec().getConfigMapName()))
            .count() > 1) {
            throw new IllegalStateException(
                "Theme must only have one config.yaml or config.yml.");
        }
        Theme.ThemeSpec spec = theme.getSpec();
        for (Unstructured unstructured : unstructureds) {
            String name = unstructured.getMetadata().getName();

            boolean isThemeSetting = unstructured.getKind().equals(Setting.KIND)
                && StringUtils.equals(spec.getSettingName(), name);

            boolean isThemeConfig = unstructured.getKind().equals(ConfigMap.KIND)
                && StringUtils.equals(spec.getConfigMapName(), name);
            if (isThemeSetting || isThemeConfig) {
                client.create(unstructured);
            }
        }
        return theme;
    }

    private Path getThemePath(Theme theme) {
        return getThemeWorkDir().resolve(theme.getMetadata().getName());
    }

    static class ThemeUtils {
        private static final String THEME_TMP_PREFIX = "halo-theme-";
        private static final String[] themeManifests = {"theme.yaml", "theme.yml"};
        private static final String[] THEME_RESOURCES = {
            "settings.yaml",
            "settings.yml",
            "config.yaml",
            "config.yml"
        };

        static List<Unstructured> loadThemeResources(Path themePath) {
            List<Resource> resources = new ArrayList<>(4);
            for (String themeResource : THEME_RESOURCES) {
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
                    FileSystemUtils.deleteRecursively(tempDirectory);
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

        private static Unstructured loadThemeManifest(Path themeManifestPath) {
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
