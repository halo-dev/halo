package run.halo.app.core.extension.endpoint;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springdoc.core.fn.builders.schema.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class PluginEndpoint implements CustomEndpoint {

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final var tag = "api.halo.run/v1alpha1/Plugin";
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
                    .response(responseBuilder())
            )
            .build();
    }

    public record InstallRequest(
        @Schema(required = true, description = "Plugin Jar file.") MultipartFile file) {
    }

    private Mono<ServerResponse> install(ServerRequest request) {
        return request.bodyToMono(new ParameterizedTypeReference<MultiValueMap<String, Part>>() {
            })
            .flatMap(installRequest -> {
                return ServerResponse.ok().build();
            });
    }

}
