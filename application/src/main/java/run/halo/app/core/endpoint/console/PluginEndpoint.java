package run.halo.app.core.endpoint.console;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;
import static org.springframework.boot.convert.ApplicationConversionService.getSharedInstance;
import static org.springframework.core.io.buffer.DataBufferUtils.write;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static run.halo.app.extension.ListResult.generateGenericClass;
import static run.halo.app.extension.index.query.QueryFactory.contains;
import static run.halo.app.extension.index.query.QueryFactory.equal;
import static run.halo.app.extension.index.query.QueryFactory.or;
import static run.halo.app.extension.router.QueryParamBuildUtil.sortParameter;
import static run.halo.app.infra.utils.FileUtils.deleteFileSilently;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Sort;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;
import run.halo.app.core.extension.Plugin;
import run.halo.app.core.extension.Setting;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.core.user.service.SettingConfigService;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.router.IListRequest;
import run.halo.app.extension.router.SortableRequest;
import run.halo.app.infra.ReactiveUrlDataBufferFetcher;
import run.halo.app.infra.utils.SettingUtils;
import run.halo.app.plugin.PluginService;

@Slf4j
@Component
public class PluginEndpoint implements CustomEndpoint, InitializingBean {

    private final ReactiveExtensionClient client;

    private final PluginService pluginService;

    private final ReactiveUrlDataBufferFetcher reactiveUrlDataBufferFetcher;

    private final SettingConfigService settingConfigService;

    private final WebProperties webProperties;

    private final Scheduler scheduler = Schedulers.boundedElastic();

    private boolean useLastModified;

    private CacheControl bundleCacheControl = CacheControl.empty();

