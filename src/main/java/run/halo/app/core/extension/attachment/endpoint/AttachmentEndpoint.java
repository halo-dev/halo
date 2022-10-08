package run.halo.app.core.extension.attachment.endpoint;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;
import static org.springframework.web.reactive.function.BodyExtractors.toMultipartData;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static run.halo.app.extension.ListResult.generateGenericClass;
import static run.halo.app.extension.router.QueryParamBuildUtil.buildParametersFromType;
import static run.halo.app.extension.router.selector.SelectorUtil.labelAndFieldSelectorToPredicate;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Optional;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.util.StringUtils;
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
import run.halo.app.extension.router.IListRequest;
import run.halo.app.extension.router.IListRequest.QueryListRequest;
import run.halo.app.plugin.ExtensionComponentsFinder;

@Slf4j
@Component
public class AttachmentEndpoint implements CustomEndpoint {

    private final ReactiveExtensionClient client;

    private final ExtensionComponentsFinder extensionComponentsFinder;

    public AttachmentEndpoint(ReactiveExtensionClient client,
        ExtensionComponentsFinder extensionComponentsFinder) {
        this.client = client;
        this.extensionComponentsFinder = extensionComponentsFinder;
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = "api.console.halo.run/v1alpha1/Attachment";
        return SpringdocRouteBuilder.route()
            .POST("/attachments/upload", contentType(MediaType.MULTIPART_FORM_DATA), this::upload,
                builder -> builder
                    .operationId("UploadAttachment")
                    .tag(tag)
                    .requestBody(Builder.requestBodyBuilder()
                        .required(true)
                        .content(contentBuilder()
                            .mediaType(MediaType.MULTIPART_FORM_DATA_VALUE)
                            .schema(schemaBuilder().implementation(IUploadRequest.class))
                        ))
                    .response(responseBuilder().implementation(Attachment.class))
                    .build())
            .GET("/attachments", this::search,
                builder -> {
                    builder
                        .operationId("SearchAttachments")
                        .tag(tag)
                        .response(
                            responseBuilder().implementation(generateGenericClass(Attachment.class))
                        );
                    buildParametersFromType(builder, ISearchRequest.class);
                }
            )
            .build();
    }

    Mono<ServerResponse> upload(ServerRequest request) {
        return ReactiveSecurityContextHolder.getContext()
            .switchIfEmpty(Mono.error(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "Please login first and try it again")))
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
                            extensionComponentsFinder.getExtensions(AttachmentHandler.class))
                        .concatMap(uploadHandler -> uploadHandler.upload(uploadOption)
                            .doOnNext(attachment -> {
                                var spec = attachment.getSpec();
                                if (spec == null) {
                                    spec = new Attachment.AttachmentSpec();
                                    attachment.setSpec(spec);
                                }
                                spec.setUploadedBy(Ref.of(username));
                                spec.setPolicyRef(Ref.of(uploadOption.policy()));
                                var groupName = uploadRequest.getGroupName();
                                if (groupName != null) {
                                    // validate the group name
                                    spec.setGroupRef(Ref.of(groupName));
                                }
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

    Mono<ServerResponse> search(ServerRequest request) {
        var searchRequest = new SearchRequest(request.queryParams());
        return client.list(Attachment.class, searchRequest.toPredicate(), null,
                searchRequest.getPage(), searchRequest.getSize())
            .flatMap(listResult -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(listResult));
    }

    public interface ISearchRequest extends IListRequest {

        @Schema(description = "Display name of attachment")
        Optional<String> getDisplayName();

        @Schema(description = "Name of policy")
        Optional<String> getPolicy();

        @Schema(description = "Name of group")
        Optional<String> getGroup();

        @Schema(description = "Name of user who uploaded the attachment")
        Optional<String> getUploadedBy();

    }

    public static class SearchRequest extends QueryListRequest implements ISearchRequest {

        public SearchRequest(MultiValueMap<String, String> queryParams) {
            super(queryParams);
        }

        @Override
        public Optional<String> getDisplayName() {
            return Optional.ofNullable(queryParams.getFirst("displayName"))
                .filter(StringUtils::hasText);
        }

        @Override
        public Optional<String> getPolicy() {
            return Optional.ofNullable(queryParams.getFirst("policy"))
                .filter(StringUtils::hasText);
        }

        @Override
        public Optional<String> getGroup() {
            return Optional.ofNullable(queryParams.getFirst("group"))
                .filter(StringUtils::hasText);
        }

        @Override
        public Optional<String> getUploadedBy() {
            return Optional.ofNullable(queryParams.getFirst("uploadedBy"))
                .filter(StringUtils::hasText);
        }

        public Predicate<Attachment> toPredicate() {
            var predicate = (Predicate<Attachment>) (attachment) -> getDisplayName()
                .map(displayNameInParam -> {
                    String displayName = attachment.getSpec().getDisplayName();
                    return displayName.contains(displayNameInParam);
                }).orElse(true)
                && getPolicy()
                .map(policy -> {
                    var policyRef = attachment.getSpec().getPolicyRef();
                    return policyRef != null && policy.equals(policyRef.getName());
                }).orElse(true)
                && getGroup()
                .map(group -> {
                    var groupRef = attachment.getSpec().getGroupRef();
                    return groupRef != null && group.equals(groupRef.getName());
                })
                .orElse(true)
                && getUploadedBy()
                .map(uploadedBy -> {
                    var uploadedByRef = attachment.getSpec().getUploadedBy();
                    return uploadedByRef != null && uploadedBy.equals(uploadedByRef.getName());
                })
                .orElse(true);

            var selectorPredicate = labelAndFieldSelectorToPredicate(getLabelSelector(),
                getFieldSelector());

            return predicate.and(selectorPredicate);
        }
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
}
