package run.halo.app.core.endpoint.uc;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static run.halo.app.extension.index.query.QueryFactory.equal;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.content.PostService;
import run.halo.app.core.attachment.AttachmentLister;
import run.halo.app.core.attachment.SearchRequest;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.core.extension.service.AttachmentService;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.exception.NotFoundException;

@Component
public class UcAttachmentEndpoint implements CustomEndpoint {

    public static final String POST_NAME_LABEL = "content.halo.run/post-name";
    public static final String SINGLE_PAGE_NAME_LABEL = "content.halo.run/single-page-name";

    private final AttachmentService attachmentService;

    private final AttachmentLister attachmentLister;

    private final PostService postService;

    private final SystemConfigurableEnvironmentFetcher systemSettingFetcher;

    public UcAttachmentEndpoint(AttachmentService attachmentService,
        AttachmentLister attachmentLister, PostService postService,
        SystemConfigurableEnvironmentFetcher systemSettingFetcher) {
        this.attachmentService = attachmentService;
        this.attachmentLister = attachmentLister;
        this.postService = postService;
        this.systemSettingFetcher = systemSettingFetcher;
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = "AttachmentV1alpha1Uc";
        return route()
            .POST("/attachments",
                this::createAttachmentForPost,
                builder -> builder.operationId("CreateAttachmentForPost").tag(tag)
                    .description("Create attachment for the given post.")
                    .parameter(parameterBuilder()
                        .name("waitForPermalink")
                        .description("Wait for permalink.")
                        .in(ParameterIn.QUERY)
                        .required(false)
                        .implementation(boolean.class))
                    .requestBody(requestBodyBuilder()
                        .content(contentBuilder()
                            .mediaType(MediaType.MULTIPART_FORM_DATA_VALUE)
                            .schema(schemaBuilder().implementation(PostAttachmentRequest.class)))
                    )
                    .response(responseBuilder().implementation(Attachment.class))
            )
            .POST("/attachments/-/upload", contentType(MediaType.MULTIPART_FORM_DATA),
                this::uploadAttachment, builder -> builder
                    .operationId("UploadUcAttachment")
                    .description("Upload attachment to user center storage.")
                    .tag(tag)
                    .requestBody(requestBodyBuilder()
                        .required(true)
                        .content(contentBuilder()
                            .mediaType(MediaType.MULTIPART_FORM_DATA_VALUE)
                            .schema(schemaBuilder().implementation(UcUploadRequest.class))
                        ))
                    .response(responseBuilder().implementation(Attachment.class))
            )
            .POST("/attachments/-/upload-from-url", contentType(MediaType.APPLICATION_JSON),
                this::uploadFromUrlForPost,
                builder -> builder
                    .operationId("ExternalTransferAttachment_1")
                    .description("Upload attachment from the given URL.")
                    .tag(tag)
                    .parameter(parameterBuilder()
                        .name("waitForPermalink")
                        .description("Wait for permalink.")
                        .in(ParameterIn.QUERY)
                        .required(false)
                        .implementation(boolean.class))
                    .requestBody(requestBodyBuilder()
                        .required(true)
                        .content(contentBuilder()
                            .mediaType(MediaType.APPLICATION_JSON_VALUE)
                            .schema(schemaBuilder().implementation(UploadFromUrlRequest.class))
                        ))
                    .response(responseBuilder().implementation(Attachment.class))
                    .build()
            )
            .GET("/attachments", this::listMyAttachments, builder -> {
                builder.operationId("ListMyAttachments")
                    .description("List attachments of the current user uploaded.")
                    .tag(tag)
                    .response(responseBuilder()
                        .implementation(ListResult.generateGenericClass(Attachment.class))
                    );
                SearchRequest.buildParameters(builder);
            })
            .build();
    }