    public PluginEndpoint(ReactiveExtensionClient client,
        PluginService pluginService,
        ReactiveUrlDataBufferFetcher reactiveUrlDataBufferFetcher,
        SettingConfigService settingConfigService,
        WebProperties webProperties) {
        this.client = client;
        this.pluginService = pluginService;
        this.reactiveUrlDataBufferFetcher = reactiveUrlDataBufferFetcher;
        this.settingConfigService = settingConfigService;
        this.webProperties = webProperties;
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = "PluginV1alpha1Console";
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
            .POST("plugins/-/install-from-uri", this::installFromUri,
                builder -> builder.operationId("InstallPluginFromUri")
                    .description("Install a plugin from uri.")
                    .tag(tag)
                    .requestBody(requestBodyBuilder()
                        .required(true)
                        .content(contentBuilder()
                            .mediaType(MediaType.APPLICATION_JSON_VALUE)
                            .schema(schemaBuilder()
                                .implementation(InstallFromUriRequest.class))
                        ))
                    .response(responseBuilder()
                        .implementation(Plugin.class))
            )
            .POST("plugins/{name}/upgrade-from-uri", this::upgradeFromUri,
                builder -> builder.operationId("UpgradePluginFromUri")
                    .description("Upgrade a plugin from uri.")
                    .tag(tag)
                    .parameter(parameterBuilder()
                        .in(ParameterIn.PATH)
                        .name("name")
                        .required(true)
                    )
                    .requestBody(requestBodyBuilder()
                        .required(true)
                        .content(contentBuilder()
                            .mediaType(MediaType.APPLICATION_JSON_VALUE)
                            .schema(schemaBuilder()
                                .implementation(UpgradeFromUriRequest.class))
                        ))
                    .response(responseBuilder()
                        .implementation(Plugin.class))
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
            .PUT("plugins/{name}/json-config", this::updatePluginJsonConfig,
                builder -> builder.operationId("updatePluginJsonConfig")
                    .description("Update the config of plugin setting.")
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
                            .schema(schemaBuilder().implementation(ObjectNode.class))))
                    .response(responseBuilder()
                        .responseCode(String.valueOf(HttpStatus.NO_CONTENT.value()))
                        .implementation(Void.class))
            )
            .PUT("plugins/{name}/config", this::updatePluginConfig,
                builder -> builder.operationId("updatePluginConfig")
                    .description(
                        "Update the configMap of plugin setting, it is deprecated since 2.20.0")
                    .tag(tag)
                    .parameter(parameterBuilder()
                        .name("name")
                        .in(ParameterIn.PATH)
                        .required(true)
                        .implementation(String.class)
                    )
                    .deprecated(true)
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
            .PUT("plugins/{name}/reload", this::reload,
                builder -> builder.operationId("reloadPlugin")
                    .description("Reload a plugin by name.")
                    .tag(tag)
                    .parameter(parameterBuilder()
                        .name("name")
                        .in(ParameterIn.PATH)
                        .required(true)
                        .implementation(String.class)
                    )
                    .response(responseBuilder()
                        .implementation(Plugin.class))
            )
            .PUT("plugins/{name}/plugin-state", this::changePluginRunningState,
                builder -> builder.operationId("ChangePluginRunningState")
                    .description("Change the running state of a plugin by name.")
                    .tag(tag)
                    .parameter(parameterBuilder()
                        .name("name")
                        .in(ParameterIn.PATH)
                        .required(true)
                        .implementation(String.class)
                    )
                    .requestBody(requestBodyBuilder()
                        .required(true)
                        .content(contentBuilder()
                            .mediaType(MediaType.APPLICATION_JSON_VALUE)
                            .schema(schemaBuilder()
                                .implementation(RunningStateRequest.class))
                        )
                    )
                    .response(responseBuilder()
                        .implementation(Plugin.class))
            )
            .GET("plugins", this::list, builder -> {
                builder.operationId("ListPlugins")
                    .tag(tag)
                    .description("List plugins using query criteria and sort params")
                    .response(responseBuilder().implementation(generateGenericClass(Plugin.class)));
                ListRequest.buildParameters(builder);
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
                    .description(
                        "Fetch configMap of plugin by configured configMapName. it is deprecated "
                            + "since 2.20.0")
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
            .GET("plugins/{name}/json-config", this::fetchPluginJsonConfig,
                builder -> builder.operationId("fetchPluginJsonConfig")
                    .description(
                        "Fetch converted json config of plugin by configured configMapName.")
                    .tag(tag)
                    .parameter(parameterBuilder()
                        .name("name")
                        .in(ParameterIn.PATH)
                        .required(true)
                        .implementation(String.class)
                    )
                    .response(responseBuilder()
                        .implementation(ObjectNode.class))
            )
            .GET("plugins/-/bundle.js", this::fetchJsBundle,
                builder -> builder.operationId("fetchJsBundle")
                    .description("Merge all JS bundles of enabled plugins into one.")
                    .tag(tag)
                    .response(responseBuilder().implementation(String.class))
            )
            .GET("plugins/-/bundle.css", this::fetchCssBundle,
                builder -> builder.operationId("fetchCssBundle")
                    .description("Merge all CSS bundles of enabled plugins into one.")
                    .tag(tag)
                    .response(responseBuilder().implementation(String.class))
            )
            .build();
    }

    private Mono<ServerResponse> fetchPluginJsonConfig(ServerRequest request) {
        final var name = request.pathVariable("name");
        return client.fetch(Plugin.class, name)
            .mapNotNull(plugin -> plugin.getSpec().getConfigMapName())
            .flatMap(settingConfigService::fetchConfig)
            .flatMap(json -> ServerResponse.ok().bodyValue(json));
    }

    private Mono<ServerResponse> updatePluginJsonConfig(ServerRequest request) {
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
                return request.bodyToMono(ObjectNode.class)
                    .switchIfEmpty(
                        Mono.error(new ServerWebInputException("Required request body is missing")))
                    .flatMap(configMapJsonData ->
                        settingConfigService.upsertConfig(configMapName, configMapJsonData));
            })
            .then(ServerResponse.noContent().build());
    }

    Mono<ServerResponse> changePluginRunningState(ServerRequest request) {
        final var name = request.pathVariable("name");
        return request.bodyToMono(RunningStateRequest.class)
            .flatMap(runningState -> {
                var enable = runningState.isEnable();
                var async = runningState.isAsync();
                return pluginService.changeState(name, enable, !async);
            })
            .flatMap(plugin -> ServerResponse.ok().bodyValue(plugin));
    }

    @Override
    public void afterPropertiesSet() {
        var cache = this.webProperties.getResources().getCache();
        this.useLastModified = cache.isUseLastModified();
        var cacheControl = cache.getCachecontrol().toHttpCacheControl();
        if (cacheControl != null) {
            this.bundleCacheControl = cacheControl;
        }
    }

    @Data
    @Schema(name = "PluginRunningStateRequest")
    static class RunningStateRequest {
        private boolean enable;
        private boolean async;
    }

