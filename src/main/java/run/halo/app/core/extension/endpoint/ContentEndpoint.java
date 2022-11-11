package run.halo.app.core.extension.endpoint;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springdoc.core.fn.builders.schema.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.content.ContentRequest;
import run.halo.app.content.ContentService;
import run.halo.app.content.ContentWrapper;

/**
 * Endpoint for managing content.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class ContentEndpoint implements CustomEndpoint {

    private final ContentService contentService;

    public ContentEndpoint(ContentService contentService) {
        this.contentService = contentService;
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final var tag = "api.console.halo.run/v1alpha1/Content";
        return SpringdocRouteBuilder.route()
            .GET("contents/{snapshotName}", this::obtainContent,
                builder -> builder.operationId("ObtainSnapshotContent")
                    .description("Obtain a snapshot content.")
                    .tag(tag)
                    .parameter(parameterBuilder()
                        .required(true)
                        .name("snapshotName")
                        .in(ParameterIn.PATH))
                    .response(responseBuilder()
                        .implementation(ContentResponse.class))
            )
            .POST("contents", this::draftSnapshotContent,
                builder -> builder.operationId("DraftSnapshotContent")
                    .description("Draft a snapshot content.")
                    .tag(tag)
                    .requestBody(requestBodyBuilder()
                        .required(true)
                        .content(contentBuilder()
                            .mediaType(MediaType.APPLICATION_JSON_VALUE)
                            .schema(Builder.schemaBuilder()
                                .implementation(ContentRequest.class))
                        ))
                    .response(responseBuilder()
                        .implementation(ContentResponse.class))
            )
            .PUT("contents/{snapshotName}", this::updateSnapshotContent,
                builder -> builder.operationId("UpdateSnapshotContent")
                    .description("Update a snapshot content.")
                    .tag(tag)
                    .parameter(parameterBuilder()
                        .required(true)
                        .name("snapshotName")
                        .in(ParameterIn.PATH))
                    .requestBody(requestBodyBuilder()
                        .required(true)
                        .content(contentBuilder()
                            .mediaType(MediaType.APPLICATION_JSON_VALUE)
                            .schema(Builder.schemaBuilder()
                                .implementation(ContentRequest.class))
                        ))
                    .response(responseBuilder()
                        .implementation(ContentResponse.class))
            )
            .build();
    }

    private Mono<ServerResponse> obtainContent(ServerRequest request) {
        String snapshotName = request.pathVariable("snapshotName");
        return contentService.getContent(snapshotName)
            .map(ContentResponse::from)
            .flatMap(content -> ServerResponse.ok().bodyValue(content));
    }

    private Mono<ServerResponse> updateSnapshotContent(ServerRequest request) {
        String snapshotName = request.pathVariable("snapshotName");
        return request.bodyToMono(ContentRequest.class)
            .flatMap(content -> {
                ContentRequest contentRequest =
                    new ContentRequest(content.subjectRef(), snapshotName,
                        content.raw(), content.content(), content.rawType());
                return contentService.updateContent(contentRequest);
            })
            .map(ContentResponse::from)
            .flatMap(content -> ServerResponse.ok().bodyValue(content));
    }

    private Mono<ServerResponse> draftSnapshotContent(ServerRequest request) {
        return request.bodyToMono(ContentRequest.class)
            .flatMap(contentService::draftContent)
            .map(ContentResponse::from)
            .flatMap(content -> ServerResponse.ok().bodyValue(content));
    }

    @Data
    public static class ContentResponse {
        @Schema(required = true, description = "The headSnapshotName if updated or new name if "
            + "created.")
        private String snapshotName;
        @Schema(required = true)
        private String raw;

        @Schema(required = true)
        private String content;

        @Schema(required = true)
        private String rawType;

        /**
         * Converts content response from {@link ContentWrapper}.
         */
        public static ContentResponse from(ContentWrapper wrapper) {
            ContentResponse response = new ContentResponse();
            response.raw = wrapper.getRaw();
            response.setSnapshotName(wrapper.getSnapshotName());
            response.content = wrapper.getContent();
            response.rawType = wrapper.getRawType();
            return response;
        }
    }
}