    private Mono<ServerResponse> uploadAttachment(ServerRequest request) {
        var builder = UploadContext.builder();
        var filePartMono = request.body(BodyExtractors.toMultipartData())
            .map(UcUploadRequest::new)
            .switchIfEmpty(
                Mono.error(new ServerWebInputException("Required request body is missing.")))
            .doOnNext(uploadRequest -> builder.filePart(uploadRequest.getFile()))
            .subscribeOn(Schedulers.boundedElastic());

        var ownerMono = getCurrentUser()
            .doOnNext(builder::owner);

        var storagePolicyMono =
            systemSettingFetcher.fetch(SystemSetting.User.GROUP, SystemSetting.User.class)
                .mapNotNull(SystemSetting.User::getUcAttachmentPolicy)
                .filter(StringUtils::isNotBlank)
                .switchIfEmpty(Mono.error(new ServerWebInputException(
                    "Please contact the administrator to configure the storage policy."))
                )
                .doOnNext(builder::storagePolicy)
                .subscribeOn(Schedulers.boundedElastic());

        return Mono.when(filePartMono, storagePolicyMono, ownerMono)
            .then(Mono.fromSupplier(builder::build))
            .flatMap(context -> attachmentService.upload(context.owner(),
                context.storagePolicy(), null, context.filePart(), null)
            )
            .flatMap(attachment -> ServerResponse.ok().bodyValue(attachment));
    }

    private Mono<ServerResponse> listMyAttachments(ServerRequest request) {
        return getCurrentUser()
            .flatMap(username -> {
                var searchRequest = new UcSearchRequest(request, username);
                return attachmentLister.listBy(searchRequest)
                    .flatMap(listResult -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(listResult)
                    );
            });
    }

    @Builder
    record UploadContext(String owner, String storagePolicy, FilePart filePart) {
    }

    public record UcUploadRequest(MultiValueMap<String, Part> formData) {

        @Schema(description = "The file to upload.", requiredMode = REQUIRED)
        public FilePart getFile() {
            if (formData.getFirst("file") instanceof FilePart file) {
                return file;
            }
            throw new ServerWebInputException("Invalid part of file");
        }
    }

    @Getter
    public static class UcSearchRequest extends SearchRequest {
        private final String owner;

        public UcSearchRequest(ServerRequest request, String owner) {
            super(request);
            Assert.state(StringUtils.isNotBlank(owner), "Owner must not be blank.");
            this.owner = owner;
        }

        @Override
        public ListOptions toListOptions(List<String> hiddenGroups) {
            var listOptions = super.toListOptions(hiddenGroups);
            return ListOptions.builder(listOptions)
                .andQuery((equal("spec.ownerName", owner)))
                .build();
        }
    }

    private Mono<ServerResponse> uploadFromUrlForPost(ServerRequest request) {
        var uploadFromUrlRequestMono = request.bodyToMono(UploadFromUrlRequest.class);

        var uploadAttachment = getPostSettingMono()
            .flatMap(postSetting -> uploadFromUrlRequestMono.flatMap(
                uploadFromUrlRequest -> {
                    var url = uploadFromUrlRequest.url();
                    var fileName = uploadFromUrlRequest.filename();
                    return attachmentService.uploadFromUrl(url,
                        postSetting.getAttachmentPolicyName(),
                        postSetting.getAttachmentGroupName(),
                        fileName
                    );
                })
            );

        var waitForPermalink = request.queryParam("waitForPermalink")
            .map(Boolean::valueOf)
            .orElse(false);
        if (waitForPermalink) {
            uploadAttachment = waitForPermalink(uploadAttachment);
        }
        return ServerResponse.ok().body(uploadAttachment, Attachment.class);
    }

    private Mono<ServerResponse> createAttachmentForPost(ServerRequest request) {
        var postAttachmentRequestMono = request.body(BodyExtractors.toMultipartData())
            .map(PostAttachmentRequest::from)
            .cache();

        // get settings
        var createdAttachment =
            getPostSettingMono().flatMap(postSetting -> postAttachmentRequestMono
                .flatMap(postAttachmentRequest -> getCurrentUser().flatMap(
                    username -> attachmentService.upload(username,
                        postSetting.getAttachmentPolicyName(),
                        postSetting.getAttachmentGroupName(),
                        postAttachmentRequest.file(),
                        linkWith(postAttachmentRequest)))));

        var waitForPermalink = request.queryParam("waitForPermalink")
            .map(Boolean::valueOf)
            .orElse(false);
        if (waitForPermalink) {
            createdAttachment = waitForPermalink(createdAttachment);
        }
        return ServerResponse.ok().body(createdAttachment, Attachment.class);
    }

