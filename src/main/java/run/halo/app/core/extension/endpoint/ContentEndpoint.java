package run.halo.app.core.extension.endpoint;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
import run.halo.app.core.extension.Snapshot;

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
                        .implementation(ContentWrapper.class))
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
                        .implementation(ContentWrapper.class))
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
                        .implementation(ContentWrapper.class))
            )
            .PUT("contents/{snapshotName}/publish", this::publishSnapshotContent,
                builder -> builder.operationId("PublishSnapshotContent")
                    .description("Publish a snapshot content.")
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
                                .implementation(Snapshot.SubjectRef.class))
                        ))
                    .response(responseBuilder()
                        .implementation(ContentWrapper.class))
            )
            .build();
    }

    private Mono<ServerResponse> obtainContent(ServerRequest request) {
        String snapshotName = request.pathVariable("snapshotName");
        return contentService.getContent(snapshotName)
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
            .flatMap(content -> ServerResponse.ok().bodyValue(content));
    }

    private Mono<ServerResponse> publishSnapshotContent(ServerRequest request) {
        String snapshotName = request.pathVariable("snapshotName");
        return request.bodyToMono(Snapshot.SubjectRef.class)
            .flatMap(subjectRef -> contentService.publish(snapshotName, subjectRef))
            .flatMap(content -> ServerResponse.ok().bodyValue(content));
    }

    private Mono<ServerResponse> draftSnapshotContent(ServerRequest request) {
        return request.bodyToMono(ContentRequest.class)
            .flatMap(contentService::draftContent)
            .flatMap(content -> ServerResponse.ok().bodyValue(content));
    }
}
