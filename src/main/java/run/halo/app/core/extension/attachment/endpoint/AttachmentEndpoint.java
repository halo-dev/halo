package run.halo.app.core.extension.attachment.endpoint;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;
import static org.springframework.web.reactive.function.BodyExtractors.toMultipartData;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static run.halo.app.core.extension.attachment.Constant.FINALIZER_NAME;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.pf4j.PluginManager;
import org.springdoc.core.fn.builders.requestbody.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.attachment.Policy;
import run.halo.app.core.extension.attachment.endpoint.AttachmentHandler.UploadOption;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Ref;

@Slf4j
@Component
public class AttachmentEndpoint implements CustomEndpoint {

    private final ReactiveExtensionClient client;

    private final PluginManager pluginManager;

    public AttachmentEndpoint(ReactiveExtensionClient client, PluginManager pluginManager) {
        this.client = client;
        this.pluginManager = pluginManager;
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = "storage.halo.run/v1alpha1/Attachment";
        return SpringdocRouteBuilder.route()
            .POST("/attachments/upload", contentType(MediaType.MULTIPART_FORM_DATA), this::upload,
                builder -> {
                    builder.operationId("UploadAttachment")
                        .tag(tag)
                        .requestBody(Builder.requestBodyBuilder()
                            .required(true)
                            .content(contentBuilder()
                                .mediaType(MediaType.MULTIPART_FORM_DATA_VALUE)
                                .schema(schemaBuilder().implementation(IUploadRequest.class))
                            ))
                        .response(responseBuilder().implementation(Attachment.class))
                        .build();
                }).build();
    }

    public interface IUploadRequest {

        @Schema(required = true, description = "Attachment file")
        FilePart getFile();

        @Schema(required = true, description = "Storage policy name")
        String getPolicyName();

        @Schema(description = "The name of the group to which the attachment belongs")
        String getGroupName();

    }

    public record UploadRequest(MultiValueMap<String, Part> formData) implements IUploadRequest {

        public FilePart getFile() {
            if (formData.getFirst("file") instanceof FilePart file) {
                return file;
            }
            throw new ServerWebInputException("Invalid part of file");
        }

        public String getPolicyName() {
            if (formData.getFirst("policyName") instanceof FormFieldPart form) {
                return form.value();
            }
            throw new ServerWebInputException("Invalid part of policyName");
        }

        @Override
        public String getGroupName() {
            if (formData.getFirst("groupName") instanceof FormFieldPart form) {
                return form.value();
            }
            return null;
        }

    }

    private Mono<ServerResponse> upload(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .map(Authentication::getName)
            .flatMap(username -> request.body(toMultipartData())
                .map(UploadRequest::new)
                // prepare the upload option
                .flatMap(uploadRequest -> client.get(Policy.class, uploadRequest.getPolicyName())
                    .filter(policy -> policy.getSpec().getConfigMapRef() != null)
                    .switchIfEmpty(Mono.error(() -> new ServerWebInputException(
                        "Please configure the attachment policy before uploading")))
                    .flatMap(policy -> {
                        var configMapName = policy.getSpec().getConfigMapRef().getName();
                        return client.get(ConfigMap.class, configMapName)
                            .map(configMap -> new UploadOption(uploadRequest.getFile(), policy,
                                configMap));
                    })
                    // find the proper handler to handle the attachment
                    .flatMap(uploadOption -> Flux.fromIterable(
                            pluginManager.getExtensions(AttachmentHandler.class))
                        .concatMap(uploadHandler -> uploadHandler.upload(uploadOption)
                            .doOnNext(attachment -> {
                                var spec = attachment.getSpec();
                                spec.setUploadedBy(Ref.of(username));
                                spec.setPolicyRef(Ref.of(uploadOption.policy()));
                                var groupName = uploadRequest.getGroupName();
                                if (groupName != null) {
                                    // validate the group name
                                    spec.setGroupRef(Ref.of(groupName));
                                }
                                // set finalizers mandatory
                                attachment.getMetadata().setFinalizers(Set.of(FINALIZER_NAME));
                            }))
                        .next()
                        .switchIfEmpty(Mono.error(
                            () -> new ResponseStatusException(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "No suitable handler found for uploading the attachment"))))
                )
                // create the attachment
                .flatMap(client::create)
                .flatMap(attachment -> ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(attachment)));
    }

}