    private Mono<Attachment> waitForPermalink(Mono<Attachment> createdAttachment) {
        createdAttachment = createdAttachment.flatMap(attachment ->
            attachmentService.getPermalink(attachment)
                .doOnNext(permalink -> {
                    var status = attachment.getStatus();
                    if (status == null) {
                        status = new Attachment.AttachmentStatus();
                        attachment.setStatus(status);
                    }
                    status.setPermalink(permalink.toString());
                })
                .thenReturn(attachment));
        return createdAttachment;
    }

    private Mono<SystemSetting.Post> getPostSettingMono() {
        return systemSettingFetcher.fetchPost().handle((postSetting, sink) -> {
            var attachmentPolicyName = postSetting.getAttachmentPolicyName();
            if (StringUtils.isBlank(attachmentPolicyName)) {
                sink.error(new ServerWebInputException(
                    "Please configure storage policy for post attachment first."));
                return;
            }
            sink.next(postSetting);
        });
    }

    private Consumer<Attachment> linkWith(PostAttachmentRequest request) {
        return attachment -> {
            var labels = attachment.getMetadata().getLabels();
            if (labels == null) {
                labels = new HashMap<>();
                attachment.getMetadata().setLabels(labels);
            }
            if (StringUtils.isNotBlank(request.postName())) {
                labels.put(POST_NAME_LABEL, request.postName());
            }
            if (StringUtils.isNotBlank(request.singlePageName())) {
                labels.put(SINGLE_PAGE_NAME_LABEL, request.singlePageName());
            }
        };
    }

    private Mono<Void> checkPostOwnership(Mono<PostAttachmentRequest> postAttachmentRequest) {
        // check the post
        var postNotFoundError = Mono.<Post>error(
            () -> new NotFoundException("The post was not found or deleted.")
        );
        return postAttachmentRequest.map(PostAttachmentRequest::postName)
            .flatMap(postName -> getCurrentUser()
                .flatMap(username -> postService.getByUsername(postName, username)
                    .switchIfEmpty(postNotFoundError)))
            .then();
    }

    private Mono<String> getCurrentUser() {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .map(Authentication::getName);
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("uc.api.storage.halo.run/v1alpha1");
    }

    public record UploadFromUrlRequest(@Schema(requiredMode = REQUIRED) URL url,
                                       String filename) {
        public UploadFromUrlRequest {
            if (Objects.isNull(url)) {
                throw new ServerWebInputException("Required url is missing.");
            }
        }
    }

    @Schema(types = "object")
    public record PostAttachmentRequest(
        @Schema(requiredMode = REQUIRED, description = "Attachment data.")
        FilePart file,

        @Schema(requiredMode = NOT_REQUIRED, description = "Post name.")
        String postName,

        @Schema(requiredMode = NOT_REQUIRED, description = "Single page name.")
        String singlePageName
    ) {

        /**
         * Convert multipart data into PostAttachmentRequest.
         *
         * @param multipartData is multipart data from request.
         * @return post attachment request data.
         */
        public static PostAttachmentRequest from(MultiValueMap<String, Part> multipartData) {
            var part = multipartData.getFirst("postName");
            String postName = null;
            if (part instanceof FormFieldPart formFieldPart) {
                postName = formFieldPart.value();
            }

            part = multipartData.getFirst("singlePageName");
            String singlePageName = null;
            if (part instanceof FormFieldPart formFieldPart) {
                singlePageName = formFieldPart.value();
            }

            part = multipartData.getFirst("file");
            if (!(part instanceof FilePart file)) {
                throw new ServerWebInputException("Invalid type of parameter 'file'.");
            }

            return new PostAttachmentRequest(file, postName, singlePageName);
        }

    }
}
