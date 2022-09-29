package run.halo.app.core.extension.endpoint;

import static java.util.Comparator.comparing;
import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;
import static org.springframework.boot.convert.ApplicationConversionService.getSharedInstance;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static run.halo.app.extension.ListResult.generateGenericClass;
import static run.halo.app.extension.router.QueryParamBuildUtil.buildParametersFromType;
import static run.halo.app.extension.router.selector.SelectorUtil.labelAndFieldSelectorToPredicate;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Plugin;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.router.IListRequest.QueryListRequest;
import run.halo.app.plugin.PluginProperties;
import run.halo.app.plugin.YamlPluginFinder;

@Slf4j
@Component
public class PluginEndpoint implements CustomEndpoint {

    private final PluginProperties pluginProperties;

    private final ReactiveExtensionClient client;

    public PluginEndpoint(PluginProperties pluginProperties, ReactiveExtensionClient client) {
        this.pluginProperties = pluginProperties;
        this.client = client;
    }

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
            .GET("plugins", this::list, builder -> {
                builder.operationId("ListPlugins")
                    .tag(tag)
                    .description("List plugins using query criteria and sort params")
                    .response(responseBuilder().implementation(generateGenericClass(Plugin.class)));
                buildParametersFromType(builder, ListRequest.class);
            })
            .build();
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
                example = "creationtimestamp,desc"))
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
            Comparator<Plugin> comparator = null;
            if (ctOrder != null) {
                comparator = comparing(plugin -> plugin.getMetadata().getCreationTimestamp());
                if (ctOrder.isDescending()) {
                    comparator = comparator.reversed();
                }
            }
            return comparator;
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
        return request.bodyToMono(new ParameterizedTypeReference<MultiValueMap<String, Part>>() {
            })
            .flatMap(this::getJarFilePart)
            .flatMap(file -> {
                var pluginRoot = Paths.get(pluginProperties.getPluginsRoot());
                createDirectoriesIfNotExists(pluginRoot);
                var pluginPath = pluginRoot.resolve(file.filename());
                return file.transferTo(pluginPath).thenReturn(pluginPath);
            })
            .flatMap(pluginPath -> {
                log.info("Plugin uploaded at {}", pluginPath);
                var plugin = new YamlPluginFinder().find(pluginPath);
                // overwrite the enabled flag
                plugin.getSpec().setEnabled(false);
                return client.fetch(Plugin.class, plugin.getMetadata().getName())
                    .switchIfEmpty(Mono.defer(() -> client.create(plugin)));
            })
            .flatMap(plugin -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(plugin));
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
