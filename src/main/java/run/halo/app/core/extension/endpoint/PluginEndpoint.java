package run.halo.app.core.extension.endpoint;

import static java.nio.file.Files.copy;
import static java.util.Comparator.comparing;
import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;
import static org.springframework.boot.convert.ApplicationConversionService.getSharedInstance;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static run.halo.app.extension.ListResult.generateGenericClass;
import static run.halo.app.extension.router.QueryParamBuildUtil.buildParametersFromType;
import static run.halo.app.extension.router.selector.SelectorUtil.labelAndFieldSelectorToPredicate;
import static run.halo.app.infra.utils.FileUtils.deleteRecursivelyAndSilently;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.retry.RetryException;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerErrorException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;
import run.halo.app.core.extension.Plugin;
import run.halo.app.core.extension.Setting;
import run.halo.app.core.extension.theme.SettingUtils;
import run.halo.app.extension.Comparators;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.router.IListRequest.QueryListRequest;
import run.halo.app.infra.utils.FileUtils;
import run.halo.app.plugin.PluginProperties;
import run.halo.app.plugin.YamlPluginFinder;

@Slf4j
@Component
@AllArgsConstructor
public class PluginEndpoint implements CustomEndpoint {

    private final PluginProperties pluginProperties;

    private final ReactiveExtensionClient client;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final var tag = "api.console.halo.run/v1alpha1/Plugin";
        return SpringdocRouteBuilder.route()
            .POST("plugins/install", contentType(MediaType.MULTIPART_FORM_DATA),
                this::install, builder -> builder.operationId("InstallPlugin")
                    .description("Install a plugin by uploading a Jar file.")
                    .tag(tag)
                    .requestBody(requestBodyBuilder()
                        .required(true)
                        .content(contentBuilder()
                            .mediaType(MediaType.MULTIPART_FORM_DATA_VALUE)
                            .schema(schemaBuilder().implementation(InstallRequest.class))
                        ))
                    .response(responseBuilder().implementation(Plugin.class))
            )
            .POST("plugins/{name}/upgrade", contentType(MediaType.MULTIPART_FORM_DATA),
                this::upgrade, builder -> builder.operationId("UpgradePlugin")
                    .description("Upgrade a plugin by uploading a Jar file")
                    .tag(tag)
                    .parameter(parameterBuilder().name("name").in(ParameterIn.PATH).required(true))
                    .requestBody(requestBodyBuilder()
                        .required(true)
                        .content(contentBuilder().mediaType(MediaType.MULTIPART_FORM_DATA_VALUE)
                            .schema(schemaBuilder().implementation(InstallRequest.class))))
            )
            .PUT("plugins/{name}/config", this::updatePluginConfig,
                builder -> builder.operationId("updatePluginConfig")
                    .description("Update the configMap of plugin setting.")
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
            .PUT("plugins/{name}/reset-config", this::resetSettingConfig,
                builder -> builder.operationId("ResetPluginConfig")
                    .description("Reset the configMap of plugin setting.")
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
            .GET("plugins", this::list, builder -> {
                builder.operationId("ListPlugins")
                    .tag(tag)
                    .description("List plugins using query criteria and sort params")
                    .response(responseBuilder().implementation(generateGenericClass(Plugin.class)));
                buildParametersFromType(builder, ListRequest.class);
            })
            .GET("plugins/{name}/setting", this::fetchPluginSetting,
                builder -> builder.operationId("fetchPluginSetting")
                    .description("Fetch setting of plugin.")
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
            .GET("plugins/{name}/config", this::fetchPluginConfig,
                builder -> builder.operationId("fetchPluginConfig")
                    .description("Fetch configMap of plugin by configured configMapName.")
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

    private Mono<ServerResponse> fetchPluginConfig(ServerRequest request) {
        final var name = request.pathVariable("name");
        return client.fetch(Plugin.class, name)
            .mapNotNull(plugin -> plugin.getSpec().getConfigMapName())
            .flatMap(configMapName -> client.fetch(ConfigMap.class, configMapName))
            .flatMap(configMap -> ServerResponse.ok().bodyValue(configMap));
    }

    private Mono<ServerResponse> fetchPluginSetting(ServerRequest request) {
        final var name = request.pathVariable("name");
        return client.fetch(Plugin.class, name)
            .mapNotNull(plugin -> plugin.getSpec().getSettingName())
            .flatMap(settingName -> client.fetch(Setting.class, settingName))
            .flatMap(setting -> ServerResponse.ok().bodyValue(setting));
    }

    private Mono<ServerResponse> updatePluginConfig(ServerRequest request) {
        final var pluginName = request.pathVariable("name");
        return client.fetch(Plugin.class, pluginName)
            .doOnNext(plugin -> {
                String configMapName = plugin.getSpec().getConfigMapName();
                if (!StringUtils.hasText(configMapName)) {
                    throw new ServerWebInputException(
                        "Unable to complete the request because the plugin configMapName is blank");
                }
            })
            .flatMap(plugin -> {
                final String configMapName = plugin.getSpec().getConfigMapName();
                return request.bodyToMono(ConfigMap.class)
                    .doOnNext(configMapToUpdate -> {
                        var configMapNameToUpdate = configMapToUpdate.getMetadata().getName();
                        if (!configMapName.equals(configMapNameToUpdate)) {
                            throw new ServerWebInputException(
                                "The name from the request body does not match the plugin "
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

    private Mono<ServerResponse> resetSettingConfig(ServerRequest request) {
        String name = request.pathVariable("name");
        return client.fetch(Plugin.class, name)
            .filter(plugin -> StringUtils.hasText(plugin.getSpec().getSettingName()))
            .flatMap(plugin -> {
                String configMapName = plugin.getSpec().getConfigMapName();
                String settingName = plugin.getSpec().getSettingName();
                return client.fetch(Setting.class, settingName)
                    .map(SettingUtils::settingDefinedDefaultValueMap)
                    .flatMap(data -> updateConfigMapData(configMapName, data));
            })
            .flatMap(configMap -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(configMap));
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

    private Mono<ServerResponse> upgrade(ServerRequest request) {
        var pluginNameInPath = request.pathVariable("name");
        var tempDirRef = new AtomicReference<Path>();
        var tempPluginPathRef = new AtomicReference<Path>();
        return client.fetch(Plugin.class, pluginNameInPath)
            .switchIfEmpty(Mono.error(() -> new ServerWebInputException(
                "The given plugin with name " + pluginNameInPath + " does not exit")))
            .then(request.multipartData())
            .flatMap(this::getJarFilePart)
            .flatMap(jarFilePart -> createTempDirectory()
                .doOnNext(tempDirRef::set)
                .flatMap(tempDirectory -> {
                    var pluginPath = tempDirectory.resolve(jarFilePart.filename());
                    return jarFilePart.transferTo(pluginPath).thenReturn(pluginPath);
                }))
            .doOnNext(tempPluginPathRef::set)
            .map(pluginPath -> new YamlPluginFinder().find(pluginPath))
            .doOnNext(newPlugin -> {
                // validate the plugin name
                if (!Objects.equals(pluginNameInPath, newPlugin.getMetadata().getName())) {
                    throw new ServerWebInputException(
                        "The uploaded plugin doesn't match the given plugin name");
                }
            })
            .flatMap(newPlugin -> deletePluginAndWaitForComplete(newPlugin.getMetadata().getName())
                .map(oldPlugin -> {
                    var enabled = oldPlugin.getSpec().getEnabled();
                    newPlugin.getSpec().setEnabled(enabled);
                    return newPlugin;
                })
            )
            .publishOn(Schedulers.boundedElastic())
            .doOnNext(newPlugin -> {
                // copy the Jar file into plugin root
                try {
                    var pluginRoot = Paths.get(pluginProperties.getPluginsRoot());
                    createDirectoriesIfNotExists(pluginRoot);
                    var tempPluginPath = tempPluginPathRef.get();
                    var filename = tempPluginPath.getFileName().toString();
                    copy(tempPluginPath, pluginRoot.resolve(newPlugin.generateFileName()));
                } catch (IOException e) {
                    throw Exceptions.propagate(e);
                }
            })
            .flatMap(client::create)
            .flatMap(newPlugin -> ServerResponse.ok().bodyValue(newPlugin))
            .doFinally(signalType -> deleteRecursivelyAndSilently(tempDirRef.get()));
    }

    private Mono<Plugin> deletePluginAndWaitForComplete(String pluginName) {
        return client.fetch(Plugin.class, pluginName)
            .flatMap(client::delete)
            .flatMap(plugin -> waitForDeleted(plugin.getMetadata().getName()).thenReturn(plugin));
    }

    private Mono<Void> waitForDeleted(String pluginName) {
        return client.fetch(Plugin.class, pluginName)
            .flatMap(plugin -> Mono.error(
                new RetryException("Re-check if the plugin is deleted successfully")))
            .retryWhen(Retry.fixedDelay(20, Duration.ofMillis(100))
                .filter(t -> t instanceof RetryException)
            )
            .onErrorMap(Exceptions::isRetryExhausted,
                t -> new ServerErrorException("Wait timeout for plugin deleted", t))
            .then();
    }

    private Mono<Path> createTempDirectory() {
        return Mono.fromCallable(() -> Files.createTempDirectory("halo-plugin-"))
            .subscribeOn(Schedulers.boundedElastic());
    }

    public static class ListRequest extends QueryListRequest {

        private final ServerWebExchange exchange;

        public ListRequest(ServerRequest request) {
            super(request.queryParams());
            this.exchange = request.exchange();
        }

        @Schema(name = "keyword", description = "Keyword of plugin name or description")
        public String getKeyword() {
            return queryParams.getFirst("keyword");
        }

        @Schema(name = "enabled", description = "Whether the plugin is enabled")
        public Boolean getEnabled() {
            var enabled = queryParams.getFirst("enabled");
            return enabled == null ? null : getSharedInstance().convert(enabled, Boolean.class);
        }

        @ArraySchema(uniqueItems = true,
            arraySchema = @Schema(name = "sort",
                description = "Sort property and direction of the list result. Supported fields: "
                    + "creationTimestamp"),
            schema = @Schema(description = "like field,asc or field,desc",
                implementation = String.class,
                example = "creationTimestamp,desc"))
        public Sort getSort() {
            return SortResolver.defaultInstance.resolve(exchange);
        }

        public Predicate<Plugin> toPredicate() {
            Predicate<Plugin> displayNamePredicate = plugin -> {
                var keyword = getKeyword();
                if (!StringUtils.hasText(keyword)) {
                    return true;
                }
                var displayName = plugin.getSpec().getDisplayName();
                if (!StringUtils.hasText(displayName)) {
                    return false;
                }
                return displayName.toLowerCase().contains(keyword.trim().toLowerCase());
            };
            Predicate<Plugin> descriptionPredicate = plugin -> {
                var keyword = getKeyword();
                if (!StringUtils.hasText(keyword)) {
                    return true;
                }
                var description = plugin.getSpec().getDescription();
                if (!StringUtils.hasText(description)) {
                    return false;
                }
                return description.toLowerCase().contains(keyword.trim().toLowerCase());
            };
            Predicate<Plugin> enablePredicate = plugin -> {
                var enabled = getEnabled();
                if (enabled == null) {
                    return true;
                }
                return Objects.equals(enabled, plugin.getSpec().getEnabled());
            };
            return displayNamePredicate.or(descriptionPredicate)
                .and(enablePredicate)
                .and(labelAndFieldSelectorToPredicate(getLabelSelector(), getFieldSelector()));
        }

        public Comparator<Plugin> toComparator() {
            var sort = getSort();
            var ctOrder = sort.getOrderFor("creationTimestamp");
            List<Comparator<Plugin>> comparators = new ArrayList<>();
            if (ctOrder != null) {
                Comparator<Plugin> comparator =
                    comparing(plugin -> plugin.getMetadata().getCreationTimestamp());
                if (ctOrder.isDescending()) {
                    comparator = comparator.reversed();
                }
                comparators.add(comparator);
            }
            comparators.add(Comparators.compareCreationTimestamp(false));
            comparators.add(Comparators.compareName(true));
            return comparators.stream()
                .reduce(Comparator::thenComparing)
                .orElse(null);
        }
    }

    Mono<ServerResponse> list(ServerRequest request) {
        return Mono.just(request)
            .map(ListRequest::new)
            .flatMap(listRequest -> {
                var predicate = listRequest.toPredicate();
                var comparator = listRequest.toComparator();
                return client.list(Plugin.class,
                    predicate,
                    comparator,
                    listRequest.getPage(),
                    listRequest.getSize());
            })
            .flatMap(listResult -> ServerResponse.ok().bodyValue(listResult));
    }

    public record InstallRequest(
        @Schema(required = true, description = "Plugin Jar file.") FilePart file) {
    }

    Mono<ServerResponse> install(ServerRequest request) {
        return request.multipartData()
            .flatMap(this::getJarFilePart)
            .flatMap(this::transferToTemp)
            .flatMap(tempJarFilePath -> {
                var plugin = new YamlPluginFinder().find(tempJarFilePath);
                // Disable auto enable during installation
                plugin.getSpec().setEnabled(false);
                return client.fetch(Plugin.class, plugin.getMetadata().getName())
                    .switchIfEmpty(Mono.defer(() -> client.create(plugin)))
                    .publishOn(Schedulers.boundedElastic())
                    .map(created -> {
                        String fileName = created.generateFileName();
                        var pluginRoot = Paths.get(pluginProperties.getPluginsRoot());
                        createDirectoriesIfNotExists(pluginRoot);
                        Path pluginFilePath = pluginRoot.resolve(fileName);
                        if (Files.exists(pluginFilePath)) {
                            throw new IllegalArgumentException(
                                "Plugin already installed : " + pluginFilePath);
                        }
                        FileUtils.copy(tempJarFilePath, pluginFilePath);
                        return created;
                    })
                    .onErrorResume(
                        error -> client.fetch(Plugin.class, plugin.getMetadata().getName())
                            .flatMap(client::delete)
                            .then(Mono.error(error))
                    )
                    .doFinally(signalType -> {
                        try {
                            Files.deleteIfExists(tempJarFilePath);
                        } catch (IOException e) {
                            log.error("Failed to delete temp file: {}", tempJarFilePath, e);
                        }
                    });
            })
            .flatMap(plugin -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(plugin));
    }

    private Mono<Path> transferToTemp(FilePart filePart) {
        return Mono.fromCallable(() -> Files.createTempFile("halo-plugins", ".jar"))
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap(path -> filePart.transferTo(path)
                .thenReturn(path)
            );
    }

    void createDirectoriesIfNotExists(Path directory) {
        if (Files.exists(directory)) {
            return;
        }
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create directory " + directory, e);
        }
    }

    Mono<FilePart> getJarFilePart(MultiValueMap<String, Part> formData) {
        Part part = formData.getFirst("file");
        if (!(part instanceof FilePart file)) {
            return Mono.error(new ServerWebInputException(
                "Invalid parameter of file, binary data is required"));
        }
        if (!Paths.get(file.filename()).toString().endsWith(".jar")) {
            return Mono.error(new ServerWebInputException(
                "Invalid file type, only jar is supported"));
        }
        return Mono.just(file);
    }
}
