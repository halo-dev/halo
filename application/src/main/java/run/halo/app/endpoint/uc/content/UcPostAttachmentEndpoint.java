package run.halo.app.endpoint.uc.content;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;
import static org.springdoc.webflux.core.fn.SpringdocRouteBuilder.route;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashMap;
import java.util.function.Consumer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.content.PostService;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.core.extension.service.AttachmentService;
import run.halo.app.extension.GroupVersion;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.exception.NotFoundException;

@Component
public class UcPostAttachmentEndpoint implements CustomEndpoint {

    public static final String POST_NAME_LABEL = "content.halo.run/post-name";
    public static final String SINGLE_PAGE_NAME_LABEL = "content.halo.run/single-page-name";

    private final AttachmentService attachmentService;

    private final PostService postService;

    private final SystemConfigurableEnvironmentFetcher systemSettingFetcher;

    public UcPostAttachmentEndpoint(AttachmentService attachmentService, PostService postService,
        SystemConfigurableEnvironmentFetcher systemSettingFetcher) {
        this.attachmentService = attachmentService;
        this.postService = postService;
        this.systemSettingFetcher = systemSettingFetcher;
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = groupVersion() + "/Attachment";
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
            .build();
    }

    private Mono<ServerResponse> createAttachmentForPost(ServerRequest request) {
        var postAttachmentRequestMono = request.body(BodyExtractors.toMultipartData())
            .map(PostAttachmentRequest::from)
            .cache();

        var postSettingMono = systemSettingFetcher.fetchPost()
            .<SystemSetting.Post>handle((postSetting, sink) -> {
                var attachmentPolicyName = postSetting.getAttachmentPolicyName();
                if (StringUtils.isBlank(attachmentPolicyName)) {
                    sink.error(new ServerWebInputException(
                        "Please configure storage policy for post attachment first."));
                    return;
                }
                sink.next(postSetting);
            });

        // get settings
        var createdAttachment = postSettingMono.flatMap(postSetting -> postAttachmentRequestMono
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
        }
        return ServerResponse.ok().body(createdAttachment, Attachment.class);
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
        return GroupVersion.parseAPIVersion("uc.api.content.halo.run/v1alpha1");
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