    private Mono<ServerResponse> fetchJsBundle(ServerRequest request) {
        var versionOption = request.queryParam("v");
        return versionOption.map(s -> pluginService.getJsBundle(s).flatMap(
                jsRes -> {
                    var bodyBuilder = ServerResponse.ok()
                        .cacheControl(bundleCacheControl)
                        .contentType(MediaType.valueOf("text/javascript"));
                    if (useLastModified) {
                        try {
                            var lastModified = Instant.ofEpochMilli(jsRes.lastModified());
                            bodyBuilder = bodyBuilder.lastModified(lastModified);
                        } catch (IOException e) {
                            if (e instanceof FileNotFoundException) {
                                return Mono.error(new NoResourceFoundException("bundle.js"));
                            }
                            return Mono.error(e);
                        }
                    }
                    return bodyBuilder.body(BodyInserters.fromResource(jsRes));
                }))
            .orElseGet(() -> pluginService.generateBundleVersion()
                .flatMap(version -> ServerResponse
                    .temporaryRedirect(buildJsBundleUri("js", version))
                    .cacheControl(CacheControl.noStore())
                    .build()));
    }

    private Mono<ServerResponse> fetchCssBundle(ServerRequest request) {
        return request.queryParam("v")
            .map(s -> pluginService.getCssBundle(s).flatMap(cssRes -> {
                var bodyBuilder = ServerResponse.ok()
                    .cacheControl(bundleCacheControl)
                    .contentType(MediaType.valueOf("text/css"));
                if (useLastModified) {
                    try {
                        var lastModified = Instant.ofEpochMilli(cssRes.lastModified());
                        bodyBuilder = bodyBuilder.lastModified(lastModified);
                    } catch (IOException e) {
                        if (e instanceof FileNotFoundException) {
                            return Mono.error(new NoResourceFoundException("bundle.css"));
                        }
                        return Mono.error(e);
                    }
                }
                return bodyBuilder.body(BodyInserters.fromResource(cssRes));
            }))
            .orElseGet(() -> pluginService.generateBundleVersion()
                .flatMap(version -> ServerResponse
                    .temporaryRedirect(buildJsBundleUri("css", version))
                    .cacheControl(CacheControl.noStore())
                    .build()));

    }

    URI buildJsBundleUri(String type, String version) {
        return URI.create(
            "/apis/api.console.halo.run/v1alpha1/plugins/-/bundle." + type + "?v=" + version);
    }

    private Mono<ServerResponse> upgradeFromUri(ServerRequest request) {
        var name = request.pathVariable("name");
        var content = request.bodyToMono(UpgradeFromUriRequest.class)
            .map(UpgradeFromUriRequest::uri)
            .flatMapMany(reactiveUrlDataBufferFetcher::fetch);

        return Mono.usingWhen(
                writeToTempFile(content),
                path -> pluginService.upgrade(name, path),
                this::deleteFileIfExists)
            .flatMap(upgradedPlugin -> ServerResponse.ok().bodyValue(upgradedPlugin));
    }

    private Mono<ServerResponse> installFromUri(ServerRequest request) {
        var content = request.bodyToMono(InstallFromUriRequest.class)
            .map(InstallFromUriRequest::uri)
            .flatMapMany(reactiveUrlDataBufferFetcher::fetch);

        return Mono.usingWhen(
                writeToTempFile(content),
                pluginService::install,
                this::deleteFileIfExists
            )
            .flatMap(newPlugin -> ServerResponse.ok().bodyValue(newPlugin));
    }

    public record InstallFromUriRequest(@Schema(requiredMode = REQUIRED) URI uri) {
    }

    public record UpgradeFromUriRequest(@Schema(requiredMode = REQUIRED) URI uri) {
    }

