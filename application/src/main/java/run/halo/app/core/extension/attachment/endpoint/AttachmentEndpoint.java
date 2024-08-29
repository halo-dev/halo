package run.halo.app.core.extension.attachment.endpoint;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.arrayschema.Builder.arraySchemaBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springdoc.core.fn.builders.schema.Builder.schemaBuilder;
import static org.springframework.boot.convert.ApplicationConversionService.getSharedInstance;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static run.halo.app.extension.ListResult.generateGenericClass;
import static run.halo.app.extension.index.query.QueryFactory.contains;
import static run.halo.app.extension.index.query.QueryFactory.in;
import static run.halo.app.extension.index.query.QueryFactory.isNull;
import static run.halo.app.extension.index.query.QueryFactory.not;
import static run.halo.app.extension.index.query.QueryFactory.startsWith;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.fn.builders.operation.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.attachment.Group;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.core.extension.service.AttachmentService;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.index.query.QueryFactory;
import run.halo.app.extension.router.IListRequest;
import run.halo.app.extension.router.QueryParamBuildUtil;
import run.halo.app.extension.router.SortableRequest;
import run.halo.app.extension.router.selector.LabelSelector;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttachmentEndpoint implements CustomEndpoint {

    private final AttachmentService attachmentService;

    private final ReactiveExtensionClient client;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = "AttachmentV1alpha1Console";
        return SpringdocRouteBuilder.route()
            .POST("/attachments/upload", contentType(MediaType.MULTIPART_FORM_DATA),
                request -> request.body(BodyExtractors.toMultipartData())
                    .map(UploadRequest::new)
                    .flatMap(uploadReq -> {
                        var policyName = uploadReq.getPolicyName();
                        var groupName = uploadReq.getGroupName();
                        var filePart = uploadReq.getFile();
                        return attachmentService.upload(policyName,
                            groupName,
                            filePart.filename(),
                            filePart.content(),
                            filePart.headers().getContentType());
                    })
                    .flatMap(attachment -> ServerResponse.ok().bodyValue(attachment)),
                builder -> builder
                    .operationId("UploadAttachment")
                    .tag(tag)
                    .requestBody(requestBodyBuilder()
                        .required(true)
                        .content(contentBuilder()
                            .mediaType(MediaType.MULTIPART_FORM_DATA_VALUE)
                            .schema(schemaBuilder().implementation(IUploadRequest.class))
                        ))
                    .response(responseBuilder().implementation(Attachment.class))
                    .build())
            .POST("/attachments/-/upload-from-url", contentType(MediaType.APPLICATION_JSON),
                request -> request.bodyToMono(UploadFromUrlRequest.class)
                    .flatMap(uploadFromUrlRequest -> {
                        var url = uploadFromUrlRequest.url();
                        var policyName = uploadFromUrlRequest.policyName();
                        var groupName = uploadFromUrlRequest.groupName();
                        var fileName = uploadFromUrlRequest.filename();
                        return attachmentService.uploadFromUrl(url, policyName,
                            groupName, fileName);
                    })
                    .flatMap(attachment -> ServerResponse.ok().bodyValue(attachment)),
                builder -> builder
                    .operationId("ExternalTransferAttachment")
                    .tag(tag)
                    .requestBody(requestBodyBuilder()
                        .required(true)
                        .content(contentBuilder()
                            .mediaType(MediaType.APPLICATION_JSON_VALUE)
                            .schema(schemaBuilder().implementation(UploadFromUrlRequest.class))
                        ))
                    .response(responseBuilder().implementation(Attachment.class))
                    .build()
            )
            .GET("/attachments", this::search,
                builder -> {
                    builder
                        .operationId("SearchAttachments")
                        .tag(tag)
                        .response(
                            responseBuilder().implementation(generateGenericClass(Attachment.class))
                        );
                    SearchRequest.buildParameters(builder);
                }
            )
            .build();
    }

    Mono<ServerResponse> search(ServerRequest request) {
        var searchRequest = new SearchRequest(request);
        var groupListOptions = new ListOptions();
        groupListOptions.setLabelSelector(LabelSelector.builder()
            .exists(Group.HIDDEN_LABEL)
            .build());
        return client.listAll(Group.class, groupListOptions, Sort.unsorted())
            .map(group -> group.getMetadata().getName())
            .collectList()
            .defaultIfEmpty(List.of())
            .flatMap(hiddenGroups -> client.listBy(Attachment.class,
                        searchRequest.toListOptions(hiddenGroups),
                        PageRequestImpl.of(searchRequest.getPage(), searchRequest.getSize(),
                            searchRequest.getSort())
                    )
                    .flatMap(listResult -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(listResult)
                    )
            );
    }

    public static class SearchRequest extends SortableRequest {

        public SearchRequest(ServerRequest request) {
            super(request.exchange());
        }

        public Optional<String> getKeyword() {
            return Optional.ofNullable(queryParams.getFirst("keyword"))
                .filter(StringUtils::hasText);
        }

        public Optional<Boolean> getUngrouped() {
            return Optional.ofNullable(queryParams.getFirst("ungrouped"))
                .map(ungroupedStr -> getSharedInstance().convert(ungroupedStr, Boolean.class));
        }

        public Optional<List<String>> getAccepts() {
            return Optional.ofNullable(queryParams.get("accepts"))
                .filter(accepts -> !accepts.isEmpty()
                    && !accepts.contains("*")
                    && !accepts.contains("*/*")
                );
        }

        public ListOptions toListOptions(List<String> hiddenGroups) {
            var builder = ListOptions.builder(super.toListOptions());

            getKeyword().ifPresent(keyword -> {
                builder.andQuery(contains("spec.displayName", keyword));
            });

            getUngrouped()
                .filter(ungrouped -> ungrouped)
                .ifPresent(ungrouped -> builder.andQuery(isNull("spec.groupName")));

            if (!CollectionUtils.isEmpty(hiddenGroups)) {
                builder.andQuery(not(in("spec.groupName", hiddenGroups)));
            }

            getAccepts().flatMap(accepts -> accepts.stream()
                    .filter(StringUtils::hasText)
                    .map(accept -> accept.replace("/*", "/").toLowerCase())
                    .distinct()
                    .map(accept -> startsWith("spec.mediaType", accept))
                    .reduce(QueryFactory::or)
                )
                .ifPresent(builder::andQuery);

            return builder.build();
        }

        public static void buildParameters(Builder builder) {
            IListRequest.buildParameters(builder);
            builder.parameter(QueryParamBuildUtil.sortParameter())
                .parameter(parameterBuilder()
                    .in(ParameterIn.QUERY)
                    .name("ungrouped")
                    .required(false)
                    .description("""
                        Filter attachments without group. This parameter will ignore group \
                        parameter.\
                        """)
                    .implementation(Boolean.class))
                .parameter(parameterBuilder()
                    .in(ParameterIn.QUERY)
                    .name("keyword")
                    .required(false)
                    .description("Keyword for searching.")
                    .implementation(String.class))
                .parameter(parameterBuilder()
                    .in(ParameterIn.QUERY)
                    .name("accepts")
                    .required(false)
                    .description("Acceptable media types.")
                    .array(
                        arraySchemaBuilder()
                            .uniqueItems(true)
                            .schema(schemaBuilder()
                                .implementation(String.class)
                                .example("image/*"))
                    )
                    .implementationArray(String.class)
                );
        }
    }

    public record UploadFromUrlRequest(@Schema(requiredMode = REQUIRED) URL url,
                                       @Schema(requiredMode = REQUIRED) String policyName,
                                       String groupName,
                                       String filename) {
        public UploadFromUrlRequest {
            if (Objects.isNull(url)) {
                throw new ServerWebInputException("Required url is missing.");
            }

            if (!StringUtils.hasText(policyName)) {
                throw new ServerWebInputException("Policy name must not be blank");
            }
        }
    }

    @Schema(types = "object")
    public interface IUploadRequest {

        @Schema(requiredMode = REQUIRED, description = "Attachment file")
        FilePart getFile();

        @Schema(requiredMode = REQUIRED, description = "Storage policy name")
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
