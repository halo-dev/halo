package run.halo.app.core.extension.endpoint;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.fn.builders.schema.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Plugin;
import run.halo.app.extension.ReactiveExtensionClient;
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
                            .schema(Builder.schemaBuilder().implementation(InstallRequest.class))
                        ))
                    .response(responseBuilder().implementation(Plugin.class))
            )
            .build();
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
