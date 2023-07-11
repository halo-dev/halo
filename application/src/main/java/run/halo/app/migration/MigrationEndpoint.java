package run.halo.app.migration;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;

@Component
public class MigrationEndpoint implements CustomEndpoint {

    private final MigrationService migrationService;

    public MigrationEndpoint(MigrationService migrationService) {
        this.migrationService = migrationService;
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = groupVersion().toString() + "/Restoration";
        return SpringdocRouteBuilder.route()
            .POST("/restorations", request -> request.multipartData()
                    .map(RestoreRequest::new)
                    .flatMap(restoreRequest -> migrationService.restore(
                        restoreRequest.getFile().content()))
                    .flatMap(v -> ServerResponse.ok().bodyValue("Restored successfully!")),
                builder -> builder
                    .tag(tag)
                    .operationId("RestoreBackup")
                    .requestBody(requestBodyBuilder()
                        .required(true)
                        .content(contentBuilder()
                            .mediaType(MediaType.MULTIPART_FORM_DATA_VALUE)
                            .schema(schemaBuilder().implementation(RestoreRequest.class))
                        )
                    )
                    .build())
            .build();
    }

    public static class RestoreRequest {
        private final MultiValueMap<String, Part> multipart;

        public RestoreRequest(MultiValueMap<String, Part> multipart) {
            this.multipart = multipart;
        }

        @Schema(requiredMode = REQUIRED, name = "file", description = "Backup file.")
        public FilePart getFile() {
            var part = multipart.getFirst("file");
            if (part instanceof FilePart filePart) {
                return filePart;
            }
            throw new ServerWebInputException("Invalid file part");
        }
    }


    @Override
    public GroupVersion groupVersion() {
        return CustomEndpoint.super.groupVersion();
    }
}