    private Mono<ServerResponse> reload(ServerRequest serverRequest) {
        var name = serverRequest.pathVariable("name");
        return ServerResponse.ok().body(pluginService.reload(name), Plugin.class);
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


    private Mono<ServerResponse> install(ServerRequest request) {
        return request.multipartData()
            .map(InstallRequest::new)
            .flatMap(installRequest -> installRequest.getSource()
                .flatMap(source -> {
                    if (InstallSource.FILE.equals(source)) {
                        return installFromFile(installRequest.getFile(), pluginService::install);
                    }
                    return Mono.error(
                        new UnsupportedOperationException("Unsupported install source " + source));
                }))
            .flatMap(plugin -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(plugin));
    }

    private Mono<ServerResponse> upgrade(ServerRequest request) {
        var pluginName = request.pathVariable("name");
        return request.multipartData()
            .map(InstallRequest::new)
            .flatMap(installRequest -> installRequest.getSource()
                .flatMap(source -> {
                    if (InstallSource.FILE.equals(source)) {
                        return installFromFile(installRequest.getFile(),
                            path -> pluginService.upgrade(pluginName, path));
                    }
                    return Mono.error(
                        new UnsupportedOperationException("Unsupported install source " + source));
                }))
            .flatMap(upgradedPlugin -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(upgradedPlugin));
    }

    private Mono<Plugin> installFromFile(FilePart filePart,
        Function<Path, Mono<Plugin>> resourceClosure) {
        return Mono.usingWhen(
            writeToTempFile(filePart.content()),
            resourceClosure,
            this::deleteFileIfExists);
    }

    public static class ListRequest extends SortableRequest {

        public ListRequest(ServerRequest request) {
            super(request.exchange());
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

        @Override
        public Sort getSort() {
            var orders = super.getSort().stream()
                .map(order -> {
                    if ("creationTimestamp".equals(order.getProperty())) {
                        return order.withProperty("metadata.creationTimestamp");
                    }
                    return order;
                })
                .toList();
            return Sort.by(orders);
        }

        @Override
        public ListOptions toListOptions() {
            var builder = ListOptions.builder(super.toListOptions());

            Optional.ofNullable(queryParams.getFirst("keyword"))
                .filter(StringUtils::hasText)
                .ifPresent(keyword -> builder.andQuery(or(
                    contains("spec.displayName", keyword),
                    contains("spec.description", keyword)
                )));

            Optional.ofNullable(queryParams.getFirst("enabled"))
                .map(Boolean::parseBoolean)
                .ifPresent(enabled -> builder.andQuery(equal("spec.enabled", enabled.toString())));

            return builder.build();
        }

        public static void buildParameters(Builder builder) {
            IListRequest.buildParameters(builder);
            builder.parameter(sortParameter());
            builder.parameter(parameterBuilder()
                    .in(ParameterIn.QUERY)
                    .name("keyword")
                    .description("Keyword of plugin name or description")
                    .implementation(String.class)
                    .required(false))
                .parameter(parameterBuilder()
                    .in(ParameterIn.QUERY)
                    .name("enabled")
                    .description("Whether the plugin is enabled")
                    .implementation(Boolean.class)
                    .required(false));
        }
    }

    Mono<ServerResponse> list(ServerRequest request) {
        return Mono.just(request)
            .map(ListRequest::new)
            .flatMap(listRequest -> client.listBy(
                Plugin.class,
                listRequest.toListOptions(),
                listRequest.toPageRequest()
            ))
            .flatMap(listResult -> ServerResponse.ok().bodyValue(listResult));
    }

    @Schema(name = "PluginInstallRequest", types = "object")
    public static class InstallRequest {

        private final MultiValueMap<String, Part> multipartData;

        public InstallRequest(MultiValueMap<String, Part> multipartData) {
            this.multipartData = multipartData;
        }

        @Schema(requiredMode = NOT_REQUIRED, description = "Plugin Jar file.")
        public FilePart getFile() {
            var part = multipartData.getFirst("file");
            if (part == null) {
                throw new ServerWebInputException("Form field file is required");
            }
            if (!(part instanceof FilePart file)) {
                throw new ServerWebInputException("Invalid parameter of file");
            }
            if (!Paths.get(file.filename()).toString().endsWith(".jar")) {
                throw new ServerWebInputException("Invalid file type, only jar is supported");
            }
            return file;
        }

        @Schema(requiredMode = NOT_REQUIRED,
            description = "Plugin preset name. We will find the plugin from plugin presets")
        public Mono<String> getPresetName() {
            var part = multipartData.getFirst("presetName");
            if (part == null) {
                return Mono.error(new ServerWebInputException(
                    "Form field presetName is required."));
            }
            if (!(part instanceof FormFieldPart presetName)) {
                return Mono.error(new ServerWebInputException(
                    "Invalid format of presetName field, string required"));
            }
            if (!StringUtils.hasText(presetName.value())) {
                return Mono.error(new ServerWebInputException("presetName must not be blank"));
            }
            return Mono.just(presetName.value());
        }

        @Schema(requiredMode = NOT_REQUIRED, description = "Install source. Default is file.")
        public Mono<InstallSource> getSource() {
            var part = multipartData.getFirst("source");
            if (part == null) {
                return Mono.just(InstallSource.FILE);
            }
            if (!(part instanceof FormFieldPart sourcePart)) {
                return Mono.error(new ServerWebInputException(
                    "Invalid format of source field, string required."));
            }
            var installSource = InstallSource.valueOf(sourcePart.value().toUpperCase());
            return Mono.just(installSource);
        }
    }

    public enum InstallSource {
        FILE,
        PRESET,
        URL
    }

    Mono<Void> deleteFileIfExists(Path path) {
        return deleteFileSilently(path, this.scheduler).then();
    }

    private Mono<Path> writeToTempFile(Publisher<DataBuffer> content) {
        return Mono.fromCallable(() -> Files.createTempFile("halo-plugin-", ".jar"))
            .flatMap(path -> write(content, path).thenReturn(path))
            .subscribeOn(this.scheduler);
    }

}
