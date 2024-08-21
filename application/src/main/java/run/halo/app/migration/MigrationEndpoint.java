package run.halo.app.migration;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import java.util.function.Supplier;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.data.util.Optionals;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.ReactiveUrlDataBufferFetcher;

@Component
public class MigrationEndpoint implements CustomEndpoint {

    private final MigrationService migrationService;

    private final ReactiveExtensionClient client;

    private final ReactiveUrlDataBufferFetcher dataBufferFetcher;

    public MigrationEndpoint(MigrationService migrationService,
        ReactiveExtensionClient client,
        ReactiveUrlDataBufferFetcher dataBufferFetcher) {
        this.migrationService = migrationService;
        this.client = client;
        this.dataBufferFetcher = dataBufferFetcher;
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = "MigrationV1alpha1Console";
        return SpringdocRouteBuilder.route()
            .GET("/backup-files",
                this::getBackups,
                builder -> builder.operationId("getBackupFiles")
                    .tag(tag)
                    .description("Get backup files from backup root.")
                    .response(responseBuilder()
                        .implementationArray(BackupFile.class)
                    )
            )
            .GET("/backups/{name}/files/{filename}",
                request -> {
                    var name = request.pathVariable("name");
                    return client.get(Backup.class, name)
                        .flatMap(migrationService::download)
                        .flatMap(backupResource -> ServerResponse.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + backupResource.getFilename() + "\"")
                            .contentType(MediaType.APPLICATION_OCTET_STREAM)
                            .bodyValue(backupResource));
                },
                builder -> builder
                    .tag(tag)
                    .operationId("DownloadBackups")
                    .parameter(parameterBuilder()
                        .name("name")
                        .description("Backup name.")
                        .required(true)
                        .in(ParameterIn.PATH))
                    .parameter(parameterBuilder()
                        .name("filename")
                        .description("Backup filename.")
                        .required(true)
                        .in(ParameterIn.PATH))
                    .build())
            .POST("/restorations", request -> request.multipartData()
                    .map(RestoreRequest::new)
                    .flatMap(restoreRequest -> {
                        var content = getContent(restoreRequest)
                            .switchIfEmpty(Mono.error(() -> new ServerWebInputException(
                                "Please upload a file "
                                    + "or provide a download link or backup name.")));
                        return migrationService.restore(content);
                    })
                    .then(Mono.defer(
                        () -> ServerResponse.ok().bodyValue("Restored successfully!")
                    )),
                builder -> builder
                    .tag(tag)
                    .description("Restore backup by uploading file "
                        + "or providing download link or backup name.")
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

    private Mono<ServerResponse> getBackups(ServerRequest request) {
        var backupFiles = migrationService.getBackupFiles();
        return ServerResponse.ok().body(backupFiles, BackupFile.class);
    }

    private Flux<DataBuffer> getContent(RestoreRequest request) {
        Supplier<Optional<Flux<DataBuffer>>> contentFromFilename = () ->
            request.getFilename().map(filename -> migrationService.getBackupFile(filename)
                .map(BackupFile::getPath)
                .flatMapMany(
                    path -> DataBufferUtils.read(
                        path,
                        DefaultDataBufferFactory.sharedInstance,
                        StreamUtils.BUFFER_SIZE)));

        Supplier<Optional<Flux<DataBuffer>>> contentFromDownloadUrl = () -> request.getDownloadUrl()
            .map(downloadURL -> {
                try {
                    var url = new URL(downloadURL);
                    return dataBufferFetcher.fetch(url.toURI());
                } catch (MalformedURLException e) {
                    return Flux.<DataBuffer>error(new ServerWebInputException(
                        "Invalid download URL: " + downloadURL));
                } catch (URISyntaxException e) {
                    // Should never happen
                    return Flux.<DataBuffer>error(e);
                }
            });

        Supplier<Optional<Flux<DataBuffer>>> contentFromUpload = () -> request.getFile()
            .map(Part::content);

        Supplier<Optional<Flux<DataBuffer>>> contentFromBackupName = () -> request.getBackupName()
            .map(backupName -> client.get(Backup.class, backupName)
                .flatMap(migrationService::download)
                .flatMapMany(resource -> DataBufferUtils.read(resource,
                    DefaultDataBufferFactory.sharedInstance,
                    StreamUtils.BUFFER_SIZE)));

        return Optionals.firstNonEmpty(
                contentFromUpload,
                contentFromDownloadUrl,
                contentFromBackupName,
                contentFromFilename
            )
            .orElseGet(() -> Flux.error(new ServerWebInputException("""
                Please upload a file or provide a download link or backup name or backup filename.\
                """)));
    }

    @Schema(types = "object")
    public static class RestoreRequest {
        private final MultiValueMap<String, Part> multipart;

        public RestoreRequest(MultiValueMap<String, Part> multipart) {
            this.multipart = multipart;
        }

        @Schema(requiredMode = NOT_REQUIRED, name = "file", description = "Backup file.")
        public Optional<FilePart> getFile() {
            var part = multipart.getFirst("file");
            if (part instanceof FilePart filePart) {
                return Optional.of(filePart);
            }
            return Optional.empty();
        }

        @Schema(requiredMode = NOT_REQUIRED, name = "filename", description = """
            Filename of backup file in backups root.\
            """)
        public Optional<String> getFilename() {
            var part = multipart.getFirst("filename");
            if (part instanceof FormFieldPart filenamePart) {
                return Optional.of(filenamePart.value())
                    .filter(StringUtils::hasText);
            }
            return Optional.empty();
        }

        @Schema(requiredMode = NOT_REQUIRED,
            name = "downloadUrl",
            description = "Remote backup HTTP URL.")
        public Optional<String> getDownloadUrl() {
            var part = multipart.getFirst("downloadUrl");
            if (part instanceof FormFieldPart downloadUrlPart) {
                return Optional.of(downloadUrlPart.value())
                    .filter(StringUtils::hasText);
            }
            return Optional.empty();
        }

        @Schema(requiredMode = NOT_REQUIRED,
            name = "backupName",
            description = "Backup metadata name.")
        public Optional<String> getBackupName() {
            var part = multipart.getFirst("backupName");
            if (part instanceof FormFieldPart backupNamePart) {
                return Optional.of(backupNamePart.value())
                    .filter(StringUtils::hasText);
            }
            return Optional.empty();
        }
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion(
            "console.api." + Constant.GROUP + "/" + Constant.VERSION);
    }
}
