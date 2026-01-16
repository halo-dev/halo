package run.halo.app.core.endpoint;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.service.AttachmentService;
import run.halo.app.infra.SystemSetting.Attachment.UploadOptions;

@Slf4j
@RequiredArgsConstructor
@Component
public class AttachmentHandler {

    private final AttachmentService attachmentService;

    /**
     * Build OpenAPI doc of request and response for upload attachment endpoint.
     *
     * @param builder the operation builder
     */
    public void buildDoc(Builder builder) {
        builder.requestBody(requestBodyBuilder()
                .content(contentBuilder()
                    .mediaType(MediaType.MULTIPART_FORM_DATA_VALUE)
                    .schema(schemaBuilder().implementation(UploadForm.class))
                )
            )
            .response(responseBuilder().implementation(Attachment.class));
    }

    /**
     * Handle upload attachment request.
     *
     * @param serverRequest the server request
     * @param getConfig the upload options fetcher
     * @return the server response
     */
    public Mono<ServerResponse> handleUpload(ServerRequest serverRequest,
        Mono<UploadOptions> getConfig) {
        var getForm = serverRequest.bind(UploadForm.class);
        var uploadAttachment = Mono.zip(getForm, getConfig)
            .flatMap(tuple2 -> {
                var form = tuple2.getT1();
                var config = tuple2.getT2();
                var file = form.getFile();
                var upload = Mono.defer(() -> {
                    if (file != null) {
                        var mediaType = Optional.ofNullable(file.headers().getContentType())
                            .orElse(MediaType.APPLICATION_OCTET_STREAM);
                        if (log.isDebugEnabled()) {
                            log.debug("Preparing to upload attachment [filename={} mediaType={}]",
                                file.name(), mediaType);
                        }
                        return attachmentService.upload(
                            config.policyName(),
                            config.groupName(),
                            file.filename(),
                            file.content(),
                            mediaType
                        );
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Preparing to upload attachment from url [{}], filename: {}",
                            form.getUrl(), form.getFilename());
                    }
                    var url = Optional.ofNullable(form.getUrl())
                        .filter(StringUtils::hasText)
                        .map(URI::create)
                        .map(uri -> {
                            try {
                                return uri.toURL();
                            } catch (MalformedURLException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .orElse(null);
                    if (url == null) {
                        return Mono.error(new ServerWebInputException(
                            "Invalid url provided: " + form.getUrl()
                        ));
                    }
                    return attachmentService.uploadFromUrl(
                        url, config.policyName(), config.groupName(), form.getFilename()
                    );
                });
                return upload.flatMap(a -> attachmentService.getPermalink(a)
                    .doOnNext(permalink -> a.getStatus().setPermalink(permalink.toString()))
                    .thenReturn(a)
                );
            });
        return ServerResponse.ok().body(uploadAttachment, Attachment.class);
    }

    /**
     * Upload form from console. The file and url are mutually exclusive. If both are provided,
     * the file will be used.
     *
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UploadForm {

        /**
         * The file to upload. If not provided, the url will be used.
         */
        @Nullable
        private FilePart file;

        /**
         * The filename to use when uploading from url. If not provided, the filename will be
         * extracted from the url.
         */
        @Nullable
        private String filename;

        /**
         * The url to upload from. If not provided, the file will be used.
         */
        @Nullable
        private String url;

    }
}
